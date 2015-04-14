package resourceGathering;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import resourceGathering.Robot.State;

public class TestRobot {

	private Robot sourceRobot;
	private State testState;
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private ResourceSensor sensor;
	private Communicator communicator;
	private ArrayList<Message> messages;
	
	private int fuelLevel = 0;
	private int fuelRate = 0;
	
	private float distanceToResource = Float.MAX_VALUE;
	private float distanceToHQ = Float.MAX_VALUE;
	
	private Resource payload;
	
	private int payloadResourceValue = 0;
	private int payloadResourceSize = 0;
	private int numPayloadHandlers = 0;
	
	private GridPoint location;
	
	Utility utilityCalculator;
	
	public TestRobot(Robot original, State testState, Utility utilityCalculator) {
		
		this.sourceRobot = original;
		this.testState = testState;
		
		this.grid = sourceRobot.grid;
		this.space = sourceRobot.space;
		
		this.sensor = sourceRobot.sensor;
		this.messages = sourceRobot.communicator.receivedMessages;
		
		this.fuelLevel = sourceRobot.fuelLevel;
		this.fuelRate = sourceRobot.fuelRate;
	
		this.payload = sourceRobot.payload;
		
		this.utilityCalculator = utilityCalculator;
		
		NdPoint spacePt = space.getLocation(sourceRobot);
		Context<Object> context = ContextUtils.getContext(sourceRobot);
		context.add(this);
		space.moveTo(this, spacePt.getX(), spacePt.getY());
		grid.moveTo(this, grid.getLocation(sourceRobot).getX(), grid.getLocation(sourceRobot).getY());
		
		this.location = grid.getLocation(sourceRobot);
		
	}
	
	public void testState() {
		
		if(testState == State.CARRY) {
			
			moveTowards(grid.getLocation(sourceRobot.HQ));
			
			payloadResourceValue = sourceRobot.payload.value;
			payloadResourceSize = sourceRobot.payload.size;
			numPayloadHandlers = sourceRobot.payload.handlers.size();
			
			
			fuelLevel = fuelLevel - fuelRate*4;
			
		} else if (testState == State.PURSUIT) {
			
			moveTowards(sensor.location);
			
		} else if (testState == State.ASSIST) {
			
			//some logic for determining which robot to assist.
			//use the utility function.
			GridPoint bestLocation = grid.getLocation(this);
			float highestUtility = -1;
			
			for(Message m : communicator.receivedMessages) {
				float utility = this.utilityCalculator.UtilityOfProximityToOthers(m.resourceValue, m.resourceSize, m.handlersNeeded, calculateDistance(grid.getLocation(this),m.location), grid.getDimensions().getHeight());
				
				if(utility > highestUtility) {
					highestUtility = utility;
					bestLocation = m.location;
				}
				
			}
			
			moveTowards(bestLocation);
			
		} else if (testState == State.WAIT) {
			
			payloadResourceValue = sourceRobot.payload.value;
			payloadResourceSize = sourceRobot.payload.size;
			numPayloadHandlers = sourceRobot.payload.handlers.size();
			
			fuelLevel = fuelLevel - fuelRate;
		} 
		
		
		updateMetrics();
		
		
		Context<Object> context = ContextUtils.getContext(sourceRobot);
		context.remove(this);
	}
	
	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this,  1,  angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			this.location = grid.getLocation(this);
		}
	}
	
	private void updateMetrics() {
		
		if(sensor.sensesFuel) {
			distanceToResource = calculateDistance(grid.getLocation(this), sensor.location);
		}
		
		//if the robot is still adjacent to the payload, it should still count as a handler. or should it?
		if(distanceToResource <= Math.sqrt(2)) {
			if(payload != null) {
				payloadResourceValue = sourceRobot.payload.value;
				payloadResourceSize = sourceRobot.payload.size;
				numPayloadHandlers = sourceRobot.payload.handlers.size();
			}
		}
		
		//if the resource can't be sensed anymore
		if(distanceToResource > sensor.range) {
			distanceToResource = Float.MAX_VALUE;
		}
		
		this.distanceToHQ = calculateDistance(this, sourceRobot.HQ);
		
	}
	
	private float calculateDistance(GridPoint a, GridPoint b) {
		return (float) Math.sqrt(
	            Math.pow(a.getX() - b.getX(), 2) +
	            Math.pow(a.getY() - b.getY(), 2) );
	}
	
	private float calculateDistance(Object a, Object b) {
		return (float) Math.sqrt(
	            Math.pow(grid.getLocation(a).getX() - grid.getLocation(b).getX(), 2) +
	            Math.pow(grid.getLocation(a).getY() - grid.getLocation(b).getY(), 2) );
	}
	
	public float getUtility() {
		float utility = 0;
		if(sensor.distance > 0) {
			utility += utilityCalculator.UtilityOfDistanceToResource(sensor.distance, grid.getDimensions().getHeight());
		}
		
		if(this.payload != null) {
			utility += utilityCalculator.UtilityOfResourceInPossession(payload.value, payload.size, payload.size-payload.handlers.size());
		}

		if(this.messages.size() > 0) {
			float highestUtility = -1;
			for(Message m : messages) {
				float distanceToRobot =  calculateDistance(location, m.location);
				float currentUtility = utilityCalculator.UtilityOfProximityToOthers(m.resourceValue, m.resourceSize, m.handlersNeeded,
						distanceToRobot, grid.getDimensions().getHeight());
				if(currentUtility > highestUtility) {
					highestUtility = currentUtility;
				}
			}
			utility += highestUtility;
		}
		
		utility += utilityCalculator.UtilityOfProximityToHQ(distanceToHQ, grid.getDimensions().getHeight());

		utility += utilityCalculator.UtilityOfFuelLevel(this.fuelLevel, sourceRobot.maxFuelLevel);
		
		return utility;
	}
	
}
