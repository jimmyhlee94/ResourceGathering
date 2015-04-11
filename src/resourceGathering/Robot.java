package resourceGathering;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Robot {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int id;
	
	private int maxFuelLevel, fuelLevel;
	
	private boolean adequateFuel, sensesFuel, receivingBroadcast, canCarry, isCarrying;
	
	private ResourceSensor sensor;
	private Communicator communicator;
	
	private Resource payload;
	
	private State currentState;
	
	private Headquarters HQ;
	
	private GridPoint hqLocation;
	
	public Robot(ContinuousSpace<Object> space, Grid<Object> grid, Headquarters HQ, int maxFuelLevel, int sensorMaxRange, int id) {
		this.space = space;
		this.grid = grid;
		
		this.id = id;
		
		//this.hqLocation = hqLocation;
		this.HQ = HQ;
		
		this.maxFuelLevel = maxFuelLevel;
		this.fuelLevel = maxFuelLevel;
		this.sensor = new ResourceSensor(sensorMaxRange);
		
		this.payload = null;
		
		this.adequateFuel = true;
		this.sensesFuel = false;
		this.receivingBroadcast = false;
		this.canCarry = true;
		this.isCarrying = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		System.out.println(id);
		currentState = determineState();
		
		switch(this.currentState) {
		case RANDOM:
			random();
			break;
		case PURSUIT:
			pursue();
			break;
		case ASSIST:
			break;
		case CARRY:
			carry();
			break;
		case WAIT:
			break;
		case REFUEL:
			break;
		}
	}
	
	public State determineState() {
		
		if(this.payload != null) {
			if(payload.size <= payload.handlers.size()) {
				System.out.println("Has load, can carry");
				return State.CARRY;
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
			//System.out.println("pursue");
			return State.PURSUIT;
		}
		
		//System.out.println("random");
		return State.RANDOM;
	}
	
	// Move in a random direction
	public void random() {
		GridPoint current = grid.getLocation(this);
		int randomX = RandomHelper.nextIntFromTo(-1, 1);
		int randomY = RandomHelper.nextIntFromTo(-1, 1);
		int[] pointArray = { current.getX() + randomX, current.getY() + randomY };
		GridPoint randomPoint = new GridPoint(pointArray);
		moveTowards(randomPoint);
	}
	
	//TODO pursuit
	public void pursue() {
		moveTowards(sensor.location);
	}
	
	//TODO assist
	public void assist() {
		
	}
	
	//TODO carry
	public void carry() {
		moveTowards(grid.getLocation(HQ));
		if(payload.handlers.get(0).equals(this)) {
			moveObjectTowards(grid.getLocation(HQ), payload);
		}
		
	}
	
	//TODO assist
	public void waitForAssistance() {
		
	}
	
	//TODO refuel
	public void refuel() {
		
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
