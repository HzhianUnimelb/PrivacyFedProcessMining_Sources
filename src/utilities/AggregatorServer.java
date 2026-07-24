package utilities;

import java.io.File;
import java.sql.ClientInfoStatus;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import model.ALERGIA;
import model.DFFA;
import model.FPTA;
import model.SDAG;
import nodes.ClientOptimiser;
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;

public class AggregatorServer {
	private List<ClientOptimiser> clients;
	private SimpleDateFormat sdf ;
	private LogParser logParser;
	private int numberOfNodes;
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public static void main(String[] args) {
			// TODO Auto-generated method stub
    	System.out.println("  ================================================================================\r\n");
    	System.out.println("Sharing Pearls, Not Pebbles Privacy-Preserving Federated Stochastic Process Discovery via Partial Model Disclosure.\n");

		AggregatorServer.executeFederatedStochasticProcessDiscovery(args);
		System.out.println("Program terminated"+ new SimpleDateFormat("hh:mm:ss:SSS").format(new Date()));

	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public static void executeFederatedStochasticProcessDiscovery(String []args)
    {
		HashMap<String,String> parms = processInput(args);
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
		if(parms.get("DCI_SIZE")==null)
			parms.put("DCI_SIZE", "4");
		if(parms.get("Optimal Model")==null)
			parms.put("Optimal Model", "DFFA");
		if(parms.get("Maximum Model Size")==null)
			parms.put("Maximum Model Size", "1000");
		if(parms.get("MAX_GENERATION")==null)
			parms.put("MAX_GENERATION", "25");
		if(parms.get("TIME_LIMITATION")==null)
			parms.put("TIME_LIMITATION", "3000");
		if(parms.get("POPULATION")==null)
			parms.put("POPULATION", "20");
		if(parms.get("PARETO_LIST_SIZE")==null)
			parms.put("PARETO_LIST_SIZE", "100");
		if(parms.get("Number of Nodes")==null)
			parms.put("Number of Nodes", "1");	
		if(parms.get("tau")==null)
			parms.put("tau", "0.1");	
		if(parms.get("epsilon")==null)
			parms.put("epsilon", "1.0");
		if(parms.get("cof")==null)
			parms.put("cof", "0.5");
		String fileDirectory = parms.get("LOG_DIRECTORY");   

		HashMap<String,Double> Algorithms = new HashMap<String,Double>();
		LateXReportGenerator lateXReportGenerator = new LateXReportGenerator();
		for(int i=1;i<10;i++)
			if(parms.containsKey("ALG"+i))
			{
				Algorithms.put(parms.get("ALG"+i),0.0);
			}
		if(Algorithms.size()==0)
			Algorithms.put("DE", 0.0);
		BackGroundType bkgt = BackGroundType.U; 
		int sizeLimit = Integer.parseInt(parms.get("Maximum Model Size"));
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
    	
		if(parms.get("Entropic Relevance Background Model").compareTo("U")==0)
			bkgt=BackGroundType.U;
		else if(parms.get("Entropic Relevance Background Model").compareTo("Z")==0)
			bkgt=BackGroundType.Z;
    	int maxItr = Integer.parseInt(parms.get("MAX_GENERATION"));
    	int popSize = Integer.parseInt(parms.get("POPULATION"));
    	int timeLimit = Integer.parseInt(parms.get("TIME_LIMITATION"));	
    	int numberOfNodes = Integer.parseInt(parms.get("Number of Nodes"));	
    	double epsilon = Double.parseDouble(parms.get("epsilon"));	
      	double cof = 0.9999;/*Integer.parseInt(parms.get("cof"));	*/
      	try {
      		LogParser.equallyDivideXesFile(fileDirectory,numberOfNodes);
      	}
      	catch(Exception e)
      	{
      		System.out.println("Error: Cannot read the XES file");
      	}
      	
      	LateXReportGen  latexGen = new LateXReportGen();
	    File f = new File(parms.get("LOG_DIRECTORY"));
	    List<Point1> points = new ArrayList<Point1>(); 
	    HashMap<String, Double> algo = new HashMap<String, Double>();
		for(String alg:Algorithms.keySet())
		{		
			System.out.println(alg+" started at "+ sdf.format(new Date()));
			AggregatorServer aggregatorServer = new AggregatorServer(numberOfNodes,maxItr,popSize,alg,fileDirectory,true,true,lower_f,upper_f,pareto_size,"d", LocalDateTime.now(),timeLimit,bkgt,parms.get("Optimal Model"),sizeLimit,cof,points,epsilon);		
		}
		algo.put("P2FedGASPD", 1.0);
		algo.put("FedGASPD", 2.0);

		   List<Point1> DF = new ArrayList<Point1>(); 
		    List<Point1> OPTFEDDF = new ArrayList<Point1>(); 
		    List<Point1> FEDDF = new ArrayList<Point1>(); 
		    for(Point1 p:points)
		    {
		    	if(p.name.compareTo("GASPD")==0)
		    		addPoint(DF,p);
		    	if(p.name.compareTo("P2FedGASPD")==0)
		    		addPoint(OPTFEDDF,p);
		    	if(p.name.compareTo("FedGASPD")==0)
		    		addPoint(FEDDF,p);
		    }
			latexGen.addDFFAlist(OPTFEDDF);
			latexGen.addDFFAlist(FEDDF);
			latexGen.addDFFAlist(DF);
			latexGen.generateReports(f.getName(),algo, parms);
    }
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public static void addPoint(List<Point1>list,Point1 p) {
		boolean flag=true;
	    List<Point1> rm = new ArrayList<Point1>();
	    for(Point1 p1:list)
	    {
	    	if(p1.er>=p.er && p1.size>=p.size)
	    		rm.add(p1);
	    	if(p.er>=p1.er && p.size>=p1.size)
	    		flag=false;
	    }
	    list.remove(rm);
	    if(flag)
	    	list.add(p);
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public AggregatorServer(int numberOfNodes,int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit,int x,double cof,double epsilon) {
		 HashMap<String, Character> globalActions = new 	HashMap<String, Character>();
		 sdf = new SimpleDateFormat("hh:mm:ss:SSS");
		 clients = new ArrayList<ClientOptimiser>();
	     logParser = new LogParser(fileDirectory);
	     logParser.extractEvent(globalActions);
	     for(int i=0;i<numberOfNodes;i++)
	     {
	    	 clients.add(new ClientOptimiser(i, 0.1, globalActions, iteration, population, optName, ParetoFront, OPTFlag, lower, upper, Frontier_List_Size, symbol, time, seconds, bkgt, optModel, sizeLimit,cof,epsilon));
	     }
	     
		compareFedGASPDvsPrivacyFedGASPD();
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public AggregatorServer(int numberOfNodes,int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit,double cof,List<Point1> points,double epsilon) {

		HashMap<String, Character> globalActions = new HashMap<String, Character>();
		 sdf = new SimpleDateFormat("hh:mm:ss:SSS");
			System.out.println(" Started at "+ sdf.format(new Date()));
		 clients = new ArrayList<ClientOptimiser>();
	     logParser = new LogParser(fileDirectory);
	     logParser.extractEvent(globalActions);
	     for(int i=0;i<numberOfNodes;i++)
	     {
	    	 clients.add(new ClientOptimiser(i, 0.1, globalActions, iteration, population, optName, ParetoFront, OPTFlag, lower, upper, Frontier_List_Size, symbol, time, seconds, bkgt, optModel, sizeLimit,cof,epsilon));
	     }
	     retriveOrginalModels();
	
	     FPTA aggFPTA=runAlgorithm();
	     FPTA compFPTA= compressModels(aggFPTA);
	    // System.out.println("size end "+PerformanceAnalyser.calculateModelSize(compFPTA));

	     DecimalFormat df = new DecimalFormat("0.000");
	     double avgEr=0;
	     double sharingSize=0;
	     int activeClients=0;
	     double avgPr=0;
	     for(ClientOptimiser co:clients)
	     {
	    	 if(co.getState())
	    	 {
	    		 co.setGlobalModel(compFPTA);
	    		 activeClients++;
	    		 avgEr+=co.getGlobalModel().getCurrentFitness();
	    		 sharingSize+=co.getThreshold()/co.getOptimiser().getBestFrontier().getFitness()[1];
	    		 avgPr+=co.calculateAnonymity(co.getOptimiser().getSubFPTAModel());	
	    	 }
	    }
	    points.add(new Point1(avgEr/activeClients,clients.get(0).getGlobalModel().getSize(),avgPr/activeClients,"P2FedGASPD","p"));	     
	    LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	    String dateTimeString = localDateTime.format(formatter);
	    printModels(clients.get(0).getGlobalModel().getFpta(),"P2FedGASPD",dateTimeString);
	    getBaseLineAppaorch(points,dateTimeString);
	}
	public void printModels(FPTA fpta,String solution,String dateTimeString) {
		
	    File theDir = new File(dateTimeString+"/"+solution+"/DFFA");
	    theDir.mkdirs();
	    theDir = new File(dateTimeString+"/"+solution+"/SDAG");
	    theDir.mkdirs();
	    theDir = new File(dateTimeString+"/"+solution+"/DFG");
	    theDir.mkdirs();
		LateXReportGenerator lateXReportGenerator = new LateXReportGenerator();
		File fptaFile = new File(dateTimeString+"/"+solution+"/DFFA/DFFA.dot");
		lateXReportGenerator.writeDFFAModel(fptaFile, fpta);
		FPTA sdag = SDAG.DFFAtoSDAG(fpta);
		File sdagFile = new File(dateTimeString+"/"+solution+"/SDAG/SDAG.dot");
		lateXReportGenerator.writeSDAGModel(sdagFile, sdag);
		FPTA dfg = DFFA.getDFG(sdag);
		File dfgFile = new File(dateTimeString+"/"+solution+"/DFG/DFG.dot");
		lateXReportGenerator.writeSDAGModel(dfgFile, dfg);
	
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void getBaseLineAppaorch(List<Point1> points,String dateTimeString) {
		
		List<FPTA> subgraphs= new ArrayList<FPTA>();
		
		for(ClientOptimiser co:clients)
		{
			co.getOptimiser().getBestFrontier().getFpta().clearModel(co.getOptimiser().getBestFrontier().getFpta());
			FPTA temp = new FPTA();
			co.getOptimiser().getBestFrontier().getFpta().copy(temp);
			subgraphs.add(temp);
		}
		 FPTA aggFPTA = unionModels(subgraphs);
		 FPTA comFPTA =  /*compressModels(*/aggFPTA;
	
		 int index=0;
		 double avgEr=0;
		 int activeClient=0;
		 double size =0 ;
		aggFPTA.clearModel(aggFPTA);
		double asize = PerformanceAnalyser.calculateModelSize(aggFPTA);
		double avgPr=0;
		for(ClientOptimiser co:clients)
		{
			PerformanceEstimator pe = new PerformanceEstimator(BackGroundType.U);
			HashMap<String,Double> res =pe.calculatePerformanceMetrics(co.getOptimiser().getBestFrontier().getFpta(), co.getEventLog(), activeClient);
			 co.setGlobalModel(comFPTA);
			 size = PerformanceAnalyser.calculateModelSize(co.getOptimiser().getBestFrontier().getFpta());
			 size = PerformanceAnalyser.calculateModelSize(co.getOptimiser().getBestFrontier().getFpta());

			 activeClient++;
			 avgEr+=co.getGlobalModel().getCurrentFitness();
			 avgPr+=co.calculateAnonymity(co.getOptimiser().getBestFrontier().getFpta());
		}
	    printModels(clients.get(0).getGlobalModel().getFpta(),"FedGASPD",dateTimeString);

	    points.add(new Point1(avgEr/activeClient,clients.get(0).getGlobalModel().getSize(),avgPr/activeClient, "FedGASPD","f"));	     
		
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public FPTA runAlgorithm() {
		 boolean flag=false;
		 FPTA aggFPTA = null ;
         System.out.println("Clients started extracting their submodels ... "+ sdf.format(new Date()));

		 while(!flag)
		 {
			 retriveSubModels();
			 List<FPTA> subgraphs= new ArrayList<FPTA>();
			 
			 for(ClientOptimiser co:clients)
			 {
				 FPTA fpta = new FPTA();
				 co.getOptimiser().getSubFPTAModel().copy(fpta);	
				 subgraphs.add(fpta);
			 }

			 aggFPTA = unionModels(subgraphs);
			// aggFPTA.show(aggFPTA, "");
			 flag=true;
			 for(ClientOptimiser co : clients)
			 {			 
				 co.setGlobalModel(aggFPTA);		
				 if(!co.isDone())
					 flag=false;
			 }
		 }
		 return aggFPTA;
	//	 System.out.println("before--->"+PerformanceAnalyser.calculateModelSize(aggFPTA));
	//	 aggFPTA.show(aggFPTA, "before"+PerformanceAnalyser.calculateModelSize(aggFPTA));

		// FPTA comFPTA = compressModels(aggFPTA);
	//	 System.out.println("after--->"+PerformanceAnalyser.calculateModelSize(comFPTA));
	//	 comFPTA.show(comFPTA, "before"+PerformanceAnalyser.calculateModelSize(comFPTA));

	//	 for(ClientOptimiser co : clients)
	//	 {
	//		 co.setGlobalModel(comFPTA);
	//	 }
		 
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public FPTA compressModels(FPTA mergedDffa) {
		ALERGIA alergia = new ALERGIA(mergedDffa);	
		alergia.setAlpha(0.1);
		alergia.setFilterring(30);
	
		List<String> x =ALERGIA.listNonCycle1(mergedDffa);
		List<String> list= ALERGIA.getSubRoots(mergedDffa,x);
	//	for(String x:list)
//		{
//			System.out.println(list.size()+"-->subT-->"+x);
//		}
		FPTA fpta1 = alergia.run(mergedDffa, list);
		return fpta1;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public FPTA unionModels(List<FPTA> models) {
    	FPTA mergedDffa = models.get(0);
		ALERGIA alergia = new ALERGIA(mergedDffa);	
		alergia.setAlpha(0.1);
		alergia.setFilterring(30);
    	for(int i=1;i<clients.size();i++)
    	{			
    	    alergia.mergeThirdModel(mergedDffa, models.get(i));
    	   // System.out.println("merged "+i);    	    		
    	}	
    	return mergedDffa; 
    }   
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
     public List<ClientOptimiser> getClients() {
		return clients;
	 }
 	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
     public void retriveOrginalModels() {
    	 ExecutorService executor = Executors.newFixedThreadPool(clients.size());
         System.out.println("Clients started extracting their models ... "+ sdf.format(new Date()));
         for (int i = 0; i < clients.size(); i++) {
              ClientOptimiser client = clients.get(i);
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                 	client.extractOrginalModel();
                 }
             });
         }
         executor.shutdown();
         while (!executor.isTerminated()) {

         }
        // System.out.println("Clients extracted their models ... "+ sdf.format(new Date()));
      //   unionModels(nodes);
     }
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	 public void setClients(List<ClientOptimiser> clients) {
		this.clients = clients;
	 }
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	 public int getNumberOfNodes() {
		return numberOfNodes;
	 }
	 /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	 public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
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
	 	public void retriveSubModels() {
	 		ExecutorService executor = Executors.newFixedThreadPool(clients.size());
	        for (int i = 0; i < clients.size(); i++) {
	              ClientOptimiser client = clients.get(i);
	             executor.execute(new Runnable() {
	                 @Override
	                 public void run() {
	                 	client.extractSubModel();
	                 }
	             });
	         }
	         executor.shutdown();
	         while (!executor.isTerminated()) {

	         }
	 	}
	  /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	  public void compareFedGASPDvsPrivacyFedGASPD() {
		  retriveOrginalModels();
		  List<FPTA> subgraphs= new ArrayList<FPTA>();
		  for(ClientOptimiser co:clients)
			 subgraphs.add(co.getOptimiser().getBestFrontier().getFpta());
		  FPTA aggFPTA = unionModels(subgraphs);
		  FPTA comFPTA =  compressModels(aggFPTA);
		  double avgEr=0;
		  double avgSize=0;
		  for(ClientOptimiser co:clients)
		  {
				HashMap<String,Double> res = co.evaluateAggregatedModel(comFPTA);
				avgEr+=res.get("Entropic Relevance");
				avgSize = res.get("Size");
		  }
		  double avglast=0;
		  double avgPSize=0;
		  for(ClientOptimiser co:clients)
		  {
				HashMap<String,Double> res = co.evaluateAggregatedModel(comFPTA);
				avglast+=res.get("Entropic Relevance");
			
		  }
		 
		 
		  for(double threshold=0.05;threshold<=0.06;threshold+=0.05)
		  {
			  double avesubEr=0;
			  subgraphs= new ArrayList<FPTA>();
			  for(ClientOptimiser co:clients)
			  {				  
				  subgraphs.add(co.extractSubModel(threshold));				 
			  }
			  aggFPTA = unionModels(subgraphs);
			  comFPTA =  compressModels(aggFPTA);
			  for(ClientOptimiser co:clients)
			  {	
				  HashMap<String,Double> res = co.evaluateAggregatedModel(comFPTA);
				  avesubEr+=res.get("Entropic Relevance");
				  avgPSize=res.get("Size");
			  }			
			  
		  }

		  
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

	 public static HashMap<String,String> processInput(String[] args) {
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
	            else if("-dci".equals(args[i]) && i + 1 < args.length) {
	    			parameterizedInput.put("DCI_SIZE", args[i+1]);
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
	            else if("-epsilon".equals(args[i]) && i + 1 < args.length) {
	    			parameterizedInput.put("epsilon", args[i+1]);
	    			i++;
	    		}
	            else if("-cof".equals(args[i]) && i + 1 < args.length) {
	    			parameterizedInput.put("cof", args[i+1]);
	    			i++;
	    		}
	        }
	    	return parameterizedInput;
	    }
}
