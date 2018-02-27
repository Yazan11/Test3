package cps2Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.LinkedList;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.systemdynamics.translator.RepastSimphonyEnvironment;

/**
 * Context creating class. Creates and places all the agents of the simulation.
 * Also used to get informations/actions on the environment on the Sensor Agents
 * array (get the TVD and temperature, slow down the drill, cooling the
 * equipments).
 * 
 * @author Yazan Mualla
 * @version 2.0
 * @since 2017-04-11
 *
 */
public class ContextCreator implements ContextBuilder<Agent> {

	static HashMap<Integer, String> log = new HashMap<Integer, String>();
	static File logFile;
	static FileWriter logWriter;

	static File logFile1;
	static FileWriter logWriter1;

	static File logFile2;
	static FileWriter logWriter2;

	static File logFile3;
	static FileWriter logWriter3;

	static File logFile4;
	static FileWriter logWriter4;

	static File DMlogFile;
	static FileWriter DMlogWriter;

	static File coolingLogFile;
	static FileWriter coolingLogWriter;

	static File votingLogFile;
	static FileWriter votingLogWriter;

	static ArrayList<String> coolingLogString = new ArrayList<>();

	/*--------------CONSTANS-----------------*/
	final int DANGER_TEMP_CONST = 100;
	final int CRITICAL_TEMP_CONST = 125;
	final int SHUTDOWN_TEMP_CONST = 150;
	final int CRITICALITY_SCALE_CONST = 25;

	/*--------------VARIABLES-----------------*/
	double drillingAngle;
	double rotationsPerMinute;
	double minimimumRotationsPerMinute;
	double weightOnBit;
	float holeDiameter;
	boolean hardFormation;
	float surfaceTemp;
	float geothermalGradient;
	double coolDown = 0; // the cooling down taking place, starts at no cooling
							// down
	int depthGoal;
	boolean constantWOBincrease;
	double currentSetWOB;
	ContinuousSpaceFactory spaceFactory;
	ContinuousSpace<Agent> space;

	protected Context<Agent> context;

	double heightDifferentLayers;
	int numDifferentLayers;
	double startingRunDepth = 0;
	double[][] differentLayers;
	int votingRuleChoice;
	int toolVotingWeight;

	/*--------------STATIC BLOCK-----------------*/
	static {

		try {
						
			File directory = new File(String.valueOf("Log"));

			 if(!directory.exists()){

			             directory.mkdir();
			 }
			logFile = new File("Log/log File.csv");
			logWriter = new FileWriter(logFile);

			coolingLogFile = new File("Log/CoolingLogFile.csv");
			coolingLogWriter = new FileWriter(coolingLogFile);

			votingLogFile = new File("Log/Voting log File.csv");
			votingLogWriter = new FileWriter(votingLogFile);

			DMlogFile = new File("Log/DMlog File.txt");
			DMlogWriter = new FileWriter(DMlogFile);

			ContextCreator.logWriter.append("IDSensorAgent");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("MeasuredDepth");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("TrueDepth");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("Temperature");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("DangerTemp");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("CriticalTemp");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("ShutdownTemp");
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append("Ticks");
			ContextCreator.logWriter.append('\n');

			logFile1 = new File("Log/log File1.csv");
			logWriter1 = new FileWriter(logFile1);

			ContextCreator.logWriter1.append("IDSensorAgent");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("MeasuredDepth");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("TrueDepth");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("Temperature");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("DangerTemp");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("CriticalTemp");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("ShutdownTemp");
			ContextCreator.logWriter1.append(',');
			ContextCreator.logWriter1.append("Ticks");
			ContextCreator.logWriter1.append('\n');

			logFile2 = new File("Log/log File2.csv");
			logWriter2 = new FileWriter(logFile2);

			ContextCreator.logWriter2.append("IDSensorAgent");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("MeasuredDepth");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("TrueDepth");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("Temperature");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("DangerTemp");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("CriticalTemp");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("ShutdownTemp");
			ContextCreator.logWriter2.append(',');
			ContextCreator.logWriter2.append("Ticks");
			ContextCreator.logWriter2.append('\n');

			logFile3 = new File("Log/log File3.csv");
			logWriter3 = new FileWriter(logFile3);

			ContextCreator.logWriter3.append("IDSensorAgent");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("MeasuredDepth");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("TrueDepth");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("Temperature");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("DangerTemp");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("CriticalTemp");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("ShutdownTemp");
			ContextCreator.logWriter3.append(',');
			ContextCreator.logWriter3.append("Ticks");
			ContextCreator.logWriter3.append('\n');

			logFile4 = new File("Log/log File4.csv");
			logWriter4 = new FileWriter(logFile4);

			ContextCreator.logWriter4.append("IDSensorAgent");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("MeasuredDepth");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("TrueDepth");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("Temperature");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("DangerTemp");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("CriticalTemp");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("ShutdownTemp");
			ContextCreator.logWriter4.append(',');
			ContextCreator.logWriter4.append("Ticks");
			ContextCreator.logWriter4.append('\n');

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Log File problem");
		}
	}

	/*--------------GETTERS AND SETTERS-----------------*/

	public double getWeightOnBit() {
		return weightOnBit;
	}

	public void setWeightOnBit(double weightOnBit) {
		this.weightOnBit = weightOnBit;
	}

	public boolean isConstantWOBincrease() {
		return constantWOBincrease;
	}

	public int getVotingRuleChoice() {
		return votingRuleChoice;
	}

	/*--------------CONSTRUCTOR-----------------*/
	/**
	 * Function used to create and place the agents in the simulation.
	 */
	@Override
	public Context<Agent> build(Context<Agent> context) {

		/*--------------GETTING THE VARIABLES-----------------*/
//		int nbSensor = RunEnvironment.getInstance().getParameters().getInteger("nbSensor"); //Number of Agents
//		int nbActuator = RunEnvironment.getInstance().getParameters().getInteger("nbActuator");
//		drillingAngle = RunEnvironment.getInstance().getParameters().getFloat("drillingAngle");
//		rotationsPerMinute = RunEnvironment.getInstance().getParameters().getDouble("initialRPM");
//		float downHoleRPMPercentage = RunEnvironment.getInstance().getParameters().getFloat("downHoleRPMPercentage");
//		minimimumRotationsPerMinute = rotationsPerMinute * (1 - (downHoleRPMPercentage / 100));
//		weightOnBit = RunEnvironment.getInstance().getParameters().getDouble("weightOnBit");
//		// weightOnBit = 0;
//		holeDiameter = RunEnvironment.getInstance().getParameters().getFloat("holeDiameter");
//		hardFormation = RunEnvironment.getInstance().getParameters().getBoolean("hardFormation");
//		constantWOBincrease = RunEnvironment.getInstance().getParameters().getBoolean("constantWOBincrease");
//		// currentSetWOB =
//		// RunEnvironment.getInstance().getParameters().getDouble("weightOnBit");
//		surfaceTemp = RunEnvironment.getInstance().getParameters().getFloat("surfaceTemp");
//		geothermalGradient = RunEnvironment.getInstance().getParameters().getFloat("geothermalGradient");
//		double actuatorEffectiveness = RunEnvironment.getInstance().getParameters().getDouble("actuatorEffectiveness");
//		depthGoal = RunEnvironment.getInstance().getParameters().getInteger("depthGoal");
//		boolean voteEnabled = RunEnvironment.getInstance().getParameters().getBoolean("votingEnabled");
//		boolean coolingEnabled = RunEnvironment.getInstance().getParameters().getBoolean("coolingEnabled");
//		startingRunDepth = RunEnvironment.getInstance().getParameters().getDouble("startingRunDepth");
//		int nextID = 1;
//		heightDifferentLayers = RunEnvironment.getInstance().getParameters().getDouble("heightDifferentLayers");
//		numDifferentLayers = RunEnvironment.getInstance().getParameters().getInteger("numDifferentLayers");
//		votingRuleChoice = RunEnvironment.getInstance().getParameters().getInteger("votingRuleChoice");
//		toolVotingWeight = RunEnvironment.getInstance().getParameters().getInteger("toolVotingWeight");
		
		int nbSensor = RunEnvironment.getInstance().getParameters().getInteger("nbSensor");//Number of Sensor Agents
		int nbActuator = RunEnvironment.getInstance().getParameters().getInteger("nbActuator");
		drillingAngle = RunEnvironment.getInstance().getParameters().getDouble("drillingAngle");//Drilling Angle (degree)
		rotationsPerMinute = RunEnvironment.getInstance().getParameters().getDouble("initialRPM");//Initial RPM (0 - 100) Default 1
		float downHoleRPMPercentage = RunEnvironment.getInstance().getParameters().getFloat("downHoleRPMPercentage");
		minimimumRotationsPerMinute = rotationsPerMinute * (1 - (downHoleRPMPercentage / 100));
		weightOnBit = RunEnvironment.getInstance().getParameters().getDouble("weightOnBit");//Initial WOB (0 - 30 klbf)
		// weightOnBit = 0;
		holeDiameter = RunEnvironment.getInstance().getParameters().getFloat("holeDiameter"); //Hole Diameter ( inch)
		hardFormation = RunEnvironment.getInstance().getParameters().getBoolean("hardFormation");
		constantWOBincrease = RunEnvironment.getInstance().getParameters().getBoolean("constantWOBincrease");
		//WOBIncreaseAmount, WOB Increase per Tick (klbf)
		// currentSetWOB =
		// RunEnvironment.getInstance().getParameters().getDouble("weightOnBit");
		surfaceTemp = RunEnvironment.getInstance().getParameters().getFloat("surfaceTemp");//Surface Temperature (degC)
		geothermalGradient = RunEnvironment.getInstance().getParameters().getFloat("geothermalGradient");//Geothermal Gradient (degC/m)
		double actuatorEffectiveness = RunEnvironment.getInstance().getParameters().getDouble("actuatorEffectiveness");
		depthGoal = RunEnvironment.getInstance().getParameters().getInteger("depthGoal"); //Total Depth (m) Default 550
		boolean voteEnabled = RunEnvironment.getInstance().getParameters().getBoolean("votingEnabled");
		boolean coolingEnabled = RunEnvironment.getInstance().getParameters().getBoolean("coolingEnabled");
		startingRunDepth = RunEnvironment.getInstance().getParameters().getDouble("startingRunDepth"); //Starting Run Depth (m) Default 10
		int nextID = 1;
		heightDifferentLayers = RunEnvironment.getInstance().getParameters().getDouble("heightDifferentLayers");
		numDifferentLayers = RunEnvironment.getInstance().getParameters().getInteger("numDifferentLayers");
		votingRuleChoice = RunEnvironment.getInstance().getParameters().getInteger("votingRuleChoice");
		toolVotingWeight = RunEnvironment.getInstance().getParameters().getInteger("toolVotingWeight");//Voting weight of a tool (Indicate tool ID)

		/*-------------- CHEKING ENTRY PARAMETERS-----------------*/
		if (votingRuleChoice < 1 || votingRuleChoice > 5) {
			System.out.println(
					"Voting Rule Choice should be (1: On/Off, 2: Plurality, 3: Condorcet, 4: Borda, 5: Proposed Rule)");
			RunEnvironment.getInstance().endRun();
		}
		if (nbSensor <= 0) {
			System.out.println("Number of sensors should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (nbActuator <= 0) {
			System.out.println("Number of actuators should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if ((drillingAngle < 0) || (drillingAngle > 90)) {
			System.out.println("drilling angle should be between 0 and 90");
			RunEnvironment.getInstance().endRun();
		}
		if ((rotationsPerMinute < 0) || (rotationsPerMinute > 250)) {
			System.out.println("Initial Rotations Per Minute should be between 0 and 250");
			RunEnvironment.getInstance().endRun();
		}
		if ((downHoleRPMPercentage < 0) || (downHoleRPMPercentage > 100)) {
			System.out.println("downHole RPM Percentage should be between 0 and 100");
			RunEnvironment.getInstance().endRun();
		}
		if (minimimumRotationsPerMinute < 0) {
			System.out.println("Minimimum Rotations Per Minute should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if ((weightOnBit < 0) || (weightOnBit > 100)) {
			System.out.println("weightOnBit should be between 0 and 100");
			RunEnvironment.getInstance().endRun();
		}
		if ((holeDiameter < 4) || (holeDiameter > 85)) {
			System.out.println("Hole Diameter should be between 4 and 85");
			RunEnvironment.getInstance().endRun();
		}
		if (surfaceTemp < 0) {
			System.out.println("Surface Temp should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (geothermalGradient <= 0) {
			System.out.println("Geothermal Gradient should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (startingRunDepth <= 0) {
			System.out.println("Starting Run Depth should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (depthGoal <= 0) {
			System.out.println("Depth Goal should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (heightDifferentLayers < 0) {
			System.out.println("Height of Different Layers should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (numDifferentLayers < 0) {
			System.out.println("Number of Different Layers should be positive");
			RunEnvironment.getInstance().endRun();
		}
		if (heightDifferentLayers > 0 && numDifferentLayers == 0) {
			System.out.println("Number and Height of Different Layers parameters are not compatible");
			RunEnvironment.getInstance().endRun();
		}
		if (heightDifferentLayers == 0 && numDifferentLayers > 0) {
			System.out.println("Number and Height of Different Layers parameters are not compatible");
			RunEnvironment.getInstance().endRun();
		}
		if (this.depthGoal - this.startingRunDepth < 0) {
			System.out.println("Starting Run Depth can not be larger than Depth Goal");
			RunEnvironment.getInstance().endRun();
		}
		if (this.depthGoal - this.startingRunDepth < heightDifferentLayers) {
			System.out.println("Height of Different Layers can not be larger than the run length");
			RunEnvironment.getInstance().endRun();
		}

		/*-------------- WRITING RUN INFO-----------------*/
		// writing to DMLog
		ContextCreator.writeDMlog("nbSensor: " + nbSensor);
		ContextCreator.writeDMlog("nbActuator: " + nbActuator);

		ContextCreator.writeDMlog("startingRunDepth: " + startingRunDepth);
		ContextCreator.writeDMlog("drillingAngle: " + drillingAngle);
		ContextCreator.writeDMlog("holeDiameter: " + holeDiameter);
		ContextCreator.writeDMlog("depthGoal: " + depthGoal);
		ContextCreator.writeDMlog("heightDifferentLayers: " + heightDifferentLayers);
		ContextCreator.writeDMlog("numDifferentLayers: " + numDifferentLayers);
		ContextCreator.writeDMlog("hardFormation: " + hardFormation);

		ContextCreator.writeDMlog("rotationsPerMinute: " + rotationsPerMinute);
		ContextCreator.writeDMlog("downHoleRPMPercentage: " + downHoleRPMPercentage);
		ContextCreator.writeDMlog("minimimumRotationsPerMinute: " + minimimumRotationsPerMinute);
		ContextCreator.writeDMlog("weightOnBit: " + weightOnBit);
		ContextCreator.writeDMlog("constantWOBincrease: " + constantWOBincrease);

		ContextCreator.writeDMlog("surfaceTemp: " + surfaceTemp);
		ContextCreator.writeDMlog("geothermalGradient: " + geothermalGradient);
		ContextCreator.writeDMlog("actuatorEffectiveness: " + actuatorEffectiveness);
		ContextCreator.writeDMlog("coolingEnabled: " + coolingEnabled);

		ContextCreator.writeDMlog("voteEnabled: " + voteEnabled);
		ContextCreator.writeDMlog("votingRuleChoice: " + votingRuleChoice);
		ContextCreator.writeDMlog("toolVotingWeight: " + toolVotingWeight);

		/*-------------- CREATING THE CONTINUOUS SPACE-----------------*/
		spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 11, 120);

		/*--------------CREATING THE READING AGENT-----------------*/
		ReadingAgent readingAgent = new ReadingAgent(this);
		context.add(readingAgent);

		// the system is created bottom-up :
		// 1) the downhole actuator
		// 2) the sensor agent list, ending with the upper most SA
		// 3) the field agent
		// 4) the uphole actuators

		/*--------------CREATING THE DOWNHOLE ACTUATOR-----------------*/
		// Not used now
		// ActuatorAgent daa = new ActuatorAgent(space, nextID, true, nextID +
		// 1, this, 1);
		// nextID++;
		// context.add(daa);
		// space.moveTo(daa, 5, getYCoordinates(nbSensor * 100)); //not shown

		/*--------------CREATING THE SENSOR AGENTS-----------------*/
		for (int i = 0; i < nbSensor - 1; i++) {
			double startingMeasuredDepth = (nbSensor * 10) - (10 * (i + 1)) + startingRunDepth;
			double dangerTemp = DANGER_TEMP_CONST
					+ (int) (Math.random() * ((CRITICAL_TEMP_CONST - DANGER_TEMP_CONST) + 1)); // random
			// value
			// from
			// 100
			// to
			// 125
			double criticalTemp = CRITICAL_TEMP_CONST
					+ (int) (Math.random() * ((SHUTDOWN_TEMP_CONST - CRITICAL_TEMP_CONST - 10) + 1)); // random
			// value
			// from
			// 125
			// to
			// 150
			// double shutdownTemp = criticalTemp + CRITICALITY_SCALE_CONST;
			double shutdownTemp = SHUTDOWN_TEMP_CONST + ((int) (Math.random() * 7)); // for
																						// now
																						// all
																						// have
																						// 150
			// shutdown temp

			SensorAgent sa = new SensorAgent(space, nextID, nextID + 1, nextID - 1, nbSensor, this,
					startingMeasuredDepth, dangerTemp, criticalTemp, shutdownTemp, voteEnabled);
			context.add(sa);
			space.moveTo(sa, 5, getYCoordinates(startingMeasuredDepth));
			nextID++;
			System.out.println("Agent" + (i + 1) + " dangerTemp = " + dangerTemp);
			// writing to DMLog
			ContextCreator.writeDMlog("Agent" + (i + 1) + " dangerTemp = " + dangerTemp + " criticalTemp = "
					+ criticalTemp + dangerTemp + " shutdownTemp = " + shutdownTemp);
		}

		// double startingMeasuredDepth = 0;
		double dangerTemp = DANGER_TEMP_CONST + (int) (Math.random() * ((CRITICAL_TEMP_CONST - DANGER_TEMP_CONST) + 1));
		// random value from 100 to 125

		double criticalTemp = CRITICAL_TEMP_CONST
				+ (int) (Math.random() * ((SHUTDOWN_TEMP_CONST - CRITICAL_TEMP_CONST - 10) + 1));
		// random value from 125 to 150

		// double shutdownTemp = criticalTemp + CRITICALITY_SCALE_CONST;
		double shutdownTemp = SHUTDOWN_TEMP_CONST + ((int) (Math.random() * 7)); // for
																					// now
																					// all
																					// have
																					// 150
		// shutdown temp

		UpperSensorAgent usa = new UpperSensorAgent(space, nextID, nextID + 1, nextID - 1, nbSensor, this,
				startingRunDepth, dangerTemp, criticalTemp, shutdownTemp, voteEnabled);

		// writing to DMLog
		ContextCreator.writeDMlog("Agent" + nextID + " dangerTemp = " + dangerTemp + " criticalTemp = " + criticalTemp
				+ dangerTemp + " shutdownTemp = " + shutdownTemp);

		context.add(usa);
		space.moveTo(usa, 5, getYCoordinates(startingRunDepth));
		nextID++;

		/*--------------CREATING THE FIELD AGENT-----------------*/
		FieldAgent fa = new FieldAgent(space, nextID, nextID - 1, coolingEnabled, this);
		nextID++;

		// writing to DMLog
		ContextCreator.writeDMlog("Field Agent Created");

		context.add(fa);
		space.moveTo(fa, 5, -10); // we place it at ground level

		/*--------------CREATING THE UPHOLE ACTUATORS-----------------*/
		for (int i = 0; i < nbActuator; i++) {
			int IDFA = fa.getIDFieldAgent(); // get the ID of the Field Agent
			ActuatorAgent uaa = new ActuatorAgent(space, nextID, false, IDFA, this, actuatorEffectiveness);
			context.add(uaa);
			nextID++;
			space.moveTo(uaa, 7, -10); // not shown

			// writing to DMLog
			ContextCreator.writeDMlog("Actuator" + (i + 1) + "created");
		}
		ContextCreator.writeDMlog("########################## Start of Run #########################");

		/*--------------CREATING THE AGENT THAT REPRESENTS THE GOAL-----------------*/
		GoalAgent ga = new GoalAgent();
		ga.setGoalDepth(depthGoal);
		context.add(ga);
		space.moveTo(ga, 5, getYCoordinates(depthGoal));

		/*--------------CREATING THE BACKGROUND-----------------*/
		for (int i = 0; i < 120; i++) {
			for (int j = 0; j < 11; j++) {
				BackgroundAgent bga = new BackgroundAgent();
				bga.setX(j);
				bga.setY(i);
				// we define the air tiles
				if (i <= 10 && i != 0)
					bga.setContent(false);
				else // all others are ground tiles
					bga.setContent(true);
				context.add(bga);
				space.moveTo(bga, j, -i);
			}
		}

		/*--------------CREATING DIFFERENT LAYERS-----------------*/

		if (this.numDifferentLayers > 0) {
			this.assignDifferentLayers();
		} else {
			// System.out.println("No different Layers");
		}

		return context;
	}

	/*--------------FUNCTIONS-----------------*/
	/**
	 * Increase the cooling of the equipments
	 * 
	 * @param cooling
	 *            : the number of °C the equipments are cooled down.
	 */
	public void coolDown(double cooling) {
		coolDown += cooling;

		// writing to DMLog
		ContextCreator.writeDMlog("CoolDown increase by " + cooling + " to be " + coolDown);

		System.out.println(coolDown);
		// writing to Cooling Log
		ContextCreator.writeCoolingLog(coolDown);

	}

	public void reduceCooling(double cooling) {
		coolDown -= cooling;

		// writing to DMLog
		ContextCreator.writeDMlog("CoolDown decreased by " + cooling + " to be " + coolDown);

		System.out.println(coolDown);
		// writing to Cooling Log
		ContextCreator.writeCoolingLog(coolDown);
	}

	/**
	 * Check if we reached the depth goal. If it is reached, ends the simulation
	 * with a message.
	 * 
	 * @param depth
	 *            : the depth compared to the goal
	 */
	public void checkDepthReached(double depth) {
		if (depth >= depthGoal) {
			double ticks = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			System.out.println("Depth goal reached in " + ticks + " minutes");
			RunEnvironment.getInstance().endRun();
		}
	}

	/**
	 * returns the Y coordinates of the TVD input
	 * 
	 * @param TVD
	 *            : the TVD of the agent going down
	 * @return a number in the scope [10-110]
	 */
	public double getYCoordinates(double TVD) {
		double yCoord = 10 + ((TVD / depthGoal) * 100);
		return -yCoord;
	}

	/**
	 * Get the true depth of an equipment given the angle of drilling and the
	 * measured depth
	 * 
	 * @param measuredDepth
	 * @return the true depth linked to the measured depth
	 */
	public double getTrueDepth(double measuredDepth) {
		double trueDepth = 0;
		double angle = ((this.drillingAngle) / 180) * 3.14159;
		trueDepth = measuredDepth * Math.cos(angle);
		checkDepthReached(trueDepth);
		return trueDepth;
	}

	/**
	 * Get the temperature from a TVD with the geothermal gradient, the surface
	 * temperature, and the cooling.
	 * 
	 * @param trueDepth
	 * @return the temperature associated to the TVD
	 */
	public double getTemperatureFromTVD(double trueDepth) {
		double temperature = 0;
		temperature = surfaceTemp + (geothermalGradient * trueDepth);
		temperature = temperature - coolDown;

		return temperature;
	}

	/**
	 * Lower the drilling speed for other rules
	 */
	public void changeDrillingSpeedRPM(int chosen) {
		// System.out.println("Current RPM = "+rotationsPerMinute);
		// System.out.println("Minimum RPM = " + minimimumRotationsPerMinute);
		// RunEnvironment.getInstance().pauseRun();
		// TO DO: modify to add the ability to perform 5 different levels of
		// power

		if (this.votingRuleChoice == 1) {

			rotationsPerMinute = (rotationsPerMinute * 0.90);

		} else {
			if (minimimumRotationsPerMinute > 50)
			rotationsPerMinute = minimimumRotationsPerMinute + (minimimumRotationsPerMinute - (chosen / 2)); // between
			else
				rotationsPerMinute = minimimumRotationsPerMinute + (minimimumRotationsPerMinute - (chosen)); // between	
			// 50
			// and
			// 100
		}
		// System.out.println("RPM reduced to " + rotationsPerMinute);

		// writing to DMLog
		ContextCreator.writeDMlog("New RPM after Voting is" + rotationsPerMinute);

		// writing to Cooling Log
		ContextCreator.writeVotingLog(chosen);

		if (rotationsPerMinute <= minimimumRotationsPerMinute) {
			System.out.println("Can't reduce RPM anymore");
			rotationsPerMinute = minimimumRotationsPerMinute;
		}
	}

	/**
	 * Increase the drilling speed
	 */
	public void increaseDrillingSpeed() {
		rotationsPerMinute = (rotationsPerMinute * 1.111111111); // the opposite
																	// of
																	// slowing
																	// down
		// writing to DMLog
		ContextCreator.writeDMlog("RPM increased to" + rotationsPerMinute);
	}

	/**
	 * Get the speed according to all the factors
	 * 
	 * @return the speed in m/min of the last minute
	 */
	public double getSpeed(double lastMeasuredDepth) {
		double speed = 0;
		// double K = 1;
		double K = 0.05; // as per values from Risha field in Jordan, ROP should
							// have the range of 0.1 to 10
		speed = K * getWOBFactor(weightOnBit, holeDiameter)
				* getRPMFactor(rotationsPerMinute, hardFormation, lastMeasuredDepth) * getFlowFactor();// speed
																										// =
																										// ROP
		// writing to DMLog
		// ContextCreator.writeDMlog("Speed is" + speed);

		// System.out.println("Speed = "+speed);
		return speed;
	}

	/**
	 * Get the Weight On Bit factor in the Speed equation
	 * 
	 * @param weightOnBit
	 *            : force applied to the tools from uphole
	 * @param holeDiameter
	 * @return the factor
	 */
	public double getWOBFactor(double weightOnBit, float holeDiameter) {
		double factor = 1;
		factor = (7.88 * weightOnBit) / holeDiameter;
		return factor;
	}

	/**
	 * Get the Rotations Per Minute factor in the Speed equation considers
	 * different layers in the formation
	 * 
	 * @param rotationsPerMinute
	 *            : Rotations per Minute from the bit (both uphole and downhole
	 *            rotation applied)
	 * @param hardFormation
	 *            : whether the formation is hard or soft
	 * 
	 * @param lastMeasuredDepth
	 *            : the last measured depth to help identify the layer
	 *            (soft/hard)
	 * 
	 * @return the factor
	 */
	public double getRPMFactor(double rotationsPerMinute, boolean hardFormation, double lastMeasuredDepth) {
		double factor = 1;
		double N = rotationsPerMinute;
		double a = (-100) / (N * N);

		// One layer in the formation
		if (this.numDifferentLayers == 0) {
			if (hardFormation)
				factor = (Math.exp(a) * Math.pow(N, 0.428)) + 0.2 * N * (1 - Math.exp(a));// hard
																							// function
			else
				factor = (Math.exp(a) * Math.pow(N, 0.75)) + 0.5 * N * (1 - Math.exp(a));// soft
																							// function
		} else {// Several layers in the formation
			if (hardFormation) {
				// Check if we are in a soft layer
				if (isSoftLayer(lastMeasuredDepth, hardFormation)) {
					factor = (Math.exp(a) * Math.pow(N, 0.75)) + 0.5 * N * (1 - Math.exp(a));// soft
																								// function
				} else {
					factor = (Math.exp(a) * Math.pow(N, 0.428)) + 0.2 * N * (1 - Math.exp(a));// hard
																								// function
				}
			} else {// soft formation
				// Check if we are in a hard layer
				if (!(isSoftLayer(lastMeasuredDepth, hardFormation))) {
					factor = (Math.exp(a) * Math.pow(N, 0.428)) + 0.2 * N * (1 - Math.exp(a));// hard
																								// function
				} else {
					factor = (Math.exp(a) * Math.pow(N, 0.75)) + 0.5 * N * (1 - Math.exp(a));// soft
																								// function
				}
			}
		}
		return factor;
	}

	/**
	 * Get the flow factor in the Speed equation
	 * 
	 * @return
	 */
	public double getFlowFactor() {
		return 1; // TODO : determine the flow factor
	}

	/**
	 * Get the signal delay as per the current meseured depth
	 * 
	 * @return
	 */
	public double getSignalDelay(double measuredDepth) {
		return measuredDepth / 50;
	}

	/**
	 * Assign soft/hard layers as per the entry of the user and put all layers
	 * as start/end depth in 'differentLayers' array from context creator class,
	 * A start/end depth of each layer in each line of the array if the user
	 * checked the hard formation box then this array will hold the depths of
	 * the soft formation, and vice versa
	 * 
	 * @return
	 */
	public void assignDifferentLayers() {
		this.differentLayers = new double[this.numDifferentLayers][2];

		double[] layers = new double[numDifferentLayers];
		double[] interruptions = new double[numDifferentLayers + 1];

		double sumOfLayers = 0;
		for (int iterator = 0; iterator < this.numDifferentLayers - 1; iterator++) {
			if (iterator == 0)
				layers[iterator] = Math.random() * heightDifferentLayers;
			else
				layers[iterator] = Math.random() * (heightDifferentLayers - sumOfLayers);
			// layer2 = layer1 + (Math.random() * (heightDifferentLayers -
			// layer1));
			sumOfLayers = sumOfLayers + layers[iterator];

		}
		layers[numDifferentLayers - 1] = heightDifferentLayers - sumOfLayers;

		double sumOfInterruptions = 0;
		for (int iterator = 0; iterator < this.numDifferentLayers; iterator++) {
			if (iterator == 0)
				interruptions[iterator] = Math.random()
						* (this.depthGoal - this.startingRunDepth - heightDifferentLayers);
			else
				interruptions[iterator] = Math.random()
						* (this.depthGoal - this.startingRunDepth - heightDifferentLayers - sumOfInterruptions);
			// layer2 = layer1 + (Math.random() * (heightDifferentLayers -
			// layer1));
			sumOfInterruptions = sumOfInterruptions + interruptions[iterator];
		}
		interruptions[numDifferentLayers] = (this.depthGoal - this.startingRunDepth - heightDifferentLayers)
				- sumOfInterruptions;

		double zones; // it includes the min and max value for each layer
		zones = this.depthGoal - this.startingRunDepth;
		double startCurrentZone;
		double endCurrentZone;

		for (int iterator = 0; iterator < this.numDifferentLayers; iterator++) {

			if (iterator == 0) {
				this.differentLayers[0][0] = this.startingRunDepth + interruptions[0];
				this.differentLayers[0][1] = this.startingRunDepth + interruptions[0] + layers[0];
			} else {
				this.differentLayers[iterator][0] = interruptions[iterator] + this.differentLayers[iterator - 1][1];
				this.differentLayers[iterator][1] = this.differentLayers[iterator][0] + layers[iterator];
			}
			System.out.println("zones in");
		}

		System.out.println("zones out");
	}

	/**
	 * Return true if formation is soft for this depth, and false if formation
	 * is hard
	 * 
	 * @param Current
	 *            Depth
	 * @return True/False as per formation soft/hard
	 */
	public boolean isSoftLayer(double depth, boolean hardFormation) {

		for (int iterator = 0; iterator < this.numDifferentLayers; iterator++) {
			if (depth >= differentLayers[iterator][0] && depth <= differentLayers[iterator][1])
				return (hardFormation);
		}
		return !(hardFormation);
	}

	static public void writeLog(SensorAgent agent) {

		try {
			ContextCreator.logWriter.append(Integer.toString(agent.getIDSensorAgent()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getMeasuredDepth()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getTrueDepth()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getTemperature()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getDangerTemp()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getCriticalTemp()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter.append(Double.toString(agent.getShutdownTemp()));
			ContextCreator.logWriter.append(',');
			ContextCreator.logWriter
					.append(Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
			ContextCreator.logWriter.append('\n');

			switch (agent.getIDSensorAgent()) {
			case 1:
				ContextCreator.logWriter1.append(Integer.toString(agent.getIDSensorAgent()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getMeasuredDepth()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getTrueDepth()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getTemperature()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getDangerTemp()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getCriticalTemp()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1.append(Double.toString(agent.getShutdownTemp()));
				ContextCreator.logWriter1.append(',');
				ContextCreator.logWriter1
						.append(Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
				ContextCreator.logWriter1.append('\n');
				break; // optional
			case 2:
				ContextCreator.logWriter2.append(Integer.toString(agent.getIDSensorAgent()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getMeasuredDepth()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getTrueDepth()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getTemperature()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getDangerTemp()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getCriticalTemp()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2.append(Double.toString(agent.getShutdownTemp()));
				ContextCreator.logWriter2.append(',');
				ContextCreator.logWriter2
						.append(Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
				ContextCreator.logWriter2.append('\n');
				break; // optional
			case 3:
				ContextCreator.logWriter3.append(Integer.toString(agent.getIDSensorAgent()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getMeasuredDepth()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getTrueDepth()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getTemperature()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getDangerTemp()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getCriticalTemp()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3.append(Double.toString(agent.getShutdownTemp()));
				ContextCreator.logWriter3.append(',');
				ContextCreator.logWriter3
						.append(Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
				ContextCreator.logWriter3.append('\n');
				break; // optional
			case 4:
				ContextCreator.logWriter4.append(Integer.toString(agent.getIDSensorAgent()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getMeasuredDepth()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getTrueDepth()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getTemperature()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getDangerTemp()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getCriticalTemp()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4.append(Double.toString(agent.getShutdownTemp()));
				ContextCreator.logWriter4.append(',');
				ContextCreator.logWriter4
						.append(Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
				ContextCreator.logWriter4.append('\n');
				break; // optional

			// You can have any number of case statements.
			default: // Optional
				// Statements
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Writing log file problem");
		}
	}

	// writing to DMLog
	static public void writeDMlog(String messageLine) {

		try {
			int temp = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			ContextCreator.DMlogWriter.append(temp + "_" + messageLine);
			ContextCreator.DMlogWriter.append('\n');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("DMlog file writing error");
			e.printStackTrace();
		}
	}

	// writing to Cooling Log
	static public void writeCoolingLog(Double coolingVal) {

		int temp = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

		coolingLogString.add(Integer.toString(temp));
		coolingLogString.add(",");
		coolingLogString.add(Double.toString(coolingVal));
		coolingLogString.add("\n");
	}

	// writing to Voting Log
	static public void writeVotingLog(int votingPowerLevelChoice) {

		try {

			int temp = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			ContextCreator.votingLogWriter.append(Integer.toString(temp));
			ContextCreator.votingLogWriter.append(',');
			ContextCreator.votingLogWriter.append(Integer.toString(votingPowerLevelChoice));
			ContextCreator.votingLogWriter.append('\n');

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Cooling Log file writing error");
			e.printStackTrace();
		}
	}

	// writing to Cooling Log
	static public void writeCoolingLogAtEnd() {
		try {
			for (String iterator : ContextCreator.coolingLogString) {
				ContextCreator.coolingLogWriter.append(iterator);
			}
			ContextCreator.coolingLogWriter.flush();
			ContextCreator.coolingLogWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Cooling Log file writing error");
			e.printStackTrace();
		}
	}

	static public void closeLog() {

		 try {
//		 ContextCreator.logWriter.flush();
//		 ContextCreator.logWriter.close();
//		 ContextCreator.logWriter1.flush();
//		 ContextCreator.logWriter1.close();
//		 ContextCreator.logWriter2.flush();
//		 ContextCreator.logWriter2.close();
//		 ContextCreator.logWriter3.flush();
//		 ContextCreator.logWriter3.close();
//		 ContextCreator.logWriter4.flush();
//		 ContextCreator.logWriter4.close();

		 ContextCreator.votingLogWriter.flush();
		 ContextCreator.votingLogWriter.close();
//		 ContextCreator.DMlogWriter.flush();
//		 ContextCreator.DMlogWriter.close();
		 
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 System.out.println("Closing log file problem");
		 }

	}
}
