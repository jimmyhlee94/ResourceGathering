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
	
	public int id;
	
	public int maxFuelLevel, fuelLevel;
	public int fuelRate;
	
	private boolean adequateFuel, sensesFuel, receivingBroadcast, canCarry, isAdjacentToSensorTarget, isAdjacentToMessageTarget;
	
	public ResourceSensor sensor;
	public Communicator communicator;
	
	public Resource payload;
	
	private State currentState;
	
	public Headquarters HQ;
	
	private GridPoint hqLocation;
	
	int maxSensorRange;
	int maxCommunicationRange;
	
	Utility utility;
	
	public Robot(ContinuousSpace<Object> space, Grid<Object> grid, Headquarters HQ, int maxFuelLevel, int fuelRate,
			int maxSensorRange, int maxCommunicationRange, int id, Utility utility) {
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
		
		this.sensor = new ResourceSensor(maxSensorRange);
		
		this.communicator = new Communicator(maxCommunicationRange);
		this.payload = null;
		
		this.adequateFuel = true;
		this.sensesFuel = false;
		this.receivingBroadcast = false;
		this.canCarry = false;
		this.isAdjacentToSensorTarget = false;
		this.isAdjacentToMessageTarget = false;
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
		this.canCarry = false;
		this.isAdjacentToSensorTarget = false;
		this.isAdjacentToMessageTarget = false;
		this.receivingBroadcast = false;
		
		if(communicator.isEmitting) {
			communicator.stopEmitting();
		}
		
		communicator.isReceiving = false;
		communicator.receivedMessages.clear();
		System.out.println("	-Cleared Messages.");
		
		//preliminary actions
		sensor.detectFuel(grid.getLocation(this), grid);
		communicator.receive(grid.getLocation(this), grid);
		
		//calculate if more fuel is needed
		NdPoint hq = space.getLocation(HQ);
		NdPoint current = space.getLocation(this);		
		double currentDistance = (double) Math.sqrt(
		            Math.pow(current.getX() - hq.getX(), 2) +
		            Math.pow(current.getY() - hq.getY(), 2) );
		double angle = SpatialMath.calcAngleFor2DMovement(space, current, hq);	
		double oppositeLength = currentDistance * Math.sin(angle);
		double adjacentLength = currentDistance * Math.cos(angle);	
		int fuelToHQ = (int)(oppositeLength+ adjacentLength)*fuelRate*2;
		
		
		//set all booleans
		if(fuelToHQ < fuelLevel){
			System.out.println("Refuel - " + fuelLevel);
			this.adequateFuel = true;
		}
		
		
		if(payload != null) {
			System.out.println("Has payload");
			sensesFuel = true;
			sensor.location = grid.getLocation(payload);
			isAdjacentToSensorTarget = true;
			isAdjacentToMessageTarget = true;
		}
		
		if((sensor.sensesFuel) && (payload == null)) {
			System.out.println("-Senses fuel.");
			this.sensesFuel = true;
		}
		
		if((communicator.isReceiving) && (payload == null)) {
			System.out.println("-Receiving broadcast: " + communicator.receivedMessages.size());
			this.receivingBroadcast = true;
			GridPoint bestMessageLocation = communicator.findBestLocation(grid, grid.getLocation(this), utility);
			if(calculateDistance(grid.getLocation(this), bestMessageLocation) <= Math.sqrt(2)) {
				System.out.println("-Next to broadcast target.");
				this.isAdjacentToMessageTarget = true;
				
				//attach to adjacent object
				if(this.payload == null) {
					for (Object obj : grid.getObjectsAt(bestMessageLocation.getX(), bestMessageLocation.getY())) {
						if (obj instanceof Resource) {
							((Resource) obj).handlers.add(this);
							payload = (Resource)obj;
							break;
						}
					}
					System.out.println("attached to object");
				}
			}
			
		}
		if(payload != null) {
			if(payload.handlers.size() >= payload.size) {
				System.out.println("-Can Carry.");
				this.canCarry = true;
			}
		}
		if((sensor.isAdjacent) && (payload == null)){
			System.out.println("-Next to sensor target.");
			this.isAdjacentToSensorTarget = true;

			//attach to adjacent object
			if(this.payload == null) {
				for (Object obj : grid.getObjectsAt(sensor.location.getX(), sensor.location.getY())) {
					if (obj instanceof Resource) {
						((Resource) obj).handlers.add(this);
						payload = (Resource)obj;
						break;
					}
				}
				System.out.println("attached to object");
			}
		}
		
		//output state
		
		if(!adequateFuel) {
			return State.REFUEL;
		}
		
		if(adequateFuel && !sensesFuel && !receivingBroadcast && !canCarry && !isAdjacentToMessageTarget && !isAdjacentToSensorTarget) {
			return State.RANDOM;
		}
		
		ArrayList<State> possibleStates = new ArrayList<State>();
		
		if(adequateFuel && sensesFuel && canCarry && isAdjacentToMessageTarget) {
			possibleStates.add(State.CARRY);
		}
		
		if(adequateFuel && sensesFuel && !isAdjacentToSensorTarget) {
			possibleStates.add(State.PURSUIT);
		}
		
		if(adequateFuel && receivingBroadcast) {
			possibleStates.add(State.ASSIST);
		}
		
		if(adequateFuel && sensesFuel && !canCarry && (isAdjacentToSensorTarget || isAdjacentToMessageTarget)) {
			possibleStates.add(State.WAIT);
		}
		
		if(possibleStates.size() == 1) {
			return possibleStates.get(0);
		}
		
		float highestUtility = -1;
		State bestState = State.RANDOM;
		
		for(State state : possibleStates) {
			System.out.println("Testing: " + state.toString());
			TestRobot tRobot = new TestRobot(this, state, utility);
			tRobot.testState();
			float utility = tRobot.getUtility();
			System.out.println("* "+state.toString() + " : " + utility);
			if(utility > highestUtility) {
				highestUtility = utility;
				bestState = state;
			}
		}
		
		return bestState;
	}
	
	// Move in a random direction
	public void random() {
		releasePayload();
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
		releasePayload();
		System.out.println("Assist");
		//some logic for determining which robot to assist.
		//use the utility function to determine the best message.
		GridPoint bestLocation = grid.getLocation(this);
		float highestUtility = -1;
		
		for(Message m : communicator.receivedMessages) {
			float utility = this.utility.UtilityOfProximityToOthers(m.resourceValue, m.resourceSize, m.handlersNeeded, calculateDistance(grid.getLocation(this),m.location), grid.getDimensions().getHeight());
			
			if(utility > highestUtility) {
				highestUtility = utility;
				bestLocation = m.location;
			}
			
		}
		moveTowards(bestLocation);
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
		
		this.communicator.emit(grid.getLocation(this),grid.getLocation(payload), grid, payload.value, payload.size, payload.size - payload.handlers.size());
		fuelLevel = fuelLevel - fuelRate;
	}
	
	//TODO refuel
	public void refuel() {
		releasePayload();
		System.out.println("Refuel");
		//just sit for now
		moveTowards(grid.getLocation(HQ));		
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
	
	public void releasePayload() {
		if(payload != null) {
			this.payload.handlers.remove(this);
			this.payload = null;
		}
	}
	
	public void setPayload(Resource resource) {
		this.payload = resource;
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
	
	public int getMaxFuelLevel(){
		return maxFuelLevel;
	}
	
	public void setFuelLevel(int fl){
		fuelLevel = fl;
		return;
	}
	
	public enum State {
		RANDOM, PURSUIT, ASSIST, CARRY, WAIT, REFUEL
	}
	
	public int IsRandom() {
		return this.currentState == State.RANDOM ? 1 : 0;
	}
	
	public int IsPursuit() {
		return this.currentState == State.PURSUIT ? 1 : 0;
	}
	
	public int IsAssist() {
		return this.currentState == State.ASSIST ? 1 : 0;
	}
	
	public int IsCarry() {
		return this.currentState == State.CARRY ? 1 : 0;
	}
	
	public int IsWait() {
		return this.currentState == State.WAIT ? 1 : 0;
	}
	
	public int IsRefuel() {
		return this.currentState == State.REFUEL ? 1 : 0;
	}
}
