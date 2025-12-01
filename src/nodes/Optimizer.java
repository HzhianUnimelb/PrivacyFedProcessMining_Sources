package nodes;

import java.util.HashMap;
import model.*;
import optimization.DFvMOptimisation;
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceEstimator;
import utilities.FrequencyBasedFiltering;
import utilities.SubgraphSolver;


public class Optimizer {

	private FPTA fixFPTA,currentFPTA;
	private HashMap<String, Double>  eventLog;
	private HashMap<String, Double>  filterEventLog;
	private PerformanceEstimator performanceEstimator;
	private int actionList;
	
	/**/
	public PerformanceEstimator getPerformanceEstimator() {
		return performanceEstimator;
	}
	
	public int getActionList() {
		return actionList;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	 public FPTA runModel(double alpha,double T0,double filteringThreshold,String algorithmName) {
		 FrequencyBasedFiltering filtering = new FrequencyBasedFiltering();
		 filterEventLog = FrequencyBasedFiltering.filterEventLog(eventLog, filteringThreshold);		 
		 fixFPTA = FPTA.constructFPTA(filterEventLog);
		// fixFPTA.show(fixFPTA, "first model");
         currentFPTA = new ALERGIA(alpha, T0,filterEventLog).run();     
         return currentFPTA;
	 }
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public Optimizer(int type,int size,HashMap<String, Double> eventLog,BackGroundType bkgt) {
		super();
		this.eventLog = eventLog;
        actionList = size;
		fixFPTA = FPTA.constructFPTA(eventLog);
		
		FPTA x = new DFvMOptimisation().extractSignleModel(eventLog);
		FPTA subG = SubgraphSolver.solvePFTAStructure(x,50000);
		x.showDFvM(x, "x");
		subG.showDFvM(subG, "subG");
        performanceEstimator = new PerformanceEstimator(fixFPTA, eventLog, actionList,bkgt);
        
       
	/*	else
		{
			eventLog = new HashMap<String, Long>();
			eventLog.put("",  (long)40);
			eventLog.put("b",  (long)10);
			eventLog.put("bb",  (long)10);
			eventLog.put("a",  (long)30);
			eventLog.put("aa",  (long)10);
    		actionList = 2;
		}*/
		
		// TODO Auto-generated constructor stub
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	OptimizerEdgeNode i = new OptimizerEdgeNode(0, "chunk_1.xes",1);
		//i.performanceEstimator.calculatePerformanceMetrics(i.runModel(0.5, 30, "ALERGIA"), i.eventLog, i.actionList);	
		//i.performanceEstimator.calculatePerformanceMetrics(i.runModel(0.2, 2, "ALERGIA"), i.eventLog, i.actionList);	
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public HashMap<String, Double> getEventLog() {
		return eventLog;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public void setEventLog(HashMap<String, Double> eventLog) {
		this.eventLog = eventLog;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public FPTA getCurrentFPTA() {
		return currentFPTA;
	}
}
