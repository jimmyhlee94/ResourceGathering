package resourceGathering;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class Utility {

	//mutliplier for having a resource in possession
	public float resourceWeight;
	
	//Utility of being adjacent to a resource
	public float resourceProximityBonus;
	
	//Utility of being adjacent to HQ
	public float hqProximityBonus;
	
	//Utility of having a full tank
	public float fullTankUtility;
	
	public float penaltyForWaiting;
	
	public int numTotalRobots;
	
	public Utility() {
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		numTotalRobots = (Integer)params.getValue("robot_count");

		//utility params
		resourceWeight = (Float)params.getValue("resource_weight");
		resourceProximityBonus = (Integer)params.getValue("resource_proximity_bonus");
		hqProximityBonus = (Integer)params.getValue("hq_proximity_bonus");
		fullTankUtility = (Integer)params.getValue("full_tank_utility");
		penaltyForWaiting = (Integer)params.getValue("penalty_for_waiting");
	}
	
	public float UtilityOfResourceInPossession(int value, int size, int handlersNeeded) {
		float utility = resourceWeight * (1-handlersNeeded/numTotalRobots) * value/size;
		return utility;
	}
	
	public float UtilityOfDistanceToResource(float distance, int mapSize) {
		float utility = resourceProximityBonus * (1-(distance/mapSize));
		return utility;
	}
	
	public float UtilityOfProximityToOthers(int value, int size, int handlersNeeded, float distance, int mapSize) {
		float utility = (value/size) * (1-(handlersNeeded/numTotalRobots)) * (1-(distance/mapSize));
		return utility;
		
	}
	
	public float UtilityOfProximityToHQ(float distance, int mapSize) {
		float utility = hqProximityBonus* (1-(distance/mapSize));
		return utility;
	}
	
	public float UtilityOfFuelLevel(float currentFuelLevel, int maxFuelCapacity) {
		float utility = fullTankUtility * (currentFuelLevel/maxFuelCapacity);
		return utility;
	}
	
	public float UtilityLostFromWaiting(int ticksWaited) {
		float utility = ticksWaited * penaltyForWaiting * -1;
		return utility;
	}

	public float getResourceWeight() {
		return resourceWeight;
	}

	public float getResourceProximityBonus() {
		return resourceProximityBonus;
	}

	public float getHqProximityBonus() {
		return hqProximityBonus;
	}

	public float getFullTankUtility() {
		return fullTankUtility;
	}

	public int getNumTotalRobots() {
		return numTotalRobots;
	}
	
	
	
}
