package cps2Project;

/**
 * This class is only used to represent the goal the agents must reach to end the simulation.
 * 
 * @author Robin Vanet, Yazan Mualla
 * @version 2.0
 * @since   2017-04-11
 *
 */
public class GoalAgent  extends Agent{
	
	//the goal to reach
	protected double goalDepth;
	
	@Override
	public void compute() {
		// TODO Auto-generated method stub
		
	}
	
	public void setGoalDepth(double goalDepth)
	{
		this.goalDepth=goalDepth;
	}

}
