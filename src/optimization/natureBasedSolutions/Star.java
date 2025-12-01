package optimization.natureBasedSolutions;

import java.util.HashMap;
import java.util.Random;

import nodes.Optimizer;
import optimization.BasicObject;
import performance.EntropicRelevanceCalculator.BackGroundType;

public class Star extends BasicObject {
	
	public Star(int id, HashMap<String, Double> eventLog, HashMap<String, Character> action,double lower,double upper,BackGroundType bkgt) {
    	super();
    	this.fitness =0;
        setEdgeNode(new Optimizer(id,action.size(),eventLog,bkgt));
        setRandomPosition(lower,upper);
	}
	public void setRandomPosition(double lower,double upper) {
		Random rand = new Random(System.currentTimeMillis());
		this.solution[0] = 0.1 + (0.99 - 0.1) * rand.nextDouble();
	    this.solution[1] = 0.1 + (0.99 - 0.1) * rand.nextDouble();
	    solution[2] = rand.nextDouble(lower,upper);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
