package resourceGathering;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import resourceGathering.Robot.State;

public class Utility {

	//mutliplier for having a resource in possession
	public float resourceWeight;
	
	//Utility of being adjacent to a resource
	public float resourceProximityBonus;
	
	//Utility of being adjacent to HQ
	public float hqProximityBonus;
	
	//Utility of having a full tank
	public float fullTankUtility;
	
	//Utility lost for waiting consecutive ticks
	public float penaltyForWaiting;
	
	//Exponent applied on size of resource
	public float resourceSizeExponent;
	
	public int pursueBias;
	public int carryBias;
	public int waitBias;
	public int assistBias;
	
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
		resourceSizeExponent = (Float)params.getValue("resource_size_exponent");
		
		
		pursueBias = (Integer)params.getValue("u_pursue_bias");
		carryBias = (Integer)params.getValue("u_carry_bias");
		waitBias = (Integer)params.getValue("u_wait_bias");
		assistBias = (Integer)params.getValue("u_assist_bias");
	}
	
	//account for any manual biasing of states
	public float UtilityOfState(State state) {

		switch(state) {
		case PURSUIT:
			return pursueBias;

		case ASSIST:
			return assistBias;

		case CARRY:
			return carryBias;
		
		case WAIT:
			return waitBias;
		default:
			return 0;
		}
	}
	
	//utility of being having a resource in possession.
	public float UtilityOfResourceInPossession(int value, int size, int handlersNeeded) {
		float utility = resourceWeight * (1-handlersNeeded/numTotalRobots) * value/size;
		return utility;
	}
	
	//utility of being closer to a resource
	public float UtilityOfDistanceToResource(float distance, int mapSize) {
		float utility = resourceProximityBonus * (1-(distance/mapSize));
		return utility;
	}
	
	//utility of moving closer to robots in need of help
	public float UtilityOfProximityToOthers(int value, int size, int handlersNeeded, float distance, int mapSize) {
		float utility = (float) (value * Math.pow(size, resourceSizeExponent) * (1-(handlersNeeded/numTotalRobots)) * (1-(distance/mapSize)));
		return utility;
		
	}
	
	//utility of being closer to HQ
	public float UtilityOfProximityToHQ(float distance, int mapSize) {
		float utility = hqProximityBonus* (1-(distance/mapSize));
		return utility;
	}
	
	//utilty of having a certain amount of fuel left in the tank.
	public float UtilityOfFuelLevel(float currentFuelLevel, int maxFuelCapacity) {
		float utility = fullTankUtility * (currentFuelLevel/maxFuelCapacity);
		return utility;
	}
	
	//utility lost from waiting for consecutive ticks
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
