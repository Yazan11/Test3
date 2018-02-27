package cps2Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.lowagie.text.pdf.hyphenation.TernaryTree.Iterator;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.continuous.ContinuousSpace;

/**
 * The class used to represent the sensor array in each equipment (one agent /
 * equipment). Their goal is the preservation of the equipment through the
 * control of the temperature, but with the minimal cost in time to drill.
 * 
 * @author Robin Vanet, Yazan Mualla
 * @version 2.0
 * @since 2017-04-11
 *
 */
public class SensorAgent extends Agent {

	/*--------------CONSTANS-----------------*/
	final int MAX_POWER_LEVEL = 100;
	final int NUM_LEVELS = 6;

	/*--------------VARIABLES-----------------*/
	// informations on the sensor itself
	protected int IDSensorAgent;
	protected int neighborUp;
	protected int neighborDown;
	protected ContextCreator context;
	protected double dangerTemp;
	protected double criticalTemp;
	protected double shutdownTemp;

	// voting mechanisms variables
	protected boolean voteEnabled;
	protected int nbSensor;
	protected int voteResult; // 0 if no vote result, 1 if true, -1 if false
	protected ArrayList<Boolean> voteList;
	protected ArrayList<Integer> voterIDList;
	protected boolean voting;
	protected boolean voteFinished;
	protected boolean actionAlreadyTaken;

	// New Proposed voting rules
	// TO DO: define the ballot here
	protected ArrayList<Ballot> ballotList;
	// protected Ballot ballot;
	protected int voteResultChosen;
	protected VotingSystem system;

	// measures
	protected double measuredDepth;
	protected double trueDepth;
	protected double temperature;

	// message mechanisms variables
	protected boolean messageFATooHot = false;
	protected boolean messageFASlowDown = false;
	protected boolean speedDrill = false;

	/*--------------GETTERS AND SETTERS-----------------*/

	public double getDangerTemp() {
		return dangerTemp;
	}

	public void setDangerTemp(double dangerTemp) {
		this.dangerTemp = dangerTemp;
	}

	public double getCriticalTemp() {
		return criticalTemp;
	}

	public void setCriticalTemp(double criticalTemp) {
		this.criticalTemp = criticalTemp;
	}

	public double getShutdownTemp() {
		return shutdownTemp;
	}

	public void setShutdownTemp(double shutdownTemp) {
		this.shutdownTemp = shutdownTemp;
	}

	public double getMeasuredDepth() {
		return measuredDepth;
	}

	public void setMeasuredDepth(double measuredDepth) {
		this.measuredDepth = measuredDepth;
	}

	public double getTrueDepth() {
		return trueDepth;
	}

	public void setTrueDepth(double trueDepth) {
		this.trueDepth = trueDepth;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public boolean isSpeedDrill() {
		return speedDrill;
	}

	public void setSpeedDrill(boolean speedDrill) {
		this.speedDrill = speedDrill;
	}

	public int getIDSensorAgent() {
		return IDSensorAgent;
	}

	public ArrayList<Boolean> getVoteList() {
		return voteList;
	}

	public ArrayList<Integer> getVoterIDList() {
		return voterIDList;
	}

	public int getVoteResult() {
		return voteResult;
	}

	public int voteResultChosen() {
		return voteResultChosen;
	}

	public ArrayList<Ballot> getBallotList() {
		return ballotList;
	}

	/*--------------CONSTRUCTOR-----------------*/
	public SensorAgent(ContinuousSpace<Agent> space, int IDSensorAgent, int neighborUp, int neighborDown, int nbSensor,
			ContextCreator context, double measuredDepth, double dangerTemp, double criticalTemp, double shutdownTemp,
			boolean voteEnabled) {
		this.space = space;
		this.IDSensorAgent = IDSensorAgent;
		this.neighborUp = neighborUp;
		this.neighborDown = neighborDown;
		this.voting = false;
		this.voteResult = 0;
		this.nbSensor = nbSensor;
		this.context = context;
		this.measuredDepth = measuredDepth;
		this.dangerTemp = dangerTemp;
		this.criticalTemp = criticalTemp;
		this.shutdownTemp = shutdownTemp;
		this.voteEnabled = voteEnabled;
		// this.ballot = new Ballot();
		this.voteResultChosen = 0;
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
		measuredDepth += speed;
		trueDepth = context.getTrueDepth(measuredDepth);
		temperature = context.getTemperatureFromTVD(trueDepth);
		
		
		// writing to DMLog
		ContextCreator.writeDMlog("SensorAgent: " + this.IDSensorAgent + " speed: " + Math.floor(speed * 100) / 100  + " MD: " + Math.floor(measuredDepth * 100) / 100 
					+ " TVD: " +  Math.floor(trueDepth * 100) / 100  + " Temp: " +  Math.floor(temperature * 100) / 100  + " Danger: " + dangerTemp + " Critical: "
					+ criticalTemp + " Shutdown: " + shutdownTemp);
		
		ContextCreator.writeLog(this);
		space.moveTo(this, 5, context.getYCoordinates(trueDepth)); // moving the
																	// agent on
																	// the
																	// display

		// can be used to display what the agent knows each tick.
		// System.out.println("measuredDepth of Agent #" + IDSensorAgent + " is
		// " + measuredDepth + " meters downhole");
		// System.out.println("trueDepth of Agent #" + IDSensorAgent + " is " +
		// trueDepth + " meters downhole");
		// System.out.println("temperature of Agent #" + IDSensorAgent + " is "
		// + temperature + "°C");

		if (temperature >= shutdownTemp) {
			// end the simulation
			double ticks = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			System.out.println("Failure! Agent #" + IDSensorAgent + " overheated at "
					+ (Math.floor(trueDepth * 100) / 100) + "m trueDepth and " + (Math.floor(measuredDepth * 100) / 100)
					+ "m mesuredDepth in " + ticks + " minutes for temperature " + (Math.floor(temperature * 100) / 100)
					+ " and dangerTemp " + (Math.floor(dangerTemp * 100) / 100));

			ContextCreator.writeCoolingLogAtEnd();
			ContextCreator.closeLog();
			RunEnvironment.getInstance().endRun();
		} else if (temperature >= criticalTemp) {
			// System.out.println("Agent #" + IDSensorAgent+ " is at "+
			// temperature+"°C of "+criticalTemp+" and is starting a vote!");
			if (voteEnabled) {
				
				
				// writing to DMLog
				ContextCreator.writeDMlog("Voting started by SensorAgent: " + this.IDSensorAgent);
				
				startVote();
			}
		} else if (temperature >= dangerTemp) {
			// send a message to the FA
			messageFATooHot = !messageFATooHot;
		}

		// grid.moveTo((Agent)this, (int)trueDepth,0);
	}

	/**
	 * Clean the internal memory of previous vote to avoid results from another
	 * vote to spill in the new one, then requesting the others to vote.
	 */
	public void startVote() // when we start a vote, we create a new ArrayList
							// of boolean that we fill with nulls if the voting
							// is simple (plurality)
	// or we create ballot if the voting is otherwise
	{
		// int votingRuleChoice = context.getVotingRuleChoice();
		// if (votingRuleChoice==1)
		voteList = new ArrayList<Boolean>();
		voterIDList = new ArrayList<Integer>();
		ballotList = new ArrayList<Ballot>();
		voting = !voting;
	}

	/**
	 * The function used for the voting mechanism. Activated every time an agent
	 * requests the others to vote (with the "voting" boolean), each other
	 * sensors check if the asking one is a neighbor, and if he is they either
	 * vote is not everyone voted, or extract the result.
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor).
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "voting", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void vote(SensorAgent sensorAgent) {
		if (validSender(sensorAgent.getIDSensorAgent())) { // if we receive a
															// voting request
															// from a valid
															// neighbor
			// Done: new voting rules
			voteResultChosen = 0;
			voteResult = 0; // we forget the previous vote result
			// get the current lists in the memory of the requesting neighbor
			voteList = sensorAgent.getVoteList();
			voterIDList = sensorAgent.getVoterIDList();
			// Done: Copy the ballot from the other agent
			ballotList = sensorAgent.getBallotList();

			// we check if it is filled
			if (voteList.size() != nbSensor && !voterIDList.contains(IDSensorAgent))// if
																					// it
																					// isn't
																					// finished
																					// and
																					// we
																					// haven't
																					// already
																					// voted,
																					// we
																					// update
																					// the
																					// vote
			{
				boolean vote;

				Ballot ownBallot = new Ballot();
				if (temperature >= criticalTemp) {
					vote = true;
					if (context.getVotingRuleChoice() != 1) {
						// DONE: Fill the candidates as per this sensor agent
						// decision model
						// TO DO: decision model function
						// 125 to 130: 20% of actuator power
						// 130 to 135: 40% of actuator power
						// 135 to 140: 60% of actuator power
						// 140 to 145: 80% of actuator power
						// 145 to 150: 100% of actuator power

						ArrayList<String> candidatesChosen = new ArrayList<String>();
						double rangeTemp = shutdownTemp - temperature;
						if (rangeTemp == 0) { // very high temp
							ownBallot.setBallot("100 , 80 , 60 , 40 , 20 , 0");
						}

						else if (rangeTemp == 25) { // low temp
							ownBallot.setBallot("20 , 0 , 40 , 60 , 80 , 100");
						} else {
							// reverse the scale (25 difference which means less
							// temp will be 0, and 0 difference which means
							// high temp will be 25
							double reversedRangeTemp;
							// if the range of temperature is bigger than 25
							// becasue by luck the shut down temp is higher than
							// 150 and the critical temp is almost 125
							if (rangeTemp > 25) {
								reversedRangeTemp = 0.1;
							} else {
								reversedRangeTemp = context.SHUTDOWN_TEMP_CONST - context.CRITICAL_TEMP_CONST - rangeTemp;
							}

							// convert the scale to be (0-100)
							reversedRangeTemp = MAX_POWER_LEVEL / (context.SHUTDOWN_TEMP_CONST - context.CRITICAL_TEMP_CONST) * reversedRangeTemp;

							LinkedList<Integer> powerLevels = definePowerLevels(MAX_POWER_LEVEL, NUM_LEVELS);
							// String powerLevelsString =
							// powerLevels.toString().substring(1,
							// powerLevels.toString().indexOf("]"));

							// System.out.println(powerLevelsString);

							// Construct the ballot: Adding all power levels as
							// per
							// the decision model of the tool

							// loop for all candidates (powerLevels): while not
							// empty
							while (!powerLevels.isEmpty()) {
								// while loop till we found the best position:
								// if
								// the
								// value is 47 then the loop will stop between
								// 40
								// and
								// 60, and then we add 40 then 60 to candidates
								// then
								// we get out
								// java.util.Iterator<Integer> iterator =
								// powerLevels. iterator();
								int index = -1;
								int minVal = 0;
								int maxVal = 0;
								try {

									// indexes

									for (Integer item : powerLevels) {
										index++;
										if (reversedRangeTemp < item.intValue()) {
											break;
										}

									}
									if (index > 0 && index <= powerLevels.size()) {
										try {
											minVal = powerLevels.remove(index - 1).intValue();
											maxVal = powerLevels.remove(index - 1).intValue();
										} catch (Exception e) {
											System.out.println("Exception caught while removing from powerLevels list");
											e.printStackTrace();
										}

									} else if (powerLevels.size() > 1)// index
																		// ==0
									{
										minVal = powerLevels.remove(0).intValue();
										maxVal = powerLevels.remove(0).intValue();
									}

									// iterators

									// for (Integer item : powerLevels) {
									// if (reversedRangeTemp < item.intValue())
									// {
									// maxVal = item.intValue();
									//
									// if (index < powerLevels.size() && index >
									// 0){
									// minVal =
									// powerLevels.get(index-1).intValue();
									// powerLevels.remove(index-1);
									// }
									// }
									// index++;
									// }

									// choose the order of adding of minVal and
									// maxVal
									int highPart = maxVal - (int) reversedRangeTemp;
									int lowPart = (int) reversedRangeTemp - minVal;
									if (highPart > lowPart) {
										candidatesChosen.add(String.valueOf(minVal));
										candidatesChosen.add(String.valueOf(maxVal));
									} else {
										candidatesChosen.add(String.valueOf(maxVal));
										candidatesChosen.add(String.valueOf(minVal));
									}
								}

								catch (Exception e) {
									System.out.println("Exception caught while constructing a Ballot in Sensor agent");
									e.printStackTrace();
								}
								// double ratioTemp = reversedRangeTemp / ;
							}
							ownBallot.setBallot(candidatesChosen);

						}
					}
				} else {// temperature < criticalTemp
					vote = false;
					if (context.getVotingRuleChoice() != 1) {
						ownBallot.setBallot("0 , 20 , 40 , 60 , 80 , 100");
					}
				}

				voteList.add(vote);
				voterIDList.add(IDSensorAgent);
				// adding the ballot to the ballotList
				if (context.getVotingRuleChoice() != 1) {
					ballotList.add(ownBallot);
					// if the tool is important vote again (give weight of 2)
					if (this.IDSensorAgent == context.toolVotingWeight) {
						ballotList.add(ownBallot);
						ballotList.add(ownBallot);
					}
					//System.out.println("OwnBallot is: ");
					//ownBallot.printBallot();
					
					// writing to DMLog
					ContextCreator.writeDMlog("SensorAgent: " + this.IDSensorAgent + " Ballot: " + ownBallot.toString());
					
				}

				// we pass the vote to the others
				voting = !voting;
			} else if (voteList.size() == nbSensor)// if everybody voted, we
													// extract the result
			{
				extractVoteResult();
			}
		}
	}

	/**
	 * Extract the result of the vote once the list is full (everyone voted).
	 * The presence of a veto or not can be set here.
	 */
	public void extractVoteResult() {
		
		
		// TO DO:
		// 1-Check voting rule chosen
		// 2-Extract result as per the voting choice and put only one option to
		// be excuted in voteResultChosen
		// 20: 20%
		// 40: 40%
		// 60: 60%
		// 80: 80%
		// 100: 100%
		if (context.getVotingRuleChoice() != 1) {
			switch (context.getVotingRuleChoice()) {// 1 is for plurality with
													// one candidate (On/Off)
			// plurality with several candidates
			case 2:
				system = new Plurality(ballotList.toArray(new Ballot[ballotList.size()]));
				//System.out.println("Voting rule is Plurality");
				break;
			case 3:
				system = new Condorcet(ballotList.toArray(new Ballot[ballotList.size()]));
				system.computeWinner();
				//System.out.println("Voting rule is Condorcet");
				break;
			case 4:
				system = new Borda(ballotList.toArray(new Ballot[ballotList.size()]));

				//System.out.println("Voting rule is Borda");
				break;
			case 5:
				system = new Condorcet(ballotList.toArray(new Ballot[ballotList.size()]));
				if (system.getWinner().length() == 0) {
					system = new Borda(ballotList.toArray(new Ballot[ballotList.size()]));
					//System.out.println("Voting rule is ProposedRule:Borda");
					//System.out.println("Winner: " + system.getWinner());
				}

				else {
					//System.out.println("Voting rule is ProposedRule:Condorcet");
					//System.out.println("Winner: " + system.getWinner());
				}
				break;
			}
			// system.computeWinner();
			voteResultChosen = Integer.parseInt(system.computeWinner());
			
			
			// writing to DMLog
			ContextCreator.writeDMlog("voteResultChosen: " + this.voteResultChosen);
			
			//System.out.println("voteResultChosen: " + voteResultChosen);
			// if 5 (our proposed rule) and the result is tie, check the Borda
			// type
			// if (context.getVotingRuleChoice() != 1)

		}

		
		int trueVotes = 0;
		int falseVotes = 0;
		for (int i = 0; i < voteList.size(); i++) // we count all the votes
		{
			if (voteList.get(i) == true)
				trueVotes++;
			else
				falseVotes++;
		}
		if (trueVotes > falseVotes) {
			voteResult = 1;
		} else {
			voteResult = -1;
		} // we tell the results to the neighbors


		voteFinished = !voteFinished;
	}

	/**
	 * Get the result of a vote from another sensor agent, then passing it after
	 * checking if the other agent is a neighbor.
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor).
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "voteFinished", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void getVoteResult(SensorAgent sensorAgent) {
		if (validSender(sensorAgent.getIDSensorAgent())) { // if we receive a
															// valid answer from
															// a neighbor
			// System.out.println("Agent " + IDSensorAgent+ " received the
			// result "+sensorAgent.getVoteResult()+" from
			// Agent"+sensorAgent.getIDSensorAgent());
			int vote = sensorAgent.getVoteResult();
			int voteResultChosenfromOtherAgent = sensorAgent.voteResultChosen();

			// TO DO: should check my voteResultChosen with the one passed by
			// the triggering agent

			if (vote != voteResult) // if we didn't already have the vote result
			{
				voteResultChosen = voteResultChosenfromOtherAgent;
				voteResult = vote; // we take the result and pass it along
				voteFinished = !voteFinished;

			}

			// of the if?!
			// else we do nothing because the information has already spread
		}
	}

	/**
	 * Check if the sender of a message is a neighbor.
	 * 
	 * @param IDSender
	 *            : the ID of the sensor agent transmiting the message.
	 * @return true if the agent is a neighbor, else if it is self or a distant
	 *         one.
	 */
	public boolean validSender(int IDSender) // know if the sender is a neighbor
	{
		if (IDSender == neighborUp || IDSender == neighborDown)
			return true;
		else // if the sender is the receiver itself or a distant Agent
			return false;
	}

	/**
	 * Every time the field "voteResult" of one Sensor Agent changes, this
	 * function activates. The if only works if the Sensor Agent is the most
	 * downhole (id = 2) in that case, it will detect its own change and will
	 * lower the drilling speed.
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor).
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "voteResult", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void actionTaken(SensorAgent sensorAgent) {

		// TO DO: check voteResultChosen and voteResult

		if (sensorAgent.getIDSensorAgent() == IDSensorAgent && IDSensorAgent == 2 && voteResult == 1
				&& actionAlreadyTaken == false) { // when the most downhole
													// SensorAgent realise the
													// vote is a yes
			actionAlreadyTaken = true;
			int voteResultChosenfromOtherAgent = sensorAgent.voteResultChosen();
			
			
			// writing to DMLog
			ContextCreator.writeDMlog("ChangeDrillingSpeed as per the voting choice: " + voteResultChosenfromOtherAgent + " call from Sensor agent" + this.IDSensorAgent);
			
			context.changeDrillingSpeedRPM(voteResultChosenfromOtherAgent);
			messageFASlowDown = !messageFASlowDown; // we send a message up-hole
													// to notify that the drill
													// was slowed down
		}
	}

	/**
	 * Send the message "temperature is getting too hot for one equipment" to
	 * everyone, to reach the FA. Only the direct upper level will listen and
	 * relay the call.
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor).
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "messageFATooHot", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageFATooHot(SensorAgent sensorAgent) {
		if (sensorAgent.getIDSensorAgent() == (IDSensorAgent - 1)) // if we
																	// receive a
																	// message
																	// from the
																	// SA under
		{
			messageFATooHot = !messageFATooHot;
		}
	}

	/**
	 * Send the message "the drill was slowed down" to everyone, to reach the
	 * FA. Only the direct upper level will listen and relay the call.
	 * 
	 * @param sensorAgent
	 *            : the sensorAgent sending the message. Used to check if the
	 *            agent is a valid one (if it is a neighbor).
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "messageFASlowDown", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageFASlowDown(SensorAgent sensorAgent) {
		if (sensorAgent.getIDSensorAgent() == (IDSensorAgent - 1)) // if we
																	// receive a
																	// message
																	// from the
																	// SA under
		{
			messageFASlowDown = !messageFASlowDown;
		}
	}

	// No need for this function as the the field agent should be able to
	// communicate with context directly
	/**
	 * Watch UpperSensorAgent for the order to increase the drilling speed. Only
	 * the next sensor in the array will transmit the message, the others will
	 * ignore it.
	 * 
	 * @param upperSensorAgent
	 */
	@Watch(watcheeClassName = "cps2Project.UpperSensorAgent", watcheeFieldNames = "speedDrill", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageSASpeedDrill(UpperSensorAgent upperSensorAgent) {
		if (upperSensorAgent.getIDSensorAgent() == (IDSensorAgent + 1)) // if
																		// the
																		// sender
																		// is
																		// valid
		{
			if (IDSensorAgent == 2) // if we are the most down-hole
			{
				
				// writing to DMLog
				ContextCreator.writeDMlog("increaseDrillingSpeed by call from Sensor agent" + this.IDSensorAgent);
			
				context.increaseDrillingSpeed();
			} else {
				speedDrill = !speedDrill;
			}
		}
	}

	// No need for this function as the the field agent should be able to
	// communicate with context directly
	/**
	 * Watch other SensorAgents for the order to increase the drilling speed.
	 * Only the next sensor in the array will transmit/use the message, the
	 * others will ignore it.
	 * 
	 * @param upperSensorAgent
	 */
	@Watch(watcheeClassName = "cps2Project.SensorAgent", watcheeFieldNames = "speedDrill", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void messageSASpeedDrill(SensorAgent sensorAgent) {
		if (sensorAgent.getIDSensorAgent() == (IDSensorAgent + 1)) {
			if (IDSensorAgent == 2) // if we are at the most down-hole agent
			{
				// writing to DMLog
				ContextCreator.writeDMlog("increaseDrillingSpeed by call from Sensor agent" + this.IDSensorAgent);
				context.increaseDrillingSpeed();
			} else {
				speedDrill = !speedDrill;
			}
		}
	}

	/**
	 * Retrns the power levels of the downhole actuators to be used by the
	 * sesnor agents in the voting mechanism
	 * 
	 * @param double
	 *            maxLevel, int numLevels
	 */
	public LinkedList<Integer> definePowerLevels(int maxLevel, int numLevels) {
		LinkedList<Integer> powerLevels = new LinkedList<Integer>();

		int stepLevels = maxLevel / (numLevels - 1);

		int counter = 0;
		boolean addition = false;
		while (counter < numLevels) {
			addition = powerLevels.add((stepLevels * counter));

			counter++;
		}
		return powerLevels;

	}

}
