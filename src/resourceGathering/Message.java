package resourceGathering;

import repast.simphony.space.grid.GridPoint;

public class Message {
	public GridPoint location;
	public int resourceSize;
	public int resourceValue;
	public int handlersNeeded;
	
	public Message(GridPoint location, int resourceValue, int resourceSize, int handlersNeeded) {
		this.location = location;
		this.resourceValue = resourceValue;
		this.resourceSize = resourceSize;
		this.handlersNeeded = handlersNeeded;
	}
}	
