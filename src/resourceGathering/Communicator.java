package resourceGathering;

import repast.simphony.space.continuous.NdPoint;

public class Communicator {

	public class Message {
		public NdPoint location;
		public int resourceSize;
	}
	
	private int range;
	public boolean isEmitting;
	public boolean isReceiving;
	
	private Message message;
	
	public Communicator() {
		
	}
}
