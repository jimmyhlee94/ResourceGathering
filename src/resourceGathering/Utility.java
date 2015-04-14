package resourceGathering;

public class Utility {

	//mutliplier for having a resource in possession
	public float resourceWeight;
	
	//Utility of being adjacent to a resource
	public float resourceProximityBonus;
	
	//Utility of being adjacent to HQ
	public float hqProximityBonus;
	
	//Utility of having a full tank
	public float fullTankUtility;
	
	public Utility(float resourceWeight, float resourceProximityBonus, 
			 float hqProximityBonus, float fullTankUtility) {
		this.resourceWeight = resourceWeight;
		this.resourceProximityBonus = resourceProximityBonus;
		this.hqProximityBonus = hqProximityBonus;
		this.fullTankUtility = fullTankUtility;
	}
	
	public float UtilityOfResourceInPossession(int value, int size, int handlersNeeded, int numTotalRobots) {
		float utility = resourceWeight * (1-handlersNeeded/numTotalRobots) * value/size;
		return utility;
	}
	
	public float UtilityOfDistanceToResource(float distance, int mapSize) {
		float utility = resourceProximityBonus * (1-(distance/mapSize));
		return utility;
	}
	
	public float UtilityOfProximityToOthers(int value, int size, int handlersNeeded, float distance, int numTotalRobots, int mapSize) {
		float utility = (value/size) * (1-(handlersNeeded/numTotalRobots)) * (1-(distance/mapSize));
		return utility;
		
	}
	
	public float UtilityOfProximityToHQ(float distance, int mapSize) {
		float utility = hqProximityBonus* (1-(distance/mapSize));
		return utility;
	}
	
	public float UtilityOfFuelLevel(int currentFuelLevel, int maxFuelCapacity) {
		float utility = fullTankUtility * (currentFuelLevel/maxFuelCapacity);
		return utility;
	}
}
