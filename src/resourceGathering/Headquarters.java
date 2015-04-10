package resourceGathering;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Headquarters {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int fuelStore;
	
	private GridPoint location;
	
	public Headquarters(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	public GridPoint getLocation() {
		return this.location;
	}
	
	public void initializeHQ() {
		this.location = grid.getLocation(this);
	}
	
}
