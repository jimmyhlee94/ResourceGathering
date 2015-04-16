package resourceGathering;

import java.util.List;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
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
	
	public boolean getSensesFuel(){
		return this.sensesFuel;
	}
	
	public boolean getIsAdjacent(){
		return this.isAdjacent;
	}
	
	public int getRange(){
		return this.range;
	}
	
	public GridPoint getLocation(){
		return this.location;
	}
	
	public float getDistance(){
		return this.distance;
	}
	
	
	public void detectClosestFuel(GridPoint currentPoint, Grid<Object> grid) {
				
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
		
		GridPoint closestPoint = null;
		float smallestDistance = Float.MAX_VALUE;
		
		// for each point in the range
		for(GridCell<Resource> pt : gridCells) {
			// if there is at least one resource in the point
			if(pt.size() > 0)
			{
				// and the resource isn't being carried
				if(!pt.items().iterator().next().isBeingCarried) {
					
					//let the robot know that it senses fuel
					sensesFuel = true;
					
					float currentDistance = (float) Math.sqrt(
				            Math.pow(currentPoint.getX() - pt.getPoint().getX(), 2) +
				            Math.pow(currentPoint.getY() - pt.getPoint().getY(), 2) );
					
					if(currentDistance < smallestDistance) {
						smallestDistance = currentDistance;
						closestPoint = pt.getPoint();
					}
						
					location = closestPoint;
					distance = smallestDistance;
					
					//check if the robot is right next to fuel
					if(distance <= Math.sqrt(2)) {
						isAdjacent = true;
					}
				}
			}
		}
		//there is an idle resource.
		return;
	}
	
	public void detectAnyFuel(GridPoint currentPoint, Grid<Object> grid) {
				
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		
		GridCellNgh<Resource> nghCreator = new GridCellNgh<Resource>(grid, currentPoint, Resource.class, range, range);
		List<GridCell<Resource>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
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
					
					//let the robot know that it senses fuel
					sensesFuel = true;
					
					float currentDistance = (float) Math.sqrt(
				            Math.pow(currentPoint.getX() - pt.getPoint().getX(), 2) +
				            Math.pow(currentPoint.getY() - pt.getPoint().getY(), 2) );
						
					location = pt.getPoint();
					distance = currentDistance;
					
					//check if the robot is right next to fuel
					if(distance <= Math.sqrt(2)) {
						isAdjacent = true;
					}
				}
			}
		}
		//there is an idle resource.
		return;
	}
}
