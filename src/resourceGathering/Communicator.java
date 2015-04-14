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
				Message message = new Message(currentLocation, resourceValue, resourceSize, handlersNeeded);
				pt.items().iterator().next().communicator.receive(message);
			}
		}
	}
	
	
	public void receive(Message message) {
		this.isReceiving = true;
		this.receivedMessages.add(message);
	}
}
