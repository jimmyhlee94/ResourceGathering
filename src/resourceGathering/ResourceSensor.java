package resourceGathering;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class ResourceSensor {

	public boolean sensesFuel, isAdjacent;
	public int range;
	public GridPoint location;
	public float distance;
	
	public ResourceSensor(int maxRange) {
		this.range = maxRange;
		this.sensesFuel = false;
		this.isAdjacent = false;
		this.location = null;
		this.distance = -1;
	}
	
	public void detectFuel(GridPoint currentPoint, Grid<Object> grid) {
				
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		
		GridCellNgh<Resource> nghCreator = new GridCellNgh<Resource>(grid, currentPoint, Resource.class, range, range);
		List<GridCell<Resource>> gridCells = nghCreator.getNeighborhood(true);
		//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		//reset all values
		location = null;
		sensesFuel = false;
		isAdjacent = false;
		this.distance = -1;
		
		// for each point in the range
		for(GridCell<Resource> pt : gridCells) {
			// if there is at least one resource in the point
			if(pt.size() > 0)
			{
				// and the resource isn't being carried
				if(!pt.items().iterator().next().isBeingCarried) {
					
					//get the location of the point and let the robot know that it senses fuel
					location = pt.getPoint();
					sensesFuel = true;
					
					this.distance = (float) Math.sqrt(
				            Math.pow(currentPoint.getX() - location.getX(), 2) +
				            Math.pow(currentPoint.getY() - location.getY(), 2) );
					
					//check if the robot is right next to the fuel
					int xDiff = Math.abs(currentPoint.getX() - location.getX());
					int yDiff = Math.abs(currentPoint.getY() - location.getY());
					
					if(distance <= Math.sqrt(2)) {
						isAdjacent = true;
					}
					
					//there is an idle resource.
					return;
				}
			}
		}
	}
}
