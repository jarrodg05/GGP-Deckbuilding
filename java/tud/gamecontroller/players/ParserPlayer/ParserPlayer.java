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
import java.util.Random;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.GameController;
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
    
    CardList cards;

    Random rand;

	public ParserPlayer(String name, GDLVersion gdlVersion) {
        super(name, gdlVersion);   
        rand = new Random();
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

        cards = new CardList();
        for (int i = 0; i < terms.size(); i++){
            String term = terms.get(i);
            String[] termArgs = term.split(" ", 2);
            if (termArgs[0].equalsIgnoreCase("isCard")){
                cards.add(new Card(termArgs[1]));
            }
        }

        // TODO refactor so we arent looping through multiple times
        for (int i = 0; i < terms.size(); i++){
            String term = terms.get(i);
            String[] termArgs = term.split(" ");
            if (termArgs[0].equalsIgnoreCase("cost")){
                Card card = cards.get(termArgs[1]);
                card.AddCost("money", Integer.parseInt(termArgs[2]));
            }
        }

        // set priority to the cost of the card
        for (Card card: cards){
            card.SetPriority(card.GetTotalCost());
        }

    }

	public MoveInterface<TermType> getNextMove() {
        

        Collection<? extends MoveInterface<TermType>> legalMoves = getLegalMoves();

        Iterator<?> it = legalMoves.iterator();
        ArrayList<Move<TermType>> bestBuys = new ArrayList<Move<TermType>>();
        int bestPriority = -1;
        while(it.hasNext()){
            Move<TermType> term = (Move<TermType>) it.next();   
            if (term.getName().equalsIgnoreCase("buy")){
                List<Term> args = (List<Term>) term.getArgs();    
                for (Term card : args){
                    if (cards.contains(card.getName())){
                       int priority = cards.get(card.getName()).priority;
                       if (priority > bestPriority){
                           bestPriority = priority;
                           bestBuys.clear();
                           bestBuys.add(term);
                       }
                       else if (priority == bestPriority){
                           bestBuys.add(term);
                       }                       
                    }
                } 
            }                
        }



        // otherwise do something else 
        ArrayList<MoveInterface<TermType>> moves = new ArrayList<MoveInterface<TermType>>();
        Iterator<? extends MoveInterface<TermType>> it2 = legalMoves.iterator();
        while(it2.hasNext()){
            moves.add(it2.next());
        }

        ArrayList<MoveInterface<TermType>> playMoves = new ArrayList<MoveInterface<TermType>>();
        for (MoveInterface<TermType> move : moves){
            TermInterface term = move.getTerm();
            if (term.getName().equalsIgnoreCase("playCard")){
                playMoves.add(move);
            }
        }

        // play cards if able
        if (!playMoves.isEmpty()){
            return playMoves.get(rand.nextInt(playMoves.size()));
        }

        // buy a card if able
        // picks the highest priority card, or randomly from equal highest cards
        if (!bestBuys.isEmpty()){
            return bestBuys.get(rand.nextInt(bestBuys.size()));
        }

        // pick a random move
        return moves.get(rand.nextInt(moves.size()));
    }
    
    /*
    @Override
    public void gameStop(Object seesTerms, ConnectionEstablishedNotifier notifier) {
        super.gameStop(seesTerms, notifier);
        
    }
    */
}
