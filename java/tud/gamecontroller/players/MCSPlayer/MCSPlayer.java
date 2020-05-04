/*
    Copyright (C) 2008-2010 Stephan Schiffel <stephan.schiffel@gmx.de>
                  2010-2020 Nicolas JEAN <njean42@gmail.com>
                  2020 Michael Dorrell <michael.dorrell97@gmail.com>

    This file is part of GameController.

    GameController is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameController is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameController.  If not, see <http://www.gnu.org/licenses/>.
*/

package tud.gamecontroller.players.MCSPlayer;

import tud.gamecontroller.ConnectionEstablishedNotifier;
import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.RunnableMatchInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.game.impl.JointMove;
import tud.gamecontroller.players.LocalPlayer;
import tud.gamecontroller.term.TermInterface;

import java.util.*;

public class MCSPlayer<
	TermType extends TermInterface,
	StateType extends StateInterface<TermType, ? extends StateType>> extends LocalPlayer<TermType, StateType>  {

	private Random random;
	private long timeLimit;
	private long startTime;
	private long timeexpired;
	private RoleInterface<TermType> opponentRole;

	private static final long PREFERRED_METAGAME_BUFFER = 3900;
	private static final long PREFERRED_PLAY_BUFFER = 1000;
	private int numProbes = 4;
	
	public MCSPlayer(String name, GDLVersion gdlVersion) {
		super(name, GDLVersion.v1); // If the player will only work for GDL-I games
		random = new Random();
	}

	@Override
	public void gameStart(RunnableMatchInterface<TermType, StateType> match, RoleInterface<TermType> role, ConnectionEstablishedNotifier notifier) {
		super.gameStart(match, role, notifier);

		// Instantiate globals
		timeLimit = (this.match.getPlayclock()*1000 - PREFERRED_PLAY_BUFFER);

		// Instantiate opponent
		List<? extends RoleInterface<TermType>> allRoles = this.match.getGame().getOrderedRoles();
		for (RoleInterface<TermType> validRole : allRoles) {
			if(!validRole.equals(this.role)) {
				opponentRole = validRole;
			}
		}
	}

	public MoveInterface<TermType> getNextMove() {
		ArrayList<MoveInterface<TermType>> legalMoves = new ArrayList<MoveInterface<TermType>>(getLegalMoves());

		// Calculate amount of time to run for and track time
		startTime = System.currentTimeMillis();
		timeexpired = 0;

		// Only run if MCS if multiple options
		MoveInterface<TermType> selection = legalMoves.get(0);
		if(legalMoves.size() > 1) {
			selection = MCS(legalMoves);
		}

		return selection;
	}

	/**
	 * Runs a iterative depth limited DFS to build a game tree using monte carlo simulation to approximate the value of non-terminal nodes
	 *
	 * @param moves The legal moves given the current state
	 * @return The optimal move
	 */
	private MoveInterface<TermType> MCS(ArrayList<MoveInterface<TermType>> moves) {
		MoveInterface<TermType> bestMove = null;

		int depth = 1;
		float score;
		float bestScore = 0;

		while(timeexpired < timeLimit) {
			bestScore = 0;

			// For each move, calculate the minimax score at the given depth
			for (MoveInterface<TermType> move : moves) {
				score = minscore(move, this.currentState, depth, 0);
				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
			}
			timeexpired = System.currentTimeMillis() - startTime;
			depth++;
		}

		return bestMove;
	}

	/**
	 * Uses a joint move to produce the next state for a given state using GDL-I
	 * Will return null if any move is not legal in the current state
	 *
	 * @param playerMove   The move the player will take
	 * @param opponentMove The move the opponent will take
	 * @param currState    The current state of the game
	 * @return             The successive state after these moves
	 */
	private StateInterface<TermType, ?> getNextState(MoveInterface<TermType> playerMove, MoveInterface<TermType> opponentMove, StateInterface<TermType, ?> currState) {
		Map<RoleInterface<TermType>, MoveInterface<TermType>> moveMap = new HashMap<RoleInterface<TermType>, MoveInterface<TermType>>();
		moveMap.put(role, playerMove);
		moveMap.put(opponentRole, opponentMove);
		return currState.isLegal(role, playerMove) && currState.isLegal(opponentRole, opponentMove) ? currState.getSuccessor(new JointMove<TermType>(this.match.getGame().getOrderedRoles(), moveMap)) : null;
	}

	/**
	 * Returns expected value of a state by running numProbes simulations
	 *
	 * @param state      The given state to assess the value of
	 * @param numProbes  The number of probes to run
	 * @return           The expected value of a non-terminal state
	 */
	private float montecarlo(StateInterface<TermType, ?> state, int numProbes) {
		float total = 0;
		for(int i = 0 ; i < numProbes ; i++) {
			total += depthcharge(state);
		}
		return total/numProbes;
	}

	private float depthcharge(StateInterface<TermType, ?> state) {
		if(state.isTerminal()) {
			return (float) state.getGoalValue(role);
		}
		// Choose moves randomly
		ArrayList<MoveInterface<TermType>> playerMoves = new ArrayList<MoveInterface<TermType>>(state.getLegalMoves(role));
		int playerChoice = random.nextInt(playerMoves.size());
		ArrayList<MoveInterface<TermType>> opponentMoves = new ArrayList<MoveInterface<TermType>>(state.getLegalMoves(opponentRole));
		int opponentChoice = random.nextInt(opponentMoves.size());
		StateInterface<TermType, ?> newState = getNextState(playerMoves.get(playerChoice), opponentMoves.get(opponentChoice), state);

		assert newState != null;
		return depthcharge(newState);
	}

	/**
	 * Returns the minimum score possible for a given move and a given state
	 *
	 * @param move
	 * @param currState
	 * @param searchDepth
	 * @return
	 */
	private float minscore(MoveInterface<TermType> move, StateInterface<TermType, ?> currState, int searchDepth, int depthCount) {
		float worstScore = 100;

		// Check roles size
		if(this.match.getPlayers().size() == 2) { // If two players
			StateInterface<TermType, ?> newState;
			float currScore;

			// Get all possible moves by the opponent and search through them
			ArrayList<MoveInterface<TermType>> opponentMoves = new ArrayList<MoveInterface<TermType>>(currState.getLegalMoves(opponentRole));
			for(MoveInterface<TermType> opponentMove : opponentMoves) {
				// Check which is the optimal for the opponent
				newState = getNextState(move, opponentMove, currState);
				currScore = maxscore(newState, searchDepth, depthCount);
				worstScore = Math.min(currScore, worstScore);
			}
		} else if(this.match.getPlayers().size() == 1) { // If one player
			System.err.println("Cannot currently handle a single player");
		} else { // If unexpected number of players
			System.err.println("Unexpected number of players: " + this.match.getPlayers().size());
		}

		return worstScore;
	}

	private float maxscore(StateInterface<TermType, ?> currState, int searchDepth, int depthCount) {
		timeexpired = System.currentTimeMillis() - startTime;
		if(currState.isTerminal()) {
			return (float) currState.getGoalValue(role);
		}
		if(depthCount >= searchDepth || timeexpired >= timeLimit) {
			return montecarlo(currState, numProbes);
		}
		depthCount++;

		ArrayList<MoveInterface<TermType>> playerMoves = new ArrayList<MoveInterface<TermType>>(currState.getLegalMoves(role));
		float score;
		float bestScore = 0;
		for (MoveInterface<TermType> move : playerMoves) {
			score = minscore(move, currState, searchDepth, depthCount);
			if (score > bestScore) {
				bestScore = score;
			}
		}
		return bestScore;
	}
}
