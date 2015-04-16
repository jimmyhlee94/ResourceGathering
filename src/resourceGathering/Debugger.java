package resourceGathering;

public class Debugger {
	public boolean isDebugging;
	public Robot brokenRobot;
	
	public Debugger(Robot brokenRobot) {
		isDebugging = true;
		this.brokenRobot = brokenRobot;
	}
	
	public void log(String string) {
		if(isDebugging){
			System.out.println(string);
		}
	}
}
