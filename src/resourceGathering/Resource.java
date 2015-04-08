package resourceGathering;

import java.util.ArrayList;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Resource {


	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public int value;
	public int size;
	public ArrayList<Robot> handlers;
	
	public Resource(ContinuousSpace<Object> space, Grid<Object> grid, int value, int size) {
		this.space = space;
		this.grid = grid;
		this.value = value;
		this.size = size;
		this.handlers = new ArrayList<Robot>();
	}
}
