package optimization.swarmBasedSolutions;

import java.util.HashMap;

import nodes.Optimizer;
import optimization.BasicObject;
import performance.EntropicRelevanceCalculator.BackGroundType;


public class FoodSource extends BasicObject {
    private int limit; // To keep track of abandonment
            

    public FoodSource(double[] solution,int id, HashMap<String, Double> eventLog, HashMap<String, Character> action,BackGroundType bkgt) {
    	this.solution = solution;
    	this.limit = 0;
    	setEdgeNode(new Optimizer(id,action.size(),eventLog,bkgt));
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public void resetFoodSource(double[] solution) {
    	this.solution = solution;
    	this.limit = 0;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public int getLimit() {
		return limit;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public void incrementLimit() {
		limit++;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public void resetLimit() {
    	limit = 0;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

}