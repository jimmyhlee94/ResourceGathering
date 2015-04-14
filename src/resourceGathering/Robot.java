package resourceGathering;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import resourceGathering.Message;

public class Robot {

	public ContinuousSpace<Object> space;
	public Grid<Object> grid;
	
	private int id;
	
	public int maxFuelLevel, fuelLevel;
	public int fuelRate;
	
	private boolean adequateFuel, sensesFuel, receivingBroadcast, canCarry, isAdjacent;
	
	public ResourceSensor sensor;
	public Communicator communicator;
	
	public Resource payload;
	
	private State currentState;
	
	public Headquarters HQ;
	
	private GridPoint hqLocation;
	
	int maxSensorRange;
	int maxCommunicationRange;
	
	Utility utility;
	int numTotalRobots;
	
	public Robot(ContinuousSpace<Object> space, Grid<Object> grid, Headquarters HQ, int maxFuelLevel, int fuelRate,
			int maxSensorRange, int maxCommunicationRange, int id, Utility utility, int numTotalRobots) {
		this.space = space;
		this.grid = grid;
		
		this.id = id;
		
		//this.hqLocation = hqLocation;
		this.HQ = HQ;
		
		this.maxFuelLevel = maxFuelLevel;
		this.fuelLevel = maxFuelLevel;
		this.fuelRate = fuelRate;
		
		this.maxSensorRange = maxSensorRange;
		this.maxCommunicationRange = maxCommunicationRange;
		
		this.utility = utility;
		this.numTotalRobots = numTotalRobots;
		
		this.sensor = new ResourceSensor(maxSensorRange);
		
		this.communicator = new Communicator(maxCommunicationRange);
		
		this.payload = null;
		
		this.adequateFuel = true;
		this.sensesFuel = false;
		this.receivingBroadcast = false;
		this.canCarry = false;
		this.isAdjacent = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		System.out.println("ID: " + id);
		currentState = determineState();
		
		switch(this.currentState) {
		case RANDOM:
			random();
			break;
		case PURSUIT:
			pursue();
			break;
		case ASSIST:
			assist();
			break;
		case CARRY:
			carry();
			break;
		case WAIT:
			waitForAssistance();
			break;
		case REFUEL:
			refuel();
			break;
		}
		System.out.println("");
	}
	
	public State determineState() {
		
		this.adequateFuel = false;
		this.sensesFuel = false;
		this.receivingBroadcast = false;
		this.canCarry = false;
		this.isAdjacent = false;
		
		//preliminary actions
		sensor.detectFuel(grid.getLocation(this), grid);
	
		//set all booleans
		//fuel level?
		this.adequateFuel = true;
		
		if(sensor.sensesFuel) {
			this.sensesFuel = true;
		}
		if(communicator.isReceiving) {
			this.receivingBroadcast = true;
		}
		if(payload != null) {
			if(payload.handlers.size() >= payload.size) {
				this.canCarry = true;
			}
		}
		if(sensor.isAdjacent){
			this.isAdjacent = true;
		}
		
		//output state
		
		if(!adequateFuel) {
			return State.REFUEL;
		}
		
		if(adequateFuel && !sensesFuel && !receivingBroadcast && !canCarry && !isAdjacent) {
			return State.RANDOM;
		}
		
		ArrayList<State> possibleStates = new ArrayList<State>();
		
		if(adequateFuel && sensesFuel && canCarry && isAdjacent) {
			possibleStates.add(State.CARRY);
		}
		
		if(adequateFuel && sensesFuel && !isAdjacent) {
			possibleStates.add(State.PURSUIT);
		}
		
		if(adequateFuel && receivingBroadcast) {
			possibleStates.add(State.ASSIST);
		}
		
		if(adequateFuel && sensesFuel && !canCarry && isAdjacent) {
			possibleStates.add(State.WAIT);
		}
		
		if(possibleStates.size() == 1) {
			return possibleStates.get(0);
		}
		
		float highestUtility = -1;
		State bestState = State.RANDOM;
		
		for(State state : possibleStates) {
			TestRobot tRobot = new TestRobot(this, state, utility, numTotalRobots);
			tRobot.testState();
			float utility = tRobot.getUtility();
			System.out.println("* "+state.toString() + " : " + utility);
			if(utility > highestUtility) {
				highestUtility = utility;
				bestState = state;
			}
		}
		
		return bestState;
		
		/*
		if(fuelLevel <= 0){
			return State.REFUEL;
			
		}
		
		if(this.payload != null) {
			if(payload.size <= payload.handlers.size()) {
				System.out.println("Has load, can carry");
				return State.CARRY;
			}
		}
		
		if(communicator.isReceiving) {
			sensor.detectFuel(grid.getLocation(this), grid);
			if(!sensor.isAdjacent) {
				System.out.println("Assist");
				return State.ASSIST;
			}
		}
		
		sensor.detectFuel(grid.getLocation(this), grid);
		this.sensesFuel = sensor.sensesFuel;

		if(this.sensesFuel) {
			if(sensor.isAdjacent) {
				if(this.payload == null) {
					for (Object obj : grid.getObjectsAt(sensor.location.getX(), sensor.location.getY())) {
						if (obj instanceof Resource) {
							((Resource) obj).handlers.add(this);
							payload = (Resource)obj;
							break;
						}
					}
				}
				if(payload.size <= payload.handlers.size()) {
					canCarry = true;
					System.out.println("carry");
					return State.CARRY;
				} else {
					canCarry = false;
					System.out.println("wait: " + payload.handlers.size() + "/" + payload.size);
					return State.WAIT;
				}
			}
			//at this point, the sensor senses something, but the robot is not adjacent to the resource
			System.out.println("pursue");
			return State.PURSUIT;
		}
		
		System.out.println("random");
		return State.RANDOM;
		
		*/
	}
	
	public int calculateUtility(State state) {
		
		int utility = 0;
		
		//utility for resource proximity
		if(this.sensesFuel) {
			utility = (int) (utility + 100/sensor.distance);
		}
		
		return utility;
	}
	
	// Move in a random direction
	public void random() {
		System.out.println("Random");
		GridPoint current = grid.getLocation(this);
		int randomX = RandomHelper.nextIntFromTo(-1, 1);
		int randomY = RandomHelper.nextIntFromTo(-1, 1);
		int[] pointArray = { current.getX() + randomX, current.getY() + randomY };
		GridPoint randomPoint = new GridPoint(pointArray);
		moveTowards(randomPoint);
		
		fuelLevel = fuelLevel - fuelRate*2;
	}
	
	//TODO pursuit
	public void pursue() {
		System.out.println("Pursuing Location: " + sensor.location.getX() + " , " + sensor.location.getY());
		moveTowards(sensor.location);
	}
	
	//TODO assist
	public void assist() {
		System.out.println("Assist");
		//some logic for determining which robot to assist.
		//going with closest robot for now
		GridPoint closestLocation = grid.getLocation(this);
		float smallestDistance = Float.MAX_VALUE;
		
		for(Message m : communicator.receivedMessages) {
			float distance = (float) Math.sqrt(
		            Math.pow(m.location.getX() - grid.getLocation(this).getX(), 2) +
		            Math.pow(m.location.getY() - grid.getLocation(this).getY(), 2) );
			
			if(distance < smallestDistance) {
				smallestDistance = distance;
				closestLocation = m.location;
			}
			
		}
		
		moveTowards(closestLocation);
		communicator.isReceiving = false;
		communicator.receivedMessages.clear();
	}
	
	//TODO carry
	public void carry() {
		System.out.println("Carry");
		moveTowards(grid.getLocation(HQ));
		if(payload.handlers.get(0).equals(this)) {
			moveObjectTowards(grid.getLocation(HQ), payload);
		}
		fuelLevel = fuelLevel - fuelRate*4;
	}
	
	//TODO assist
	public void waitForAssistance() {
		System.out.println("Wait");
		this.communicator.emit(grid.getLocation(this), grid, payload.value, payload.size, payload.size - payload.handlers.size());
		fuelLevel = fuelLevel - fuelRate;
	}
	
	//TODO refuel
	public void refuel() {
		System.out.println("Refuel");
		//just sit for now
		
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
		}
	}
	
	public void moveObjectTowards(GridPoint pt, Object obj) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(obj))) {
			NdPoint myPoint = space.getLocation(obj);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(obj,  1,  angle, 0);
			myPoint = space.getLocation(obj);
			grid.moveTo(obj, (int)myPoint.getX(), (int)myPoint.getY());
		}
	}
	
	public void setPayload(Resource resource) {
		this.payload = resource;
	}
	
	public enum State {
		RANDOM, PURSUIT, ASSIST, CARRY, WAIT, REFUEL
	}
	
	
}
