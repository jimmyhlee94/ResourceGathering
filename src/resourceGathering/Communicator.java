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
	public boolean isEmitting;
	public boolean isReceiving;
	
	public ArrayList<Message> receivedMessages;
	public Message broadcastingMessage;
	
	public Communicator(int range) {
		this.range = range;
		this.isEmitting = false;
		this.isReceiving = false;
		this.receivedMessages = new ArrayList<Message>();
	}
	
	public void emit(GridPoint currentLocation, GridPoint resourceLocation, Grid<Object> grid, int resourceValue, int resourceSize, int handlersNeeded) {
		isEmitting = true;
		broadcastingMessage = new Message(resourceLocation, resourceValue, resourceSize, handlersNeeded);
	}
	
	public void stopEmitting() {
		this.isEmitting = false;
		this.broadcastingMessage = null;
	}
	
	
	public void receive(GridPoint currentLocation, Grid<Object> grid) {
		
		this.isReceiving = false;
		
		GridCellNgh<Robot> nghCreator = new GridCellNgh<Robot>(grid, currentLocation, Robot.class, range, range);
		List<GridCell<Robot>> gridCells = nghCreator.getNeighborhood(false);
		
		// for all points in the range
		for(GridCell<Robot> pt : gridCells) {
			// if there is at least one robot in the point
			if(pt.size() > 0) {
				//check if the robot is sending a message
				for(Object obj : pt.items()) {
					if(obj instanceof Robot) {
						if(((Robot) obj).communicator.isEmitting) {
							this.isReceiving = true;
							this.receivedMessages.add(((Robot) obj).communicator.broadcastingMessage);
						}
					}
				}
				
			}
		}
	}
	
	public GridPoint findBestLocation(Grid<Object> grid, GridPoint currentLocation, Utility utility) {
		GridPoint bestLocation = grid.getLocation(this);
		float highestUtility = -1;
		
		for(Message m : receivedMessages) {
			float currentUtility = utility.UtilityOfProximityToOthers(m.resourceValue, m.resourceSize, m.handlersNeeded, calculateDistance(currentLocation,m.location), grid.getDimensions().getHeight());
			
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
