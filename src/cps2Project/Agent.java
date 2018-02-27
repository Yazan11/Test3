package cps2Project;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;

/**
 * The class which all agents classes are derived from. Maily used to have a
 * compute() function in each agent.
 * 
 * @author Robin Vanet, Yazan Mualla
 *
 */
public abstract class Agent {

	/*--------------VARIABLES-----------------*/
	ContinuousSpace<Agent> space;
	// la grille servira � simplifier le nombre de calculs servant a savoir si
	// un taxi est proche ou pas

	/*--------------CONSTRUCTOR-----------------*/
	public Agent() {

	}

	/*--------------FUNCTIONS-----------------*/
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	// for every class derived from agent, each tick the compute() function will
	// start
	public abstract void compute();

}
