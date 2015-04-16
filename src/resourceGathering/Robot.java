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
	
	public int maxFuelLevel;
	public float fuelConsumed =0;
	public float fuelRate, fuelLevel;
	
	private boolean adequateFuel, sensesFuel, receivingBroadcast, canCarry, isAdjacentToSensorTarget, isAdjacentToMessageTarget;
	private boolean outOfFuel;
	
	public ResourceSensor sensor;
	public Communicator communicator;
	
	public Resource payload;
	
	private State currentState;
	private int ticksInCurrentState = 0;
	
	public Headquarters HQ;
	
	private GridPoint hqLocation;
	
	int maxSensorRange;
	int maxCommunicationRange;
	
	Debugger debugger;
	
	Utility utility;
	
	public Robot(ContinuousSpace<Object> space, Grid<Object> grid, Headquarters HQ, int maxFuelLevel, float fuelRate,
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
		this.outOfFuel = false;
		this.debugger = new Debugger(this);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		debugger.log("ID: " + id);
		State prevState = currentState;
		currentState = determineState();
		if(currentState == prevState){
			this.ticksInCurrentState++;
		} else {
			this.ticksInCurrentState = 0;
		}
		
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
		case OUT_OF_FUEL:
			outOfFuel();
			break;
		}
		debugger.log("");
	}
	
	public State determineState() {				

		this.adequateFuel = false;
		this.sensesFuel = false;
		this.canCarry = false;
		this.isAdjacentToSensorTarget = false;
		this.isAdjacentToMessageTarget = false;
		this.receivingBroadcast = false;
		
		if(fuelLevel <= 0) {
			outOfFuel = true;
			return State.OUT_OF_FUEL;
		}
		
		if(communicator.isEmitting) {
			communicator.stopEmitting();
		}
		
		communicator.isReceiving = false;
		communicator.receivedMessages.clear();
		
		//preliminary actions
		sensor.detectClosestFuel(grid.getLocation(this), grid);

		GridPoint payloadLocation = null;
		if(payload != null) {
			payloadLocation = grid.getLocation(payload);
		}
		
		communicator.receive(grid.getLocation(this),payloadLocation, grid);
		
		//calculate if more fuel is needed
		NdPoint hq = space.getLocation(HQ);
		NdPoint current = space.getLocation(this);		
		double currentDistance = (double) Math.sqrt(
		            Math.pow(current.getX() - hq.getX(), 2) +
		            Math.pow(current.getY() - hq.getY(), 2) );
		double angle = SpatialMath.calcAngleFor2DMovement(space, current, hq);	
		double oppositeLength = currentDistance * Math.sin(angle);
		double adjacentLength = currentDistance * Math.cos(angle);	
		float fuelToHQ = Math.abs((int)(oppositeLength+ adjacentLength)*fuelRate*2) + 4;
		
		
		//set all booleans
		if(fuelToHQ < fuelLevel){
			this.adequateFuel = true;
		} else {
			this.adequateFuel = false;
		}
		
		
		if(payload != null) {
			debugger.log("-Has payload");
			sensesFuel = true;
			sensor.location = grid.getLocation(payload);
			isAdjacentToSensorTarget = true;
			isAdjacentToMessageTarget = true;
		}
		
		if((sensor.sensesFuel) && (payload == null)) {
			//debugger.log("-Senses fuel.");
			this.sensesFuel = true;
		}
		
		if((communicator.isReceiving)) {
			//debugger.log("-Receiving broadcast: " + communicator.receivedMessages.size());
			this.receivingBroadcast = true;
			GridPoint bestMessageLocation = communicator.findBestLocation(grid, grid.getLocation(this), utility);
			Iterable<Object> resources = grid.getObjectsAt(bestMessageLocation.getX(), bestMessageLocation.getY());
			
			boolean resourceExists = false;
			
			for(Object obj : resources) {
				if(obj instanceof Resource) {
					resourceExists = true;
					break;
				}
			}
			
			if(!resourceExists) {
				return State.RANDOM;
			}
			
			if(calculateDistance(grid.getLocation(this), bestMessageLocation) <= Math.sqrt(2)) {
				//debugger.log("-Next to broadcast target.");
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
					//debugger.log("-attached to object");
				}
			}
			
		}
		if(payload != null) {
			if(payload.handlers.size() >= payload.size) {
				//debugger.log("-Can Carry.");
				this.canCarry = true;
			}
		}
		if((sensor.isAdjacent) && (payload == null)){
			debugger.log("-Next to sensor target.");
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
				//debugger.log("-attached to object");
			}
		}
		
		//output state
		/*
		debugger.log("AF: " + adequateFuel);
		debugger.log("SF: " + sensesFuel);
		debugger.log("CC: " + canCarry);
		debugger.log("AdjS: " + isAdjacentToSensorTarget);
		debugger.log("AdjM: " + isAdjacentToMessageTarget);
		debugger.log("RB: " + receivingBroadcast);
		debugger.log("Pay: " + (payload != null));
		*/
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
			TestRobot tRobot = new TestRobot(this, state, utility);
			tRobot.testState();
			float utility = tRobot.getUtility();
			debugger.log("* "+state.toString() + " : " + utility);
			if(utility > highestUtility) {
				highestUtility = utility;
				bestState = state;
			}
		}
		
		return bestState;
	}
	
	public void outOfFuel() {
		//do nothing for now
		releasePayload();
	}
	
	// Move in a random direction
	public void random() {

		releasePayload();
		debugger.log("Random");
		GridPoint current = grid.getLocation(this);
		int randomX = RandomHelper.nextIntFromTo(-1, 1);
		int randomY = RandomHelper.nextIntFromTo(-1, 1);
		int[] pointArray = { current.getX() + randomX, current.getY() + randomY };
		GridPoint randomPoint = new GridPoint(pointArray);
		moveTowards(randomPoint);
		
	}
	
	//TODO pursuit
	public void pursue() {
		if(!adequateFuel) {
			debugger.isDebugging = true;
		}
		debugger.log("Pursuing Location: " + sensor.location.getX() + " , " + sensor.location.getY());
		moveTowards(sensor.location);
	}
	
	//TODO assist
	public void assist() {

		releasePayload();
		debugger.log("Assist");
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

		debugger.log("Carry");
		moveTowards(grid.getLocation(HQ));
		if(payload.handlers.get(0).equals(this)) {
			moveObjectTowards(grid.getLocation(HQ), payload);
		}
		//extra fuel for carrying
		fuelLevel = fuelLevel - fuelRate*2;
		fuelConsumed += fuelRate*2;
	}
	
	//TODO assist
	public void waitForAssistance() {

		debugger.log("Wait");
		
		if((payload != null) &&(grid.getLocation(payload) != null)) {
			
			this.communicator.emit(grid.getLocation(this),grid.getLocation(payload), grid, payload.value, payload.size, payload.size - payload.handlers.size());
		} else {
			random();
		}
		fuelLevel = fuelLevel - fuelRate;
		fuelConsumed += fuelRate;
	}
	
	//TODO refuel
	public void refuel() {

		releasePayload();
		debugger.log("Refuel");
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
			fuelLevel = fuelLevel - fuelRate*2;
			fuelConsumed += fuelRate*2;
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
			fuelLevel = fuelLevel - fuelRate*4;
			fuelConsumed += fuelRate*4;

		}
	}
	
	public void releasePayload() {
		if(payload != null) {
			this.payload.handlers.remove(this);
			this.payload = null;
		}
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
	
	public int getTicksInCurrentState(){
		return this.ticksInCurrentState;
	}

	public int getID(){
		return id;
	}
	
	public int getMaxFuelLevel(){
		return maxFuelLevel;
	}
	
	public float getFuelConsumed(){
		return fuelConsumed;
	}
	
	public float getFuelLevel(){
		return fuelLevel;
	}
	
	public boolean isAdequateFuel() {
		return adequateFuel;
	}

	public boolean isSensesFuel() {
		return sensesFuel;
	}

	public boolean isReceivingBroadcast() {
		return receivingBroadcast;
	}

	public boolean isCanCarry() {
		return canCarry;
	}

	public boolean isAdjacentToSensorTarget() {
		return isAdjacentToSensorTarget;
	}

	public boolean isAdjacentToMessageTarget() {
		return isAdjacentToMessageTarget;
	}

	public boolean isOutOfFuel() {
		return outOfFuel;
	}

	public ResourceSensor getSensor() {
		return sensor;
	}

	public Communicator getCommunicator() {
		return communicator;
	}

	public Resource getPayload() {
		return payload;
	}

	public State getCurrentState() {
		return currentState;
	}

	public Utility getUtility() {
		return utility;
	}
	
	
	
	
	public void setPayload(Resource resource) {
		this.payload = resource;
	}
	
	public void setFuelLevel(int fl){
		fuelLevel = fl;
		return;
	}
	
	
	
	public enum State {
		RANDOM, PURSUIT, ASSIST, CARRY, WAIT, REFUEL, OUT_OF_FUEL
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
	
	public int IsDead() {
		return this.currentState == State.OUT_OF_FUEL ? 1 : 0;
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
