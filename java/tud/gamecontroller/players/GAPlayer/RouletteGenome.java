package tud.gamecontroller.players.GAPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RouletteGenome {
	
	//private ArrayList<String> cardOrder;
	private HashMap<String, Integer> cards;
	private float mutateChance = 0.3f;
	private float fitness;
	private Random rand;
	private int numGames;
	
	public RouletteGenome(String[] cards) {
		rand = new Random();
		fitness = 0;
		numGames = 0;

		this.cards = new HashMap<String, Integer>();
		for (String card : cards){
			this.cards.put(card, rand.nextInt(10) + 1); // need positive value
		}
	}

	// copy constructor
	public RouletteGenome(RouletteGenome base) {
		rand = new Random();
		cards = new HashMap<>(base.cards);
		fitness = 0;
		mutateChance = base.mutateChance;
	}

	// performs at least one mutation 
	public void mutate() {
		boolean mutated = false;
		while (!mutated) {

			for (String card : cards.keySet()) {
				if (rand.nextDouble() < mutateChance){
					mutated = true;
					int x = rand.nextInt(2);
					if (x == 0){
						int increase = rand.nextInt(10);
						int value = cards.get(card) + increase;
						cards.replace(card, value);
					}
					else {
						int decrease = rand.nextInt(10);
						int value = cards.get(card) - decrease;
						value = Math.max(1, value); // need to have positive values to be chosen
						cards.replace(card, value);
					}
				}
			}
		}
	}

	public HashMap<String, Integer> getProbabilities() {
		return cards;
	}

	public float getFitness() {
		return fitness;
	}

	public String getCard(String[] available) {
		int sum = 0;
		for (String card : available) {
			sum += cards.get(card);
		}

		int choice = rand.nextInt(sum);
		for (String card : available) {
			choice -= cards.get(card);
			if (choice < 0) {
				return card;
			}
		}

		// shouuldn't reach this
		return available[rand.nextInt(available.length)];
	}

	/*
	public ArrayList<String> getOrder() {
		ArrayList<String> cardOrder = new ArrayList<>();
		
		int total = 0;
		for (int val : cards.values()) {
			total += val;
		}
		return cardOrder;
	}
	*/

	public void updateFitness(int score) {
		float total = fitness * numGames;
		total += score;
		numGames++;
		fitness = total / numGames;
	}

}