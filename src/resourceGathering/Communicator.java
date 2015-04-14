package resourceGathering;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Communicator {
	
	private int range;
	//public boolean isEmitting;
	public boolean isReceiving;
	
	public ArrayList<Message> receivedMessages;
	public Message broadcastingMessage;
	
	public Communicator(int range) {
		this.range = range;
		this.isReceiving = false;
		this.receivedMessages = new ArrayList<Message>();
	}
	
	public void emit(GridPoint currentLocation, Grid<Object> grid, int resourceValue, int resourceSize, int handlersNeeded) {
		
		GridCellNgh<Robot> nghCreator = new GridCellNgh<Robot>(grid, currentLocation, Robot.class, range, range);
		List<GridCell<Robot>> gridCells = nghCreator.getNeighborhood(false);
		
		// for all points in the range
		for(GridCell<Robot> pt : gridCells) {
			// if there is at least one robot in the point
			if(pt.size() > 0) {
				//send the robot's communicator a message
				broadcastingMessage = new Message(currentLocation, resourceValue, resourceSize, handlersNeeded);
				pt.items().iterator().next().communicator.receive(broadcastingMessage);
			}
		}
	}
	
	
	public void receive(Message message) {
		this.isReceiving = true;
		this.receivedMessages.add(message);
	}
	
	public GridPoint findBestLocation(Grid<Object> grid, GridPoint currentLocation, Utility utility) {
		GridPoint bestLocation = grid.getLocation(this);
		float highestUtility = -1;
		
		for(Message m : receivedMessages) {
			float currentUtility = utility.UtilityOfProximityToOthers(m.resourceValue, m.resourceSize, m.handlersNeeded, calculateDistance(grid.getLocation(this),m.location), grid.getDimensions().getHeight());
			
			if(currentUtility > highestUtility) {
				highestUtility = currentUtility;
				bestLocation = m.location;
			}
			
		}
		return bestLocation;
	}
	
	private float calculateDistance(GridPoint a, GridPoint b) {
		return (float) Math.sqrt(
	            Math.pow(a.getX() - b.getX(), 2) +
	            Math.pow(a.getY() - b.getY(), 2) );
	}
}
