package nodes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;

import model.DFFA;
import model.FPTA;
import optimization.Optimization;
import optimization.evolutionBasedSolutions.DifferentialEvolution;
import optimization.evolutionBasedSolutions.Genetic;
import optimization.natureBasedSolutions.BlackHole;
import optimization.natureBasedSolutions.GravitationalSearch;
import optimization.swarmBasedSolutions.*;
import performance.EntropicRelevanceCalculator;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;
import performance.EntropicRelevanceCalculator.BackGroundType;
import utilities.GlobalModel;
import utilities.LogParser;
import utilities.SubgraphSolver;

enum ClientState{Started,Finished,Invalid}
public class ClientOptimiser {

	private int threshold;
	private GlobalModel globalModel;
	private Optimization optimiser;
	private LogParser logParser;
    private HashMap<String, Double> eventLog;
    private HashMap<String, Character> localActions;
    private SimpleDateFormat sdf ;
    private int clientId;
    private ClientState state;
    private BackGroundType bkgt;
    private double learningRate;
    private double epsilon;
    private double subgraphSize;
    private double orginalSize;
    
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public ClientOptimiser(int clientId,double threshold,HashMap<String, Character> globalActions, int iteration,int population,String optName,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit) {
		this.clientId = clientId;
		this.bkgt = bkgt;
		this.state=ClientState.Started;
		globalModel = new GlobalModel();
		sdf = new SimpleDateFormat("hh:mm:ss:SSS");
	
		String fileDirectory = "chunk_"+clientId+".xes";
		new File("chunck_"+clientId+".xes").deleteOnExit();
		logParser = new LogParser("chunk_"+clientId+".xes");
		localActions = new HashMap<String, Character>();
        eventLog = logParser.extractEvent(localActions);
        this.threshold= 1;
		if (optName.compareTo("PSO") == 0)
			optimiser = new PSO(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("GEN") == 0)
			optimiser = new Genetic(clientId, iteration, fileDirectory, population, 0.8, 0.0,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("BEE") == 0) 
			optimiser = new ACB(clientId, iteration, fileDirectory, population, 5,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("DE") == 0)
			optimiser = new DifferentialEvolution(clientId, iteration, fileDirectory, population, 0.5, 0.9,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("GSA") == 0)
			optimiser = new GravitationalSearch(clientId, iteration, fileDirectory, population, 100,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("ACO") == 0)
			optimiser = new ACO(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("CS") == 0)
			optimiser = new CS(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("WO") == 0)
			optimiser = new  WO(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("BLH") == 0)
			optimiser = new  BlackHole(clientId, iteration, fileDirectory, population, 0.1,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("SOS") == 0)
			optimiser = new  SOS(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);
		else if (optName.compareTo("FA") == 0)
			optimiser = new  FA(clientId, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,localActions.size(),bkgt,optModel,sizeLimit);         
		learningRate=0.5;
		epsilon = 0.5;
		
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void extractOrginalModel() {
		optimiser.run();
		PerformanceEstimator PE = new PerformanceEstimator(bkgt);	
		HashMap<String,Double> pesub =PE.calculatePerformanceMetrics(optimiser.getBestFrontier().getFpta(),eventLog ,optimiser.actionSize);
		orginalSize=pesub.get("Size");
		System.out.println("orginalSize-->"+orginalSize);
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public HashMap<String,Double> evaluateAggregatedModel(FPTA model){
		PerformanceEstimator PE = new PerformanceEstimator(bkgt);	
		HashMap<String,Double> pesub =PE.calculatePerformanceMetrics(model,eventLog ,optimiser.actionSize);
		subgraphSize=pesub.get("Size");
		return pesub;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public double calculateAnonymity(FPTA model) {
		return PerformanceAnalyser.calculateAnonymity(model, eventLog);
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public FPTA extractSubModel() {
	
		if(state.compareTo(ClientState.Finished)!=0)
		{
			threshold++;
			optimiser.setSubFPTAModel(SubgraphSolver.solveDFFAOptimization(optimiser.getBestFrontier().getFpta(),threshold));
			PerformanceEstimator PE = new PerformanceEstimator(bkgt);	
			HashMap<String,Double> pesub =PE.calculatePerformanceMetrics(optimiser.getSubFPTAModel(),eventLog ,optimiser.actionSize);
			subgraphSize=pesub.get("Size");
		}	
		return optimiser.getSubFPTAModel();
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public FPTA extractSubModel(double threshold) {		
		optimiser.setSubFPTAModel(SubgraphSolver.solveDFFAOptimization(optimiser.getBestFrontier().getFpta(),(int)(threshold*orginalSize)+1));
		return optimiser.getSubFPTAModel();
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public int getThreshold() {
		return threshold;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public Optimization getOptimiser() {
		return optimiser;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setOptimizer(Optimization optimiser) {
		this.optimiser = optimiser;
	}

    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public LogParser getLogParser() {
		return logParser;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public void setLogParser(LogParser logParser) {
		this.logParser = logParser;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public HashMap<String, Double> getEventLog() {
		return eventLog;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setEventLog(HashMap<String, Double> eventLog) {
		this.eventLog = eventLog;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public SimpleDateFormat getSdf() {
		return sdf;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public int getClientId() {
		return clientId;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setClientId(int clientId) {
		this.clientId = clientId;
	 }
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public boolean getState() {
		if(state.compareTo(ClientState.Invalid)==0)
			return false;
		return true;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setState(ClientState state) {
		this.state = state;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public GlobalModel getGlobalModel() {
		return globalModel;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public boolean isDone() {
		if(state.compareTo(ClientState.Finished)==0)
			return true;
		return false;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setGlobalModel(FPTA fpta) {
		this.globalModel.setFpta(fpta);
		PerformanceEstimator PE = new PerformanceEstimator(bkgt);	
		HashMap<String,Double> pesub =PE.calculatePerformanceMetrics(globalModel.getFpta(),eventLog ,optimiser.actionSize);
		globalModel.setCurrentFitness(pesub.get("Entropic Relevance"));
		globalModel.setSize(pesub.get("Size"));
		
		
		if(state.compareTo(ClientState.Finished)!=0 && state.compareTo(ClientState.Invalid)!=0 )
		{
			if(optimiser.getBestFrontier().getFitness()[0]>5000)
				state = ClientState.Invalid; 	
			if(optimiser.getBestFrontier().getFitness()[0]>=globalModel.getCurrentFitness())
				state = ClientState.Finished; 			
			else if(globalModel.getCurrentFitness() - optimiser.getBestFrontier().getFitness()[0]<epsilon)
				state = ClientState.Finished; 	
			else 
			{
				globalModel.setPreviousFitness(globalModel.getCurrentFitness());						
			}
			//threshold+=0.1;
	//		else
	//			state = ClientState.Finished; 
			
			if(threshold>=optimiser.getBestFrontier().getFitness()[1])				
				state = ClientState.Finished; 
		//	else
		//		System.out.println(clientId+" threshold ---> "+threshold+" "+optimiser.getBestFrontier().getFitness()[1]);
	
			
		}
		//if(state.compareTo(ClientState.Finished)!=0)
		//	threshold=threshold+(Math.abs(globalModel.getCurrentFitness()-optimiser.getBestFrontier().getFitness()[0]))*learningRate;
	
		//	state = ClientState.Finished; 	
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public double getLearningRate() {
		return learningRate;
	}
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}
	public double getSubgraphSize() {
		return subgraphSize;
	}
	public void setSubgraphSize(double subgraphSize) {
		this.subgraphSize = subgraphSize;
	}
}
