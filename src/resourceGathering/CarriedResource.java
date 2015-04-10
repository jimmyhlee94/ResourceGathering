package resourceGathering;

import java.util.ArrayList;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class CarriedResource {

	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public int value;
	public int size;
	public ArrayList<Robot> handlers;
	
	public CarriedResource(ContinuousSpace<Object> space, Grid<Object> grid, int value, int size) {
		this.space = space;
		this.grid = grid;
		this.value = value;
		this.size = size;
		this.handlers = new ArrayList<Robot>();
	}
}
