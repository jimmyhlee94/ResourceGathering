package resourceGathering;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Headquarters {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int fuelStore;
	
	private GridPoint location;
	
	public Headquarters(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.fuelStore = 0;
	}
	
	public GridPoint getLocation() {
		return this.location;
	}
	
	public void initializeHQ() {
		this.location = grid.getLocation(this);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void takeFuel() {
		
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		
		GridCellNgh<Resource> nghCreator = new GridCellNgh<Resource>(grid, grid.getLocation(this), Resource.class, 1, 1);
		List<GridCell<Resource>> gridCells = nghCreator.getNeighborhood(true);
		//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		ArrayList<Resource> resourcesToBeRemoved = new ArrayList<Resource>();
		
		for(GridCell<Resource> pt : gridCells) {
			if(pt.size() > 0)
			{
				GridPoint gpt = pt.getPoint();
				
				for(Object obj : grid.getObjectsAt(gpt.getX(), gpt.getY())) {
					if(obj instanceof Resource) {
						((Resource) obj).destroy();
						this.fuelStore += ((Resource) obj).value;
						
						resourcesToBeRemoved.add((Resource)obj);
						
					}
				}
			}
		}
		
		for(Resource resource : resourcesToBeRemoved) {
			Context<Object> context = ContextUtils.getContext(resource);
			context.remove(resource);
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void refuel() {
		
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		
		GridCellNgh<Resource> nghCreator = new GridCellNgh<Resource>(grid, grid.getLocation(this), Resource.class, 1, 1);
		List<GridCell<Resource>> gridCells = nghCreator.getNeighborhood(true);
		//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		for(GridCell<Resource> pt : gridCells) {
			if(pt.size() > 0)
			{
				GridPoint gpt = pt.getPoint();
				
				for(Object obj : grid.getObjectsAt(gpt.getX(), gpt.getY())) {
					if(obj instanceof Robot){
						((Robot) obj).setFuelLevel(  ((Robot) obj).getMaxFuelLevel()   );
					}
				}
			}
		}
		
	}
	
}
