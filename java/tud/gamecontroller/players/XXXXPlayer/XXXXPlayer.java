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

package tud.gamecontroller.players.XXXXPlayer;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.players.LocalPlayer;
import tud.gamecontroller.term.TermInterface;

import java.util.ArrayList;
import java.util.Random;

public class XXXXPlayer<
	TermType extends TermInterface,
	StateType extends StateInterface<TermType, ? extends StateType>> extends LocalPlayer<TermType, StateType>  {

	private Random random;
	
	public XXXXPlayer(String name, GDLVersion gdlVersion) {
//		super(name, GDLVersion.v1); // If the player will only work for GDL-I games
		super(name, gdlVersion);
		random = new Random();
	}
	
	public MoveInterface<TermType> getNextMove() {
		ArrayList<MoveInterface<TermType>> legalMoves = new ArrayList<MoveInterface<TermType>>(getLegalMoves());
		int i = random.nextInt(legalMoves.size());
		return legalMoves.get(i);
	}
}
