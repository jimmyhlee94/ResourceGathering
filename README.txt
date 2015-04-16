README - ResourceGathering Multi-Agent System

Team Amirite? 
 - Jimmy Lee
 - Brad Steiner
 - Sawyer Jager

Framework: Repast Simphony
Repast is an agent-based, open-source platform for modeling and simulating multi-agent systems. 

Source Files Included:
 Communicator.java
 Debugger.java
 Headquarter.java
 Message.jva
 ModelInitializer.groovy
 Resource.java
 ResourceGatheringBuilder.java
 ResourceSensor.java
 ResourceStyle.java
 Robot.java
 TestRobot.java
 Utility.java
 ModelInitializer.agent

How to Run:
Our project can be built and run using Repast Simphony version 2.2. 
To run, simply open the project in Eclipse and run the ResourceGathering Model.
This will open the Repast Simphony GUI. 
From here, we have the option of changing global parameters, and running the simulation.
To start, open the 'Run Options' tab and move the 'Schedule Tick Delay' to a value of 10 or more. This will slow the simulation down to observe the agents' behavior and prevent freezes. In the Parameters tab, you can adjust these parameters for different simulation outcomes. 
Next, press the 'Start' button to begin simulation of the system.

Description of Simulation:
The green ‘X’ in the middle represents the headquarters ship. The blue ‘+’s are the robot agents, and the red squares are the fuel resources. The robots search for fuel to bring back to the mothership. The fuel ranges in size small enough for one robot to carry or large enough that it needs all the robots to carry. The robots are able to sense fuel with sensors as well as communicate with limited range if they need help carrying a resource. Robots decide what action they will take based on a utility function. Robots also must return to HQ to refuel if their supply becomes low. This decreases the stored amount.

Global Parameters:
Communication Range - Integer
How far the robots can broadcast out that they need help carrying a resource
Default Random Seed
Number used to generate random placement of resources
Field Size - Integer
How large the wraparound environment is
Fuel Depletion Rate - Float
How fast each robot’s fuel is consumed. Carrying uses the most fuel, followed by movements, followed by waiting.
Full Tank Utility - Integer
HQ Proximity Bonus - Integer
Resource Proximity Bonus - Integer
Resource Weight - Integer
Resources - Integer
How many resources are distributed across the map
Robot Fuel Cap.
The capacity of the robots fuel storage
Robots
How many starting robots there are
Sensor Range
How far away robots can detect resources

Displays:
The first display is a Task Allocation Graph. This shows how many robots are in each of the seven states. The seven states are:
Assist
Go help another robot with a resource
Carry
Carry a resource back to HQ
Pursuit
Go towards a resource detected with resource sensor
Random
Move in a random direction
Refuel
Go back to HQ to refuel
Wait
Stay by resource and broadcast out that help is needed
Dead
Fuel storage has run out. Can no longer function

The second display shows the fuel levels. It displays the amount stored, the amount remaining in the environment, and the amount consumed.


