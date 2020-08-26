/*
    Copyright (C) 2008 Stephan Schiffel <stephan.schiffel@gmx.de>

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

package tud.gamecontroller.players.GAPlayer;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.players.LocalPlayerInfo;

public class GAPlayerInfo extends LocalPlayerInfo {

	public GAPlayerInfo(int roleindex, GDLVersion gdlVersion) {
		super(roleindex, "GA", GDLVersion.v1); // If it will only work for GDL-I games
	}

	@Override
	public String getType() {
		return TYPE_GA;
	}

}
