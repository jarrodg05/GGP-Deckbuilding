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

public class GenomeCollection {

	private String[] cards;
	private Genome[] generalPool;
	private PriorityQueue<Genome> leadingPool;
	private int lowestLeader;

	private int currentGenomeIndex;
	private int gameCounter;
	private int generation;
	private int totalGames;

	private final int gameLimit = 4;
	private final int numLeaders = 5;

	private String dataFile = "dominion_results.txt";

	private ArrayList<Integer> fitnesses;

	private Random rand;

	public GenomeCollection(CardList cardList) {
		cards = cardList.keys();
		init();
	}

	public ArrayList<String> getOrder() {
		return generalPool[currentGenomeIndex].getOrder();
	}

	public void GameOver(HashMap<RoleInterface<Term>, Integer> scores, RoleInterface<Term> role) {
		int[] playerScores = new int[2];
		for (RoleInterface<Term> r : scores.keySet()){
			if (r == role) {
				playerScores[0] = scores.get(r);
			}
			else if (!r.isNature()){
				playerScores[1] = scores.get(r);
			}
		}
		updateFitness(playerScores);
		totalGames++;

		if (++gameCounter == gameLimit) {
			gameCounter = 0;
			System.out.println(totalGames);
			if (++currentGenomeIndex == generalPool.length) {
				updateLeaders();
				regenerateGenomes();
				currentGenomeIndex = 0;
				//fitnesses.add(lowestLeader);
				printResults();
				generation++;
			}
		}
	}

	public void printResults() {
		try {
			// append to data file
			FileWriter fw = new FileWriter(dataFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			out.printf("Generation %d:%n", generation);
			for (Genome leader : leadingPool) {
				out.print(leader.getFitness());
				out.print(" ");
				out.println(leader.getOrder());
			}
			out.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void init() {
		generalPool = new Genome[25];
		leadingPool = new PriorityQueue<>((x,y) -> x.getFitness() - y.getFitness());
		gameCounter = 0;
		totalGames = 0;
		generation = 0;
		fitnesses = new ArrayList<>();
		rand = new Random();

		for (int i = 0; i < generalPool.length; i++){
			generalPool[i] = new Genome(cards);
		}
		currentGenomeIndex = 0;

		/*
		for (int i = 0; i < numLeaders; i++) {
			Genome genome = new Genome(cards);
			genome.updateFitness(Integer.MIN_VALUE);
			leadingPool.add(genome);
		}
		*/
		lowestLeader = Integer.MIN_VALUE;
	}

	private void updateLeaders() {
		leadingPool.clear();
		for (Genome genome : generalPool) {
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
		Genome[] leaders = leadingPool.toArray(new Genome[0]); 
		for (int i = 0; i < generalPool.length; i++){
			// duplicate and then mutate a random leader genome to insert in general pool
			Genome genome = new Genome(leaders[rand.nextInt(leaders.length)]);
			genome.mutate();
			generalPool[i] = genome;
		}
	}

	// currently assuming either given scores as [player] or [player, opponent]
	private void updateFitness(int[] gameScores) {
		if (gameScores.length == 1) {
			generalPool[currentGenomeIndex].updateFitness(gameScores[0]);
		}
		else if (gameScores.length == 2) {
			generalPool[currentGenomeIndex].updateFitness(gameScores[0] - gameScores[1]);
		}
	}




}