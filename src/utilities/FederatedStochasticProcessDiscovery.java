package utilities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import model.ALERGIA;
import model.DFFA;
import model.FPTA;
import model.SDAG;
import optimization.DFvMOptimisation;
import optimization.Frontier;
import optimization.Optimization;
import optimization.evolutionBasedSolutions.DifferentialEvolution;
import optimization.evolutionBasedSolutions.Genetic;
import optimization.natureBasedSolutions.BlackHole;
import optimization.natureBasedSolutions.GravitationalSearch;
import optimization.swarmBasedSolutions.ACB;
import optimization.swarmBasedSolutions.ACO;
import optimization.swarmBasedSolutions.CS;
import optimization.swarmBasedSolutions.FA;
import optimization.swarmBasedSolutions.PSO;
import optimization.swarmBasedSolutions.SOS;
import optimization.swarmBasedSolutions.WO;
import performance.EntropicRelevanceCalculator;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;
import performance.EntropicRelevanceCalculator.BackGroundType;


public class FederatedStochasticProcessDiscovery {
    private int numEdgeNodes;
    private List<Optimization> optimiser;
    private PerformanceAnalyser performanceAnalyser;
    private EntropicRelevanceCalculator entropicRelevanceCalculator;
    private LogParser logParser;
    private HashMap<String, Double> eventLog;
    private SimpleDateFormat sdf ;
    private boolean ParetoFront;
    private boolean OPTFlag;
    private KMeans clustring;
    private HashMap<String, Double> globalEvnetLog;
    HashMap<String, Character> globalActions;
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public FederatedStochasticProcessDiscovery(int numberOfNodes,int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit,double cof) {
    	sdf = new SimpleDateFormat("hh:mm:ss:SSS");
    	this.OPTFlag = OPTFlag;
    	performanceAnalyser = new PerformanceAnalyser();
    	try {
    			LogParser.equallyDivideXesFile(fileDirectory,numberOfNodes);
    			logParser = new LogParser(fileDirectory);    
    			globalEvnetLog = logParser.extractEvent(globalActions);
    	}catch(Exception e)
        {
        	System.out.println("Error: Cannot read the XES file");
        	System.exit(0);
        }
        optimiser = new ArrayList<Optimization>();
        globalActions = new HashMap<String, Character>();
        
        
        //System.out.println("Extracting Log Started... "+ sdf.format(new Date()));
        for(int i=0;i<numberOfNodes;i++)
        	if (OPTFlag) {	
        		new File("chunck_"+i+".xes").deleteOnExit();
        		logParser = new LogParser("chunk_"+i+".xes");
                HashMap<String, Character> actions = new HashMap<String, Character>();
                HashMap<String, Double> eventLog = logParser.extractEvent(actions);
                fileDirectory = "chunk_"+i+".xes";
        		if (optName.compareTo("DFvM") == 0)
        			optimiser.add(new DFvMOptimisation(lower,upper,fileDirectory,Frontier_List_Size,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		if (optName.compareTo("PSO") == 0)
        			optimiser.add(new PSO(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("GASPD") == 0)
        			optimiser.add(new Genetic(i, iteration, fileDirectory, population, 0.8, 0.0,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("BEE") == 0) 
        			optimiser.add(new ACB(i, iteration, fileDirectory, population, 5,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("DE") == 0)
        			optimiser.add(new DifferentialEvolution(i, iteration, fileDirectory, population, 0.5, 0.9,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("GSA") == 0)
        			optimiser.add(new GravitationalSearch(i, iteration, fileDirectory, population, 100,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("ACO") == 0)
        			optimiser.add(new ACO(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("CS") == 0)
        			optimiser.add(new CS(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("WO") == 0)
        			optimiser.add(new  WO(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("BLH") == 0)
        			optimiser.add( new  BlackHole(i, iteration, fileDirectory, population, 0.1,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("SOS") == 0)
        			optimiser.add(new  SOS(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));
        		else if (optName.compareTo("FA") == 0)
        			optimiser.add(new  FA(i, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,globalActions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof));         
        	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void extractModel(List<Optimization> nodes) {
        ExecutorService executor = Executors.newFixedThreadPool(nodes.size());
        System.out.println("Extracting Models Started... "+ sdf.format(new Date()));
        for (int i = 0; i < nodes.size(); i++) {
            Optimization opt = nodes.get(i);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                	opt.run();
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {

        }
        System.out.println("Finished all threads");
     //   unionModels(nodes);
   }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
  
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<FPTA> rotateList(List<FPTA> fptaList){
    	Collections.rotate(fptaList, 1);
    	return fptaList;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<FPTA> getMergedClusteredModels(List<Optimization> nodeList,int numOfClusters){

    	extractModel(nodeList);
    
    	List<FPTA> fptaList=new ArrayList<FPTA>();
    	List<List<Optimization>> clusterModels = clusterModels(nodeList,numOfClusters);
    	
    	for(List<Optimization> cluster: clusterModels)
    	{
    		if(cluster.size()>0)
    		{
    			fptaList.add(unionModels(cluster));
    		}
    	}
    	return fptaList;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<List<Optimization>> clusterModels(List<Optimization> nodeList,int numOfClusters){
    /*	if(ParetoFront)
    	{
    		List<Optimization> removelist = new ArrayList<Optimization>();
        	for(Optimization node:nodeList)
        	{
        		for(Optimization node1:nodeList)
        		{
        			if(node.equals(node1)==false)
        			if(node1.getBestMetric()[0]>node.getBestMetric()[0])
        				if(node1.getBestMetric()[1]>node.getBestMetric()[1])
        				{
        					removelist.add(node1);
        					continue;
        				}
        		}
        	}
        	nodeList.removeAll(removelist);
    	}
    */
    	//for(Optimization op:nodeList)
    	//	System.out.println(op.getBestMetric()[0]+" "+op.getBestMetric()[1]);
    		clustring = new KMeans(numOfClusters, nodeList);
    	List<List<Optimization>> clusters= clustring.fit(100);
		return clusters;
    }
   
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public FPTA unionModels(List<Optimization> nodes) {
    	List<FPTA> mergedModel = new ArrayList<FPTA>();
    	FPTA mergedDffa = nodes.get(0).getBestFrontier().getFpta();
		ALERGIA alergia = new ALERGIA(mergedDffa);	
    	for(int i=1;i<nodes.size();i++)
    	{			
    	    alergia.mergeModel(mergedDffa, nodes.get(i).getBestFrontier().getFpta());
    		//mergedModel.add(mergedDffa);
    	}
    	return mergedDffa; 
    }   
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static void main(String[] args) {
    	executeFederatedStochasticProcessDiscovery(args);
    }
    public static void executeFederatedStochasticProcessDiscovery(String []args)
    {
    	HashMap<String,String> parms = processInput(args);
    	try {
    	System.out.println("  ================================================================================\r\n"
    			+ "");

    	System.out.println("Federated Stochastic Process Discovery Using Grammatical Inference.\n");
    	
    	double upper_f =0;
    	double lower_f =0;
    	if(parms.get("LOWER&UPPERBOUND_FILTERING")==null)
    	{
    		parms.put("LOWER&UPPERBOUND_FILTERING", "0.0000001-0.125");
    	}
	    	StringTokenizer st =new StringTokenizer(parms.get("LOWER&UPPERBOUND_FILTERING"),"-");
			lower_f = Double.parseDouble(st.nextToken());
			upper_f = Double.parseDouble(st.nextToken());
    	
		int pareto_size = Integer.parseInt(parms.get("PARETO_LIST_SIZE"));
		if(parms.get("Entropic Relevance Background Model")==null)
			parms.put("Entropic Relevance Background Model","U");
	
		if(parms.get("Optimal Model")==null)
			parms.put("Optimal Model", "DFFA");
		if(parms.get("Maximum Model Size")==null)
			parms.put("Maximum Model Size", "1000");
		if(parms.get("MAX_GENERATION")==null)
			parms.put("MAX_GENERATION", "25");
		if(parms.get("TIME_LIMITATION")==null)
			parms.put("TIME_LIMITATION", "1000");
		if(parms.get("POPULATION")==null)
			parms.put("POPULATION", "20");
		if(parms.get("PARETO_LIST_SIZE")==null)
			parms.put("PARETO_LIST_SIZE", "100");
		if(parms.get("Number of Nodes")==null)
			parms.put("Number of Nodes", "1");	
		if(parms.get("COF")==null)
			parms.put("COF", "0.5");
		if(parms.get("Number of Clusters")==null)
			parms.put("Number of Clusters", "1");
		HashMap<String,Double> Algorithms = new HashMap<String,Double>();
		for(int i=1;i<10;i++)
			if(parms.containsKey("ALG"+i))
			{
				Algorithms.put(parms.get("ALG"+i),0.0);
			}
		if(Algorithms.size()==0)
			Algorithms.put("DE", 0.0);
		String fileDirectory = parms.get("LOG_DIRECTORY");   
		int time_limit = Integer.parseInt(parms.get("TIME_LIMITATION"));
		LateXReportGenerator lateXReportGenerator = new LateXReportGenerator();
		BackGroundType bkgt = BackGroundType.U; 
		int sizeLimit = Integer.parseInt(parms.get("Maximum Model Size"));
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
    	double cof = Double.parseDouble(parms.get("COF"));	
		for(String alg:Algorithms.keySet())
		{
	        System.out.println(alg+" started at "+ sdf.format(new Date()));
	        FederatedStochasticProcessDiscovery fspd = null;
			if(parms.get("Entropic Relevance Background Model").compareTo("U")==0)
				bkgt=BackGroundType.U;
			else if(parms.get("Entropic Relevance Background Model").compareTo("Z")==0)
				bkgt=BackGroundType.Z;
	    	int maxItr = Integer.parseInt(parms.get("MAX_GENERATION"));
	    	int popSize = Integer.parseInt(parms.get("POPULATION"));
	    	int timeLimit = Integer.parseInt(parms.get("TIME_LIMITATION"));	
	    	int numberOfNodes = Integer.parseInt(parms.get("Number of Nodes"));	
	    	fspd = new FederatedStochasticProcessDiscovery(numberOfNodes,maxItr,popSize,alg,fileDirectory,true,true,lower_f,upper_f,pareto_size,"d", LocalDateTime.now(),timeLimit,bkgt,parms.get("Optimal Model"),sizeLimit,cof);
	    	List<FPTA> globalModels = new ArrayList<FPTA>();
	    	if(Integer.parseInt(parms.get("Number of Clusters"))<2)
	    		globalModels.add(fspd.mergeLocalModels(fspd.optimiser));
	    	else
	    		globalModels.addAll(fspd.getMergedClusteredModels(fspd.optimiser,Integer.parseInt(parms.get("Number of Clusters"))));
	    	PerformanceEstimator PE = new PerformanceEstimator(bkgt);	    		   
			List<FPTA> DFFAList = new ArrayList<FPTA>();
			List<FPTA> SDAGList = new ArrayList<FPTA>();
			List<FPTA> DFGList = new ArrayList<FPTA>();
			LocalDateTime localDateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	        String dateTimeString = localDateTime.format(formatter);
	    	File theDir = new File(dateTimeString+"/DFFA");
	    	theDir.mkdirs();
	    	theDir = new File(dateTimeString+"/SDAG");
	    	theDir.mkdirs();
	    	theDir = new File(dateTimeString+"/DFG");
	    	theDir.mkdirs();
	    	int cluster=1;
			for(FPTA fpta:globalModels)
			{
				if(globalModels!=null)
				{				
					
					DFFAList.add(fpta);	
					//HashMap<String,Double> pesub =PE.calculatePerformanceMetrics(fpta,fspd.globalEvnetLog ,fspd.globalActions.size());
					FPTA sdag = SDAG.DFFAtoSDAG(fpta);
					File fptaFile = new File(dateTimeString+"/FPTA/FPTA"+cluster+".dot");
					lateXReportGenerator.writeDFFAModel(fptaFile, fpta);
					HashMap<String,Double> pe =PE.calculatePerformanceDFGMetrics(sdag,fspd.globalEvnetLog ,fspd.globalActions.size());
					SDAGList.add(sdag);
					File sdagFile = new File(dateTimeString+"/SDAG/SDAG"+cluster+".dot");
					lateXReportGenerator.writeSDAGModel(sdagFile,sdag);
					FPTA dfg = DFFA.getDFG(sdag);
					File dfgFile = new File(dateTimeString+"/DFG/DFG"+cluster+".dot");
					lateXReportGenerator.writeDFGModel(dfgFile,dfg);
					//pe =PE.calculatePerformanceDFGMetrics(dfg, fspd.globalEvnetLog,fspd.globalActions.size());
					DFGList.add(dfg);
					cluster++;
				}
			}
			//lateXReportGenerator.addDFFAlist(DFFAList);
		//	lateXReportGenerator.addSDAGlist(SDAGList);	
		//	lateXReportGenerator.addDFGlist(DFGList);
			Algorithms.put(alg, 0.0);		
		}
		File f = new File(parms.get("LOG_DIRECTORY"));
		
		//MetaheuristicStochasticProcessDiscovery fspd = new MetaheuristicStochasticProcessDiscovery(1,1,"DFvM",fileDirectory,true,true,0.01,0.99,pareto_size,"d", LocalDateTime.now(),time_limit,bkgt,parms.get("Optimal Model"),sizeLimit,cof);
		//List<Frontier> DFGList = new ArrayList<Frontier>();
		//System.out.println("DFvM algorithm started "+sdf.format(new Date()));
		//for(Frontier frontier:fspd.ExtractParetoList())
		//{
			
	//		Frontier DFGfrontier = new Frontier(frontier.getFpta(), frontier.getSolution(), frontier.getFitness(), "DFvM");
	//		DFGList.add(DFGfrontier);
	//	}
	//	lateXReportGenerator.addDFvMlist(DFGList);
	//	lateXReportGenerator.generateReports(f.getName(),Algorithms,parms);
		System.out.println("Program finished ... "+sdf.format(new Date()));
    	}catch(Exception e)
    	{
    		System.out.println("Error: Check the Input parameters:"+parms);
    	}

    }
    
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static boolean addressFormatChecker(String address)
    {
    	/*String directoryRegex = "^([a-zA-Z]:\\\\)?([a-zA-Z0-9_\\-\\\\ ]+\\\\?)*$";
    	Pattern pattern = Pattern.compile(directoryRegex);
        if (pattern.matcher(address).matches()) {
        	return true;
        }
        return false;*/
    	return true;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static boolean lowerandUpperBoundChecker(String input) {
    	String doubleRangeRegex = "^(0(\\.\\d+)?|1(\\.0+)?)\\s*-\\s*(0(\\.\\d+)?|1(\\.0+)?)$";
        Pattern pattern = Pattern.compile(doubleRangeRegex);
        if (pattern.matcher(input).matches()) {
            return true;
        } 
    	return false;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static HashMap<String,String>  processInput(String[] args) {
    	HashMap<String,String> parameterizedInput = new HashMap<String,String>();
    	for (int i = 0; i < args.length; i++) {
    		for(int j=1;j<10;j++)
    		{
    			if (("-m"+j).equals(args[i]) && i + 1 < args.length) {
                	parameterizedInput.put("ALG"+j, args[i+1]);
                    i++; // Skip the next element as it's the value for -n
                }
    		}
    		
            if ("-el".equals(args[i]) && i + 1 < args.length) {
            	if(addressFormatChecker(args[i+1]))
        		{		
        			parameterizedInput.put("LOG_DIRECTORY", args[i+1]);
        		}
                i++; // Skip the next element as it's the value for -l
            } else if("-ft".equals(args[i]) && i + 1 < args.length) {
            	
            	if(lowerandUpperBoundChecker( args[i+1]))
        		{
        			parameterizedInput.put("LOWER&UPPERBOUND_FILTERING", args[i+1]);
        		}
            	i++;
            } else if("-pfs".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("PARETO_LIST_SIZE", args[i+1]);
    			i++;
    		}
            else if("-maxItr".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("MAX_GENERATION", args[i+1]);
    			i++;
    		}
            else if("-p".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("POPULATION", args[i+1]);
    			i++;
    		}
            else if("-t".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("TIME_LIMITATION", args[i+1]);
    			i++;
    		}
            else if("-erbm".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Entropic Relevance Background Model", args[i+1]);
    			i++;
    		}
            else if("-mms".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Maximum Model Size", args[i+1]);
    			i++;
    		}
            else if("-non".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Number of Nodes", args[i+1]);
    			i++;
    		}
            else if("-cof".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("COF", args[i+1]);
    			i++;
    		}
            else if("-noc".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Number of Clusters", args[i+1]);
    			i++;
    		}
        }
    	return parameterizedInput;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<Frontier> FindBestSolution(List<Optimization> nodeList,int clusterType) {
    	extractModel(nodeList);
    	
    	System.out.println("Models Extracted... "+ sdf.format(new Date()));
    //	List<FPTA> mrgedmodels1 = calculateMergedModels(nodeList,clusterType);
    //	for(Frontier f:nodeList.get(0).getFrontiers())
    //		printReport(f.getFpta(),eventLog);
    	List<Frontier> bestFrontiers = new ArrayList<Frontier>();
    	if(clusterType==0)
    	{
    		
    	}
    	for(int i = 0 ;i<nodeList.size();i++)
    	{
    		nodeList.get(i).getBestFrontier().setEventLog(nodeList.get(i).getEventLog());
    		nodeList.get(i).getBestFrontier().setActionListSize(nodeList.get(i).getActionSize());
    		bestFrontiers.add(nodeList.get(i).getBestFrontier());		
    	}
    	
    	return bestFrontiers;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public FPTA mergeLocalModels(List<Optimization> nodeList) {
    	extractModel(nodeList);
    	return unionModels(nodeList);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void printReport(FPTA mergedDffa, HashMap<String, Double> log) {
    	System.out.println("Performance Analysing Started... "+ sdf.format(new Date()));
    	System.out.println("*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
   // 	System.out.println("Fitness is: "+ performanceAnalyser.calculateFitness(mergedDffa,log));
    	System.out.println("Percision is: "+ performanceAnalyser.calculatePercision(mergedDffa, log));
		System.out.println("Size is "+performanceAnalyser.calculateSize(mergedDffa));
		System.out.println("fitness is: "+ performanceAnalyser.calculateFitness1(mergedDffa, log));
		HashMap<String, Character> readMapList = logParser.readMapList("actionMap.txt");
    	int actionListSize = readMapList.size();
	//	entropicRelevanceCalculator = new EntropicRelevanceCalculator(mergedDffa,log,actionListSize);
		System.out.println("ER is: "+ entropicRelevanceCalculator.getER());

    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
  //  public List<FPTA> calculateMergedModels(List<Optimization> nodeList,int type) {
  //    return unionModels(clusterModels(nodeList,type));
  //  }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public PerformanceAnalyser getPerformanceAnalyser() {
		return performanceAnalyser;
	}
	public void setPerformanceAnalyser(PerformanceAnalyser performanceAnalyser) {
		this.performanceAnalyser = performanceAnalyser;
	}
	public boolean isOPTFlag() {
		return OPTFlag;
	}
	public void setOPTFlag(boolean oPTFlag) {
		OPTFlag = oPTFlag;
	}
}