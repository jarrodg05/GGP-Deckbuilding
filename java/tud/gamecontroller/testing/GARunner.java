package tud.gamecontroller.testing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.ReasonerFactoryInterface;
import tud.gamecontroller.game.FluentInterface;
import tud.gamecontroller.game.JointMoveInterface;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.RunnableMatchInterface;
import tud.gamecontroller.game.impl.Game;
import tud.gamecontroller.game.impl.JointMove;
import tud.gamecontroller.game.impl.RunnableMatch;
import tud.gamecontroller.game.impl.State;
import tud.gamecontroller.game.javaprover.Term;
import tud.gamecontroller.players.Player;
import tud.gamecontroller.players.PlayerFactory;
import tud.gamecontroller.players.PlayerInfo;
import tud.gamecontroller.players.RandomPlayerInfo;
import tud.gamecontroller.players.GAPlayer.GAPlayer;
import tud.gamecontroller.players.GAPlayer.RouletteGenomeCollection;
import cs227b.teamIago.util.GameState;
import tud.gamecontroller.game.javaprover.ReasonerFactory;


public class GARunner {

	Game<Term, GameState> game;
	File gameFile; // get from gui file picker
	final ReasonerFactoryInterface<Term, GameState> reasonerFactoryInterface; 
	GDLVersion gdlVersion = GDLVersion.v1; // can use gui to change
	int trainingIters = 10000;

	String matchID;
	int startClock;
	int playClock;
	Collection<PlayerInfo> playerInfos;
	State<Term, GameState> currentState;

	private Map<RoleInterface<Term>,Player<Term, State<Term, GameState>>> players;
	private RunnableMatchInterface<Term, State<Term, GameState>> match;

	public GARunner() {
		reasonerFactoryInterface = new ReasonerFactory();
	}

	public void setGame(File gameFile){
		this.gameFile = gameFile;
	}

	public void runGUI(){
		try {
			game = new Game<Term, GameState>(gameFile, reasonerFactoryInterface, gdlVersion);
		} catch (IOException e) {
			e.printStackTrace();
		}

		GameControllerFrame frame = new GameControllerFrame(this);
		frame.setVisible(true);
	}

	public void start() throws InterruptedException{
		
		// instantiates this class so the players can use it
		new RouletteGenomeCollection(this);

		players = createPlayers(game);
		match = new RunnableMatch<Term, GameState>(matchID, game, startClock, playClock, players);

		for (int i = 0; i < trainingIters; i++){
			runGame(players);			
		}
	}

	private void runGame(Map<RoleInterface<Term>,Player<Term, State<Term, GameState>>> players) {
		try {
			for (RoleInterface<Term> role : players.keySet()) {
				Player<Term, State<Term, GameState>> player = players.get(role);
				player.gameStart(match, role, null);
			}

			currentState = game.getInitialState();
			State<Term, GameState> priorState = currentState;
			JointMoveInterface<Term> priorJointMove = null;

			long startTime = System.currentTimeMillis();
			long elaspsed = 0l;

			while (!currentState.isTerminal()) {
				JointMoveInterface<Term> jointMove = new JointMove<Term>(game.getOrderedRoles());
				for (RoleInterface<Term> role : players.keySet()) {
					Player<Term, State<Term, GameState>> player = players.get(role);
					Object seesTerm = getSeesTermsForRole(role, player, priorState, priorJointMove);

					try {
					MoveInterface<Term> move = player.gamePlay(seesTerm, null);
					jointMove.put(role, move);
					}
					catch (IllegalArgumentException e) {
					
						//System.out.println(jointMove.getKIFForm());
						for (FluentInterface<Term> f : currentState.getFluents()) {
							System.out.print(f.getKIFForm());
						}
						System.out.println();						
					}


				}

				priorState = currentState;
				currentState = currentState.getSuccessor(jointMove);
				priorJointMove = jointMove;

				elaspsed = System.currentTimeMillis() - startTime;
				// timeout after 2 mins
				if (elaspsed > 1000*60*2) {
					//System.out.println(jointMove.getKIFForm());
					for (FluentInterface<Term> f : currentState.getFluents()) {
						System.out.print(f.getKIFForm());
					}
					System.out.println();
					break;
				}
			}

			HashMap<RoleInterface<Term>, Integer> goalValues = new HashMap<RoleInterface<Term>, Integer>();
			for (RoleInterface<Term> role : game.getOrderedRoles()) {
				int goal = currentState.getGoalValue(role);
				goalValues.put(role, goal);
			}

			for (Player<Term, State<Term, GameState>> player : players.values()) {
				if (player instanceof GAPlayer){
					GAPlayer<Term, State<Term, GameState>> ml = (GAPlayer<Term, State<Term, GameState>>) player;
					ml.gameScore(goalValues);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public int runSims(int numGames, int numGens, int numAgents) {
		RouletteGenomeCollection gc = RouletteGenomeCollection.instance;

		for (int i = 0; i < numAgents; i++) {
			gc.nextAgent();
			for (int j = 0; j < numGens; j++) {
				gc.nextGen();
				for (int k = 0; k < numGames; k++) {
					gc.nextOpponent();
					runGame(players);
				}

			}

			// run against other players
			Map<RoleInterface<Term>,Player<Term, State<Term, GameState>>> otherPlayers = new HashMap<>();
			for (RoleInterface<Term> role : players.keySet()) {
				if (role.isNature()) { // keep nature player
					otherPlayers.put(role, players.get(role));
				}
				else if (players.get(role) instanceof GAPlayer) {
					GAPlayer<Term, State<Term, GameState>> ga = (GAPlayer<Term, State<Term, GameState>>) players.get(role);
					if (ga.getAgentID() == 0) { // only keep one ga player
						otherPlayers.put(role, players.get(role));
					}
					else { // replace the other roles with other agent to test with
						// random player
						otherPlayers.put(role, PlayerFactory. <Term, State<Term, GameState>> createRandomPlayer(new RandomPlayerInfo(-1, game.getGdlVersion())));
					}
				}
				else {
					otherPlayers.put(role, PlayerFactory. <Term, State<Term, GameState>> createRandomPlayer(new RandomPlayerInfo(-1, game.getGdlVersion())));
				}
			}
			gc.extraAgent("random");
			for (int k = 0; k < numGames; k++) {
				runGame(otherPlayers);
			}
		}

		// +1 for the extra player type
		return numGames * (numGens + 1);
	}

	private Map<RoleInterface<Term>,Player<Term, State<Term, GameState>>> createPlayers(Game<Term, GameState> game) {
		Map<RoleInterface<Term>,Player<Term, State<Term, GameState>>> players=new HashMap<RoleInterface<Term>,Player<Term, State<Term, GameState>>>();
		for (PlayerInfo playerInfo : playerInfos){
			RoleInterface<Term> role = game.getRole(playerInfo.getRoleindex());
			players.put(role, PlayerFactory. <Term, State<Term, GameState>> createPlayer(playerInfo, null));

			Player<Term, State<Term, GameState>> player = players.get(role);
			if (player instanceof GAPlayer){
				GAPlayer<Term, State<Term, GameState>> ga = (GAPlayer<Term, State<Term, GameState>>) player;
				ga.training(game, true);
			}
		}

		

		/*
		// make sure that we have a player for each role (fill up with random players)
		for (int i = 0; i < game.getNumberOfRoles(); i++){
			if (!players.containsKey(game.getRole(i))){
				players.put(game.getRole(i), PlayerFactory. <Term, State<Term, GameState>> createRandomPlayer(new RandomPlayerInfo(i, game.getGdlVersion())));
			}
		}
		*/

		return players;
	}



	private Object getSeesTermsForRole(RoleInterface<Term> role,
			Player<Term, State<Term, GameState>> player,
			State<Term, GameState> priorState,
			JointMoveInterface<Term> priormoves) {
		/*
		 * Here is the only point at which the difference between regular GDL and GDL-II is made:
		 * - if we play a regular GDL game, we will send the moves as seesTerms;
		 * - and if on the contrary we play a GDL-II game, we will derive the seesTerms from the game description, and send them. 
		 */
		Object seesTerms = null;
		if (priormoves != null) { // not the first play message
			if ( player.getGdlVersion() == GDLVersion.v1) { // GDL-I
				seesTerms = priormoves;
			} else { // GDL-II
				// retrieve seesTerms, and send them in the PLAY/STOP messages
				seesTerms = priorState.getSeesTerms(role, priormoves);
			}
		}
		return seesTerms;
	}

	public void cleanup(){

	}

	public Game<Term, GameState> getGame() {
		return game;
	}

	public void setMatchID(String matchID) {
		this.matchID = matchID;
	}

	public void setStartclock(int startClock) {
		this.startClock = startClock;
	}

	public void setPlayclock(int playClock) {
		this.playClock = playClock;
	}

	public void setPlayerInfos(Collection<PlayerInfo> playerInfos) {
		this.playerInfos = playerInfos;
	}

	public Logger getLogger() {
		return null;
	}
}
