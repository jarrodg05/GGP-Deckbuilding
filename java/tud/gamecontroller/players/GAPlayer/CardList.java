package tud.gamecontroller.players.GAPlayer;

import java.util.HashMap;
import java.util.Iterator;

public class CardList implements Iterable<Card>{

	HashMap<String, Card> cards;

	public CardList(){
		cards = new HashMap<String, Card>();
	}

	public void add(Card card){
		cards.put(card.name, card);
	}

	public Card get(String cardName){
		return cards.get(cardName);
	}

	public boolean contains(String cardName){
		return cards.containsKey(cardName);
	}

	public @Override Iterator<Card> iterator() {
		return cards.values().iterator();
	}

	public String[] keys(){
		return cards.keySet().toArray(new String[0]);
	}
}