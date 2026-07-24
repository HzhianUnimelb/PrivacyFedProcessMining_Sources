package nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.*;
import optimization.DFvMOptimisation;
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;
import utilities.FrequencyBasedFiltering;
import utilities.SubgraphSolver;


public class Optimizer {

	private FPTA fixFPTA,currentFPTA;
	public static FPTA entireFPTA;
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
	 public FPTA runModel(double alpha,double T0,double filteringThreshold,String algorithmName,int actionSize) {
		 FrequencyBasedFiltering filtering = new FrequencyBasedFiltering();
		 filterEventLog = FrequencyBasedFiltering.filterEventLog(eventLog, filteringThreshold);		 
		 fixFPTA = FPTA.constructFPTA(filterEventLog);
		// fixFPTA.show(fixFPTA, "first model");
         currentFPTA = new ALERGIA(alpha, T0,filterEventLog,eventLog,actionSize).run();     
         return currentFPTA;
	 }
	    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

	 public FPTA runModelDFvM(double alpha,double T0,double filteringThreshold) {
		
		int size_limit = (int)(filteringThreshold*PerformanceAnalyser.calculateModelSize(entireFPTA));
		//HashMap<String, Double>  filterLog= DFvMOptimisation.extractFilteredLog(size_limit, eventLog);
		//System.out.println(DFvMOptimisation.primaryDFvMSize+")"+filteringThreshold+") ("+size_limit+") ("+filterLog.size());
		FPTA partialModel = SubgraphSolver.extractMaxFrequencySubtree(entireFPTA, size_limit);
		List<String>rmlist=new ArrayList<String>();
		HashMap<String, Double>  filterEventLog = new HashMap<String, Double>();
		filterEventLog.putAll(eventLog);	 
		for(String s:eventLog.keySet())
		{
			double i = FPTA.isTraceCovered(partialModel,s);
			if(i==0)
			{
				rmlist.add(s);
			}
		}
		for(String s:rmlist)
			filterEventLog.remove(s);
		if(filterEventLog.size() <1)
	    	return null;
		currentFPTA = new ALERGIA(alpha, T0,filterEventLog,eventLog,actionList).run();  
	    return currentFPTA;
	 }
	 public static void extractEntireFPTA(HashMap<String, Double> eventLog) {
		 System.out.println("extract entire model");
		 
		 entireFPTA = FPTA.constructFPTA(eventLog);
		// entireFPTA.calculateTransitionPercentage(entireFPTA);
		// System.out.println("end of entire model extracting");
	 }
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	 
	public Optimizer(int type,int size,HashMap<String, Double> eventLog,BackGroundType bkgt) {
		super();
		this.eventLog = eventLog;
        actionList = size;
		fixFPTA = FPTA.constructFPTA(eventLog);
       performanceEstimator = new PerformanceEstimator(fixFPTA, eventLog, actionList,bkgt);
		//	performanceEstimator = new PerformanceEstimator(entireFPTA, eventLog, actionList,bkgt);
      //  fixFPTA.calculateTransitionPercentage(fixFPTA);
       
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
