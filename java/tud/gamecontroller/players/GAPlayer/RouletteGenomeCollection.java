package tud.gamecontroller.players.GAPlayer;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.javaprover.Term;
import tud.gamecontroller.testing.GARunner;

public class RouletteGenomeCollection {

	private String[] cards;
	private RouletteGenome[] generalPool;
	private PriorityQueue<RouletteGenome> leadingPool;
	private float lowestLeader;
	private HashMap<Integer, RouletteGenome[]> prevLeaders;

	private ArrayList<Integer> currentGenomeIndices;
	private int gameCounter;
	private int generation;
	private int totalGames;
	private int numAgents;

	private boolean scoring = false;
	private Iterator<RouletteGenome> scoringAgentIter;
	private RouletteGenome scoringAgent;
	private RouletteGenome benchmarkAgent;
	private HashMap<RouletteGenome, HashMap<Integer, Float>> benchmarkScores;
	private int benchmarkGen;
	private Iterator<Integer> benchmarkGenIter;
	private HashMap<Integer, String> extraAgents;

	private final int gameLimit = 5;
	private final int numLeaders = 5;
	private final int numGenomes = 50;

	private String dataFile = "7wondersRoulette.txt";
	private String saveFile = "testSave";

	private Random rand;

	public static RouletteGenomeCollection instance;
	private GARunner gaRunner;

	public RouletteGenomeCollection(GARunner gaRunner) {
		instance = this;
		numAgents = 0;
		this.gaRunner = gaRunner;

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
		if (!scoring){
			int index = currentGenomeIndices.get(agentID);
			return generalPool[index].getCard(available);
		}
		else if (agentID == 1) {
			return benchmarkAgent.getCard(available);
		}
		else {
			return scoringAgent.getCard(available);
		}
	}

	public void GameOver(HashMap<RoleInterface<Term>, Integer> scores, RoleInterface<Term> role, int agentID) {
		int[] playerScores = new int[scores.size()];
		int opponent = 1;
		for (RoleInterface<Term> r : scores.keySet()){
			if (r == role) {
				playerScores[0] = scores.get(r);
			}
			else if (!r.isNature()){
				playerScores[opponent] = scores.get(r);
				opponent++;
			}
		}

		if (!scoring){
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
					if (generation % 5 == 0) {
						// keep leaders for benchmarking later agents against 
						prevLeaders.put(generation, leadingPool.toArray(new RouletteGenome[0]));
					}
					printResults();
					save(saveFile);
					generation++;
					gameCounter = 0;
				}
			}

			if (totalGames % 10 == 0) {
				System.out.println(totalGames);
			}
		}
		else if (agentID == 0) {
			addScore(playerScores);
		}
	}

	private int runScoringSims() {
		scoring = true;
		int numGames = 5;
		int numGens = prevLeaders.size();
		int numLeaders = leadingPool.size();
		scoringAgentIter = leadingPool.iterator();

		benchmarkScores = new HashMap<>();
		for (RouletteGenome agent : leadingPool) {
			benchmarkScores.put(agent, new HashMap<>());
		}
		benchmarkGenIter = prevLeaders.keySet().iterator();
		extraAgents = new HashMap<>();

		int games = gaRunner.runSims(numGames, numGens, numLeaders);
		scoring = false;
		return numGames;
	}

	// add to total score against current generation - divided by num games when printing
	private void addScore(int[] playerScores) {
		float score = playerScores[0] - playerScores[1];
		benchmarkScores.get(scoringAgent).merge(benchmarkGen, score, Float::sum);
	}


	public void nextOpponent() {
		int newAgent = rand.nextInt(prevLeaders.get(benchmarkGen).length);
		benchmarkAgent = prevLeaders.get(benchmarkGen)[newAgent];
	}

	public void nextGen() {
		benchmarkGen = benchmarkGenIter.next();
	}

	public void nextAgent() {
		scoringAgent = scoringAgentIter.next();
		benchmarkGenIter = prevLeaders.keySet().iterator();
	}

	public void extraAgent(String name) {
		if (benchmarkGen >= 0) {
			benchmarkGen = -1;
		}
		else {
			benchmarkGen--;
		}
		extraAgents.putIfAbsent(benchmarkGen, name);
	}

	public void printResults() {
		int numGames = runScoringSims();
		try {
			// append to data file
			FileWriter fw = new FileWriter(dataFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			out.printf("Generation %d:%n", generation);
			for (RouletteGenome agent : leadingPool) {
				// print in-pop fitness and genome info
				out.print(agent.getFitness());
				out.print(" ");
				HashMap<String, Integer> probabilities = agent.getProbabilities();
				for (String card : probabilities.keySet()) {
					out.print(card);
					out.print(":");
					out.print(probabilities.get(card));
					out.print(" ");
				}
				out.println();

				// print out scoring against previous generation agents
				for (int prevGen : benchmarkScores.get(agent).keySet()) {
					if (prevGen < 0) {
						//System.out.print("gen" + extraAgents.get(prevGen));
						out.print(extraAgents.get(prevGen)); // print out agent type 
					}
					else {
						out.print(prevGen); // the generation being compared to
					}
					out.print(":");
					out.print(benchmarkScores.get(agent).get(prevGen) / numGames); // divide to get average score per game
					out.println();
				}

			}
			out.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void init() {
		generalPool = new RouletteGenome[numGenomes];
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

		prevLeaders = new HashMap<>();
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
		else { // fitness as player score minus the highest opponent score 
			int highest = 0;
			for (int i = 1; i < gameScores.length; i++) {
				if (gameScores[i] > highest) {
					highest = gameScores[i];
				}
			}
			generalPool[index].updateFitness(highest);
		}
	}


	private void save(String filepath) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filepath);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(leadingPool.toArray());
			objectOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void load(String filepath) {
		try {
			FileInputStream fileIn = new FileInputStream(filepath);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			try {
				RouletteGenome[] leaders = (RouletteGenome[]) objectIn.readObject();
				leadingPool.clear();
				leadingPool.addAll(Arrays.asList(leaders));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			objectIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}