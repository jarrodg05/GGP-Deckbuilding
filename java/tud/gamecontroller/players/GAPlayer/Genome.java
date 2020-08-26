package tud.gamecontroller.players.GAPlayer;

import java.util.ArrayList;
import java.util.Random;

public class Genome {
	
	private ArrayList<String> cardOrder;
	private String[] cards;
	private float mutateChance = 0.1f;
	private int fitness;
	private Random rand;
	private int numGames;
	
	public Genome(String[] cards) {
		this.cards = cards;
		fitness = 0;
		rand = new Random();
		numGames = 0;

		// start with one random card
		cardOrder = new ArrayList<String>();
		cardOrder.add(cards[rand.nextInt(cards.length)]);
	}

	// copy constructor
	public Genome(Genome base) {
		rand = new Random();
		cards = base.cards;
		fitness = 0;
		mutateChance = base.mutateChance;
		cardOrder = new ArrayList<String>(base.cardOrder);
	}

	// performs at least one mutation 
	public void mutate() {
		boolean mutated = false;
		while (!mutated) {
			int i = 0;
			while (i < cardOrder.size()) {
				if (rand.nextDouble() < mutateChance){
					mutated = true;
					int x = rand.nextInt(3);
					if (x == 0){
						mutateAdd(i);
					}
					else if (x == 1){
						mutateSwap(i);
					}
					else {
						if (cardOrder.size() > 1) {
							mutateOrder(i);
						}
						else {
							mutateAdd(i);
						}
					}
				}
				i++;
			}
		}
	}

	private void mutateAdd(int index) {
		String card = null;
		int counter = 0;
		do {
			if (counter++ > 10) return;

			int x = rand.nextInt(cards.length);
			card = cards[x];
		} while (cardOrder.contains(card));

		if (card != null) {
			cardOrder.add(index, card);
		}
	}

	private void mutateSwap(int index) {
		cardOrder.remove(index);
		mutateAdd(index);
	}

	private void mutateOrder(int index) {
		int x;
		do {
			x = rand.nextInt(cardOrder.size());
		} while (x == index);

		cardOrder.add(x, cardOrder.get(index));
		String card = cardOrder.remove(x+1);
		cardOrder.add(index, card);
		cardOrder.remove(index+1);
	}

	public Genome cross(Genome other) {
		return this;
	}

	public int getFitness() {
		return fitness;
	}

	public ArrayList<String> getOrder() {
		return cardOrder;
	}

	public void updateFitness(int score) {
		int total = fitness * numGames;
		total += score;
		numGames++;
		fitness = total / numGames;
	}


	
}