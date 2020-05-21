/*
    Copyright (C) 2020 Jarrod Greene <jarrodgreene@live.com.au>

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

package tud.gamecontroller.players.ParserPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.game.impl.Move;
import tud.gamecontroller.game.javaprover.Term;
import tud.gamecontroller.term.TermInterface;
import tud.gamecontroller.players.LocalPlayer;
import tud.gamecontroller.game.RunnableMatchInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.ConnectionEstablishedNotifier;


public class ParserPlayer<
	TermType extends TermInterface,
	StateType extends StateInterface<TermType, ? extends StateType>> extends LocalPlayer<TermType, StateType>  {
    
    // replace String with card class
    ArrayList<String> cards;

    HashMap<String, Integer> cardPriority;

	public ParserPlayer(String name, GDLVersion gdlVersion) {
        super(name, gdlVersion);   

    }
    
    /**
	 * Run when the game starts to perform basic set-up
	 */
	@Override
	public void gameStart(RunnableMatchInterface<TermType, StateType> match, RoleInterface<TermType> role, ConnectionEstablishedNotifier notifier) {
        super.gameStart(match, role, notifier);
        String kif = match.getGame().getKIFGameDescription();
        ArrayList<String> terms = new ArrayList<String>();
        for (int i = 0; i < kif.length(); i++){
            if (kif.charAt(i) == '('){
                int open = 1;
                StringBuilder term = new StringBuilder();
                while(true){
                    i++;
                    if (kif.charAt(i) == '('){
                        open++;
                    }
                    else if (kif.charAt(i) == ')'){
                        open--;
                    }
                    if (open == 0){
                        break;
                    } 
                    term.append(kif.charAt(i));
                }
                terms.add(term.toString());
            }
        }

        cards = new ArrayList<String>();
        for (int i = 0; i < terms.size(); i++){
            String term = terms.get(i);
            String[] termArgs = term.split(" ", 2);
            if (termArgs[0].equalsIgnoreCase("isCard")){
                cards.add(termArgs[1]);
            }
        }

        cardPriority = new HashMap<String, Integer>(cards.size()); 
        cardPriority.put("CARD6", 0);
        cardPriority.put("CARD3", 1);
        cardPriority.put("CARD2", 2);
        cardPriority.put("CARD5", 3);
        cardPriority.put("CARD4", 4);
        cardPriority.put("CARD1", 5);

    }

	public MoveInterface<TermType> getNextMove() {
        

        Collection<? extends MoveInterface<TermType>> legalMoves = getLegalMoves();

        Iterator<?> it = legalMoves.iterator();
        Move<TermType> bestBuy = null;
        int bestPriority = Integer.MAX_VALUE;
        while(it.hasNext()){
            Move<TermType> term = (Move<TermType>) it.next();   
            if (term.getName().equalsIgnoreCase("buy")){
                List<Term> args = (List<Term>) term.getArgs();    
                for (Term card : args){
                    if (cards.contains(card.getName())){
                       int priority = cardPriority.get(card.getName());
                       if (priority < bestPriority){
                           bestPriority = priority;
                           bestBuy = term;
                       }
                       
                       
                    }
                } 
            }                
        }

        // buy a card if able
        if (bestBuy != null){
            return bestBuy;
        }

        // otherwise do something else (which will be endturn or noop)
		return legalMoves.iterator().next();
	}
}
