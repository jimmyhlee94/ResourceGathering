package resourceGathering;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Resource {


	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public int value;
	public int size;
	public int id;
	
	public ArrayList<Robot> handlers;
	
	public boolean isBeingCarried;
	
	public Resource(ContinuousSpace<Object> space, Grid<Object> grid, int value, int size, int id) {
		this.space = space;
		this.grid = grid;
		this.value = value;
		this.size = size;
		this.handlers = new ArrayList<Robot>();
		this.id = id;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void updateState() {
		if (handlers.size() >= size) {
			this.isBeingCarried = true;
		} else {
			this.isBeingCarried = false;
		}
	}
	
	//sets all handling robots' payloads to null then clears the handlers list
	public void destroy() {
		for(Robot robot : handlers) {
			robot.setPayload(null);
		}
		handlers.clear();
	}
}
