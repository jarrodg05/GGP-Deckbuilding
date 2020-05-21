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

package tud.gamecontroller.gui;

import javax.swing.table.AbstractTableModel;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.game.impl.Game;
import tud.gamecontroller.players.LegalPlayerInfo;
import tud.gamecontroller.players.XXXXPlayer.XXXXPlayerInfo;
import tud.gamecontroller.players.ParserPlayer.ParserPlayerInfo;
import tud.gamecontroller.players.PlayerInfo;
import tud.gamecontroller.players.RandomPlayerInfo;
import tud.gamecontroller.players.RemotePlayerInfo;

public class PlayerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7703631717408478495L;

	private Game<?,?> game;
	private PlayerRecord[] rows;

	public PlayerTableModel(Game<?,?> game) {
		super();
		this.game=game;
		this.rows=new PlayerRecord[getRowCount()];
		for(int i=0;i<rows.length;i++){
			rows[i]=new PlayerRecord(i);
		}
	}

	public int getColumnCount() {
		return 5; // role, type, host, port, value
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
			return String.class;
		case 1:
			return PlayerType.class;
		case 2:
			return String.class;
		case 3:
			return Integer.class;
		case 4:
			return Integer.class;
		}
		return null;
	}

	public String getColumnName(int columnIndex) {
		switch(columnIndex){
		case 0:
			return "Role";
		case 1:
			return "Type";
		case 2:
			return "Host";
		case 3:
			return "Port";
		case 4:
			return "Value";
		}
		return null;
	}

	public int getRowCount() {
		return game.getNumberOfRoles(); 
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
			return game.getRole(rowIndex).getKIFForm();
		case 1:
			return rows[rowIndex].getType().toString();
		case 2:
			return (rows[rowIndex].getType().equals(PlayerType.REMOTE)?rows[rowIndex].getHost():"-");
		case 3:
			return (rows[rowIndex].getType().equals(PlayerType.REMOTE)?rows[rowIndex].getPort():0);
		case 4:
			return rows[rowIndex].getValue();
		}
		return null;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:return false;
		case 1:return true;
		case 2:return rows[rowIndex].getType().equals(PlayerType.REMOTE);
		case 3:return rows[rowIndex].getType().equals(PlayerType.REMOTE);
		case 4:return false;
		}
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
			break;
		case 1:
			rows[rowIndex].setType((PlayerType)aValue);
			break;
		case 2:
			rows[rowIndex].setHost((String)aValue);
			break;
		case 3:
			rows[rowIndex].setPort(((Integer)aValue).intValue());
			break;
		case 4:
			rows[rowIndex].setValue(((Integer)aValue).intValue());
			break;
		}
		super.setValueAt(aValue, rowIndex, columnIndex);
	}
	
	public PlayerRecord[] getPlayerRecords(){
		return rows;
	}

	public class PlayerRecord {
		
		private PlayerType type;
		private String host;
		private int port;
		private int value;
		private int row;

		public PlayerRecord(int row){
			this.type=PlayerType.RANDOM;
			this.host="localhost";
			this.port=4001;
			this.value=-1;
			this.row=row;
		}
		
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
			fireTableCellUpdated(row, 2);
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
			fireTableCellUpdated(row, 3);
		}
		public PlayerType getType() {
			return type;
		}
		public void setType(PlayerType type) {
			this.type = type;
			fireTableRowsUpdated(row, row);
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
			fireTableCellUpdated(row, 4);
		}
		// TODO: what GDLVersion should we have here?
		public PlayerInfo getPlayerInfo(){
			if(type.equals(PlayerType.REMOTE)){
				return new RemotePlayerInfo(row,host+":"+port,host,port,GDLVersion.v1); 
			}else if(type.equals(PlayerType.RANDOM)){
				return new RandomPlayerInfo(row,GDLVersion.v1);
			}
			else if(type.equals(PlayerType.XXXX)){ // ADDED
				return new XXXXPlayerInfo(row,GDLVersion.v1);
			}
			else if(type.equals(PlayerType.MCS)){
				return new XXXXPlayerInfo(row,GDLVersion.v1);
			}
			else if(type.equals(PlayerType.PARSER)){
				return new ParserPlayerInfo(row,GDLVersion.v1);
			}else{
				return new LegalPlayerInfo(row,GDLVersion.v1);
			}
		}
	}
}
