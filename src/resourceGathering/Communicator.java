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
	
	public boolean getIsEmmiting(){
		return this.isEmitting;
	}
	
	public boolean getIsReceiving(){
		return this.isReceiving;
	}
	
	//broadcast information about the resource and where it is
	public void emit(GridPoint currentLocation, GridPoint resourceLocation, Grid<Object> grid, int resourceValue, int resourceSize, int handlersNeeded) {
		isEmitting = true;
		broadcastingMessage = new Message(resourceLocation, resourceValue, resourceSize, handlersNeeded);
	}
	
	public void stopEmitting() {
		this.isEmitting = false;
		this.broadcastingMessage = null;
	}
	
	//check for emitting robots within range.
	public void receive(GridPoint currentLocation, GridPoint currentResourceLocation ,Grid<Object> grid) {
		
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
		
		//remove messages that are being sent from robots that are waiting from the same resource that the current robot is waiting on
		if(currentResourceLocation != null) {
			ArrayList<Message> messagesToBeRemoved = new ArrayList<Message>();
			for(Message m : receivedMessages) {
				if(m.location == currentResourceLocation) {
					messagesToBeRemoved.add(m);
				}
			}
			receivedMessages.removeAll(messagesToBeRemoved);
		}
	}
	
	//Using the utility function, figure out which robot should be assisted.
	public GridPoint findBestLocation(Grid<Object> grid, GridPoint currentLocation, Utility utility) {
		GridPoint bestLocation = currentLocation;
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
