package cps2Project;

import java.io.IOException;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;

/**
 * 
 * Upper-most SensorAgent, in direct contact with the Field Agent Derived from
 * the sensor class
 * 
 * @author Robin Vanet, Yazan Mualla
 * @version 2.0
 * @since 2017-04-11
 *
 */

public class UpperSensorAgent extends SensorAgent {

	/*--------------VARIABLES-----------------*/
	// variables used to send a message to the FA up-hole
	protected int nextTooHotMessage = -1;
	protected boolean messageFATooHot = false;
	protected int nextSlowDownMessage = -1;
	protected boolean messageFASlowDown = false;

	// variables used to send messages to the SA down-hole
	protected boolean speedDrill = false;

	/*--------------CONSTRUCTOR-----------------*/
	public UpperSensorAgent(ContinuousSpace<Agent> space, int IDSensorAgent, int neighborUp, int neighborDown,
			int nbSensor, ContextCreator context, double measuredDepth, double dangerTemp, double criticalTemp,
			double shutdownTemp, boolean voteEnabled) {
		super(space, IDSensorAgent, neighborUp, neighborDown, nbSensor, context, measuredDepth, dangerTemp,
				criticalTemp, shutdownTemp, voteEnabled);
	}

	/*--------------FUNCTIONS-----------------*/
	/**
	 * Method used at each tick (as defined in the Agent class)
	 */
	@Override
	public void compute() {
		actionAlreadyTaken = false; // this boolean is a quickfix to avoid
									// having several action taken from the same
									// vote. With that, we can only have one
									// vote/tick (1 vote/min)
		double speed = context.getSpeed(measuredDepth);

		// we update the MD then ask for TVD and temp
		measuredDepth += speed;
		trueDepth = context.getTrueDepth(measuredDepth);
		temperature = context.getTemperatureFromTVD(trueDepth);
		
		// writing to DMLog
		ContextCreator.writeDMlog("UpperSensorAgent: " + this.IDSensorAgent + " speed: " + Math.floor(speed * 100) / 100  + " MD: " + Math.floor(measuredDepth * 100) / 100 
					+ " TVD: " +  Math.floor(trueDepth * 100) / 100  + " Temp: " +  Math.floor(temperature * 100) / 100  + " Danger: " + dangerTemp + " Critical: "
					+ criticalTemp + " Shutdown: " + shutdownTemp);

		space.moveTo(this, 5, context.getYCoordinates(trueDepth)); // moving the
																	// agent on
																	// the
																	// display

		if (temperature >= shutdownTemp) {
			// end the simulation
			System.out.println("Failure! Agent #" + IDSensorAgent + " overheated.");
			RunEnvironment.getInstance().endRun();
		} else if (temperature >= criticalTemp) {
			if (voteEnabled) {

				// writing to DMLog
				ContextCreator.writeDMlog("Voting started by Upper SensorAgent: " + this.IDSensorAgent);

				startVote();
			}
		} else if (temperature >= dangerTemp) {
			// send a message to the FA
			messageFATooHot(this);
		}

		// if we reached the waiting time for the "Agent too hot" message
		if (nextTooHotMessage != -1) {
			if (nextTooHotMessage == 0)
				messageFATooHot = !messageFATooHot;
			nextTooHotMessage--; // if we reached , back to -1
		}

		// if we reached the waiting time for the "Drill slowed down" message
		if (nextSlowDownMessage != -1) {
			if (nextSlowDownMessage == 0)
				messageFASlowDown = !messageFASlowDown;
			nextSlowDownMessage--; // if we reached , back to -1
		}

	}

	/**
	 * Watcher on the other SensorAgents to know when to start the countdown to
	 * send a message. When the countdown reach 0, one hour has passed, the
	 * message has reached the surface and the message is passed to the
	 * FieldAgent (see the compute() function).
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor)
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "messageFATooHot", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageFATooHot(SensorAgent sensorAgent) {
		if ((sensorAgent.getIDSensorAgent() == (IDSensorAgent - 1) || sensorAgent.getIDSensorAgent() == IDSensorAgent)
				&& nextTooHotMessage == -1) // if we receive a message from the
											// SA or self under and we don't
											// already have a message in the
											// queue
		{
			// define signal delay
			nextTooHotMessage = (int) context.getSignalDelay(measuredDepth);
		}
	}

	/**
	 * Watcher on the other SensorAgents to know when to start the countdown to
	 * send a message. When the countdown reach 0, one hour has passed, the
	 * message has reached the surface and the message is passed to the
	 * FieldAgent (see the compute() function).
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor)
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "messageFASlowDown", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageFASlowDown(SensorAgent sensorAgent) {
		if ((sensorAgent.getIDSensorAgent() == (IDSensorAgent - 1) || sensorAgent.getIDSensorAgent() == IDSensorAgent)
				&& nextSlowDownMessage == -1) // if we receive a message from
												// the SA or self under and we
												// don't already have a message
												// in the queue
		{
			nextSlowDownMessage = 60;
		}
	}

	// /**
	// * Watcher on the FieldAgent to know when to pass the message to speed the
	// * drill up.
	// *
	// * @param fieldAgent
	// * : the fieldAgent sending the message. Can be used to verify if
	// * the right FieldAgent is talking (for example in a several-well
	// * case).
	// */
	// @Watch(watcheeClassName = "cps2Project.FieldAgent", watcheeFieldNames =
	// "speedDrill", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	// public void messageSASpeedDrill(FieldAgent fieldAgent) {
	// speedDrill = !speedDrill; // we just relay the message
	// }

}
