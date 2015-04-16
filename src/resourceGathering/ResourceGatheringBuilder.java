package resourceGathering;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class ResourceGatheringBuilder implements ContextBuilder<Object> {

	public Headquarters HQ;
	
	//@Override
	public Context build(Context<Object> context) {
		
		context.setId("ResourceGathering");
		

		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int robotCount = (Integer)params.getValue("robot_count");
		int maxFuelLevel = (Integer)params.getValue("max_fuel_capacity");
		float fuelRate = (Float)params.getValue("Fuel Depletion Rate");		
		int maxSensorRange = (Integer)params.getValue("max_sensor_range");
		int maxCommunicationRange = (Integer)params.getValue("max_communication_range");
		
		int resourceCount = (Integer)params.getValue("resource_count");
		
		//utility params
		float resourceWeight = (Float)params.getValue("resource_weight");
		int resourceProximityBonus = (Integer)params.getValue("resource_proximity_bonus");

		int hqProximityBonus = (Integer)params.getValue("hq_proximity_bonus");
		int fullTankUtility = (Integer)params.getValue("full_tank_utility");
		
		int fieldSize = (Integer)params.getValue("field_size");
		
		ContinuousSpaceFactory spaceFactory = 
				ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space =
				spaceFactory.createContinuousSpace("space", context,
						new RandomCartesianAdder<Object>(),
						new repast.simphony.space.continuous.WrapAroundBorders(),
						fieldSize,fieldSize);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, fieldSize, fieldSize));	

		
		HQ = new Headquarters(space, grid);
		context.add(HQ);
		space.moveTo(HQ, space.getDimensions().getHeight()/2, space.getDimensions().getWidth()/2);
		Random random = new Random();
		for (int i = 0; i < robotCount; i++) {

			Utility utility = new Utility(resourceWeight, resourceProximityBonus,
					hqProximityBonus, fullTankUtility, robotCount);

			final Robot robot = new Robot(space, grid, HQ, maxFuelLevel, fuelRate, maxSensorRange, maxCommunicationRange, i, utility);
			context.add(robot);
			//initialize robots in a circle around the HQ
			space.moveTo(robot, space.getLocation(HQ).getX() + random.nextInt(3) - 1, space.getLocation(HQ).getY() + random.nextInt(3) - 1);
		}
				
		for (int j = 0; j < resourceCount; j++) {
			//Resource with random value between 10-1000, inclusive and size of 1.
			int resourceSize = RandomHelper.nextIntFromTo(1, 5);
			context.add(new Resource(space, grid, RandomHelper.nextIntFromTo(10,1000), resourceSize, j));
		}

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
		
		HQ.initializeHQ();
		
		return context;
	}

}
