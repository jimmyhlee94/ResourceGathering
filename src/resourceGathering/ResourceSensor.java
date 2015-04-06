package resourceGathering;

import repast.simphony.space.continuous.NdPoint;

public class ResourceSensor {

	public boolean sensesFuel;
	public int range;
	public NdPoint location;
	
	
	public ResourceSensor(int maxRange)
	{
		this.range = maxRange;
	}
}
