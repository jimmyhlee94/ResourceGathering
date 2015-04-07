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

	public boolean sensesFuel;
	public int range;
	public GridPoint location;
	
	
	public ResourceSensor(int maxRange)
	{
		this.range = maxRange;
		this.sensesFuel = false;
		this.location = null;
	}
	
	public void detectFuel(GridPoint currentPoint, Grid<Object> grid) {
				
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		
		GridCellNgh<Resource> nghCreator = new GridCellNgh<Resource>(grid, currentPoint, Resource.class, range, range);
		List<GridCell<Resource>> gridCells = nghCreator.getNeighborhood(true);
		//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		location = null;
		sensesFuel = false;
		
		for(GridCell<Resource> pt : gridCells) {
			if(pt.size() > 0)
			{
				location = pt.getPoint();
				sensesFuel = true;
				return;
			}
		}

	}
}
