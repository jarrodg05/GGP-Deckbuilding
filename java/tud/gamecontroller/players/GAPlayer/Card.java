package tud.gamecontroller.players.GAPlayer;

import java.util.HashMap;


public class Card {
	String name;
	HashMap<String, Integer> cost;	
	HashMap<String, Integer> weights;
	int priority;
	
	public Card(String name){
		this.name = name;
		cost = new HashMap<String, Integer>();
		weights = new HashMap<String, Integer>();
	}

	public void AddCost(String resource, int amount){
		cost.put(resource, amount);
		weights.put(resource, 1);
	}

	public int GetTotalCost(){
		int totalCost = 0;
		for (String resource : cost.keySet()){
			totalCost += weights.get(resource) * cost.get(resource);
		}
		return totalCost;
	}

	public void SetWeight(String resource, int weight){
		weights.put(resource, weight);
	}

	public int GetWeight(String resource){
		return weights.get(resource);
	}

	public void SetPriority(int priority){
		this.priority = priority;
	}

	public int GetPriority(){
		return priority;
	}

}