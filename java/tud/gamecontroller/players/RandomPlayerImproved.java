/*
    Copyright (C) 2008-2010 Stephan Schiffel <stephan.schiffel@gmx.de>
                  2010 Nicolas JEAN <njean42@gmail.com>

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

package tud.gamecontroller.players;

import java.util.ArrayList;
import java.util.Random;

import cs227b.teamIago.resolver.Expression;
import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.auxiliary.InvalidKIFException;
import tud.gamecontroller.game.FluentInterface;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.game.impl.Fluent;
import tud.gamecontroller.game.impl.Move;
import tud.gamecontroller.game.javaprover.Term;
import tud.gamecontroller.term.TermInterface;
import tud.gamecontroller.game.javaprover.ParserAdapter;

public class RandomPlayerImproved<
	TermType extends TermInterface,
	StateType extends StateInterface<TermType, ? extends StateType>> extends LocalPlayer<TermType, StateType>  {

	private Random random;
	
	public RandomPlayerImproved(String name, GDLVersion gdlVersion) {
		super(name, gdlVersion);
		random=new Random();
	}
	
	public MoveInterface<TermType> getNextMove() {
		for (FluentInterface f : currentState.getFluents()) {
			String[] s = f.toString().split(" ");
			if (s[0].equals("(DRAWINGCARDS")) {
				String player = s[1];
				int numCards = Integer.parseInt(s[2]);
				int deckSize = Integer.parseInt(s[3]);

				//check decksize bigger than numcards
				if (deckSize < numCards) {
					break;
				}

				int[] cards = randomSample(deckSize, numCards);

				StringBuilder kif = new StringBuilder("(DRAWCARDS");
				kif.append(" " + player);
				for (int card : cards) {
					kif.append(" ");
					kif.append(card);
				}
				kif.append(")");

				try {
					Expression expression = ParserAdapter.parseExpression(kif.toString());
					Term term = new Term(expression);
					return (MoveInterface<TermType>) new Move<Term>(term);
				} catch(InvalidKIFException e) {
					System.out.println("broken move");
				}

			}
		}


		// does the work of getting either legal moves the GDL-I way, or the GDL-II way
		ArrayList<MoveInterface<TermType>> legalMoves = new ArrayList<MoveInterface<TermType>>(getLegalMoves());
		int i=random.nextInt(legalMoves.size());
		//System.out.println(legalMoves.get(i).getKIFForm());
		return legalMoves.get(i);
	}

	private int[] randomSample(int end, int count) {
		int[] result = new int[count];
		int cur = 0;
		int remaining = end;
		for (int i = 0; i < end && count > 0; i++) {
			double probability = random.nextDouble();
			if (probability < ((double) count) / (double) remaining) {
				count--;
				result[cur++] = i;
			}
			remaining--;
		}
		return result;
	}
}
