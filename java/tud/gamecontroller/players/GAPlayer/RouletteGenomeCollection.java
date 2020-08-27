package tud.gamecontroller.players.GAPlayer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.javaprover.Term;

public class RouletteGenomeCollection {

	private String[] cards;
	private RouletteGenome[] generalPool;
	private PriorityQueue<RouletteGenome> leadingPool;
	private float lowestLeader;

	private ArrayList<Integer> currentGenomeIndices;
	private int gameCounter;
	private int generation;
	private int totalGames;
	private int numAgents;

	private final int gameLimit = 5;
	private final int numLeaders = 5;

	private String dataFile = "roulette_sushi.txt";

	private Random rand;

	public static RouletteGenomeCollection instance;

	public RouletteGenomeCollection() {
		instance = this;
		numAgents = 0;
	}

	// inits and returns an id to be used for future interactions
	public int init(CardList cardList) {
		if (numAgents == 0) {
			cards = cardList.keys();
			init();
		}

		// pick random starting point for genomes that isnt already taken
		int index;
		do {
			index = rand.nextInt(generalPool.length);
		} while (currentGenomeIndices.contains(index));
		currentGenomeIndices.add(index);

		return numAgents++;
	}

	public String getCard(String[] available, int agentID) {
		int index = currentGenomeIndices.get(agentID);
		return generalPool[index].getCard(available);
	}

	public void GameOver(HashMap<RoleInterface<Term>, Integer> scores, RoleInterface<Term> role, int agentID) {
		int[] playerScores = new int[2];
		for (RoleInterface<Term> r : scores.keySet()){
			if (r == role) {
				playerScores[0] = scores.get(r);
			}
			else if (!r.isNature()){
				playerScores[1] = scores.get(r);
			}
		}
		updateFitness(playerScores, agentID);


		int index = currentGenomeIndices.get(agentID);
		index = (index + 1) % generalPool.length;
		currentGenomeIndices.set(agentID, index);

		if (++totalGames % (generalPool.length * numAgents) == 0) {

			int newIndex;
			do {
				newIndex = rand.nextInt(generalPool.length);
			} while (currentGenomeIndices.contains(newIndex));
			currentGenomeIndices.set(agentID, newIndex);

			if (++gameCounter == gameLimit) {
				updateLeaders();
				regenerateGenomes();
				printResults();
				generation++;
				gameCounter = 0;
			}

		}

		if (totalGames % 10 == 0) {
			System.out.println(totalGames);
		}
	}

	public void printResults() {
		try {
			// append to data file
			FileWriter fw = new FileWriter(dataFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			out.printf("Generation %d:%n", generation);
			for (RouletteGenome leader : leadingPool) {
				out.print(leader.getFitness());
				out.print(" ");
				HashMap<String, Integer> probabilities = leader.getProbabilities();
				for (String card : probabilities.keySet()) {
					out.print(card);
					out.print(":");
					out.print(probabilities.get(card));
					out.print(" ");
				}
				out.println();
			}
			out.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void init() {
		generalPool = new RouletteGenome[50];
		leadingPool = new PriorityQueue<>((x,y) -> Float.compare(x.getFitness(), y.getFitness()));
		gameCounter = 0;
		totalGames = 0;
		generation = 0;
		rand = new Random(); 

		for (int i = 0; i < generalPool.length; i++){
			generalPool[i] = new RouletteGenome(cards);
		}
		currentGenomeIndices = new ArrayList<Integer>();
		lowestLeader = Integer.MIN_VALUE;
	}

	private void updateLeaders() {
		leadingPool.clear();
		for (RouletteGenome genome : generalPool) {
			if (leadingPool.size() < numLeaders) {
				leadingPool.add(genome);
			}
			else if (genome.getFitness() > lowestLeader) {
				leadingPool.poll();
				leadingPool.add(genome);
				lowestLeader = leadingPool.peek().getFitness();
			}
		}
	}

	private void regenerateGenomes() {
		RouletteGenome[] leaders = leadingPool.toArray(new RouletteGenome[0]); 
		for (int i = 0; i < generalPool.length; i++){
			// duplicate and then mutate a random leader genome to insert in general pool
			RouletteGenome genome = new RouletteGenome(leaders[rand.nextInt(leaders.length)]);
			genome.mutate();
			generalPool[i] = genome;
		}
	}

	// currently assuming either given scores as [player] or [player, opponent]
	private void updateFitness(int[] gameScores, int agentID) {
		int index = currentGenomeIndices.get(agentID);
		if (gameScores.length == 1) {
			generalPool[index].updateFitness(gameScores[0]);
		}
		else if (gameScores.length == 2) {
			generalPool[index].updateFitness(gameScores[0] - gameScores[1]);
		}
	}




}