package utilities;

import java.sql.ClientInfoStatus;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
import model.FPTA;
import nodes.ClientOptimiser;
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceAnalyser;

public class AggregatorServer {
	private List<ClientOptimiser> clients;
	private SimpleDateFormat sdf ;
	private LogParser logParser;
	private int numberOfNodes;
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public static void main(String[] args) {
			// TODO Auto-generated method stub
		AggregatorServer.executeFederatedStochasticProcessDiscovery(args);
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
        LogParser.equallyDivideXesFile(fileDirectory,numberOfNodes);

		for(String alg:Algorithms.keySet())
		{		
			System.out.println(alg+" started at "+ sdf.format(new Date()));
			AggregatorServer aggregatorServer = new AggregatorServer(numberOfNodes,maxItr,popSize,alg,fileDirectory,true,true,lower_f,upper_f,pareto_size,"d", LocalDateTime.now(),timeLimit,bkgt,parms.get("Optimal Model"),sizeLimit);		
		}
    }
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public AggregatorServer(int numberOfNodes,int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit,int x) {
		 HashMap<String, Character> globalActions = new 	HashMap<String, Character>();
		 sdf = new SimpleDateFormat("hh:mm:ss:SSS");
		 clients = new ArrayList<ClientOptimiser>();
	     logParser = new LogParser(fileDirectory);
	     logParser.extractEvent(globalActions);
	     for(int i=0;i<numberOfNodes;i++)
	     {
	    	 clients.add(new ClientOptimiser(i, 0.1, globalActions, iteration, population, optName, ParetoFront, OPTFlag, lower, upper, Frontier_List_Size, symbol, time, seconds, bkgt, optModel, sizeLimit));
	     }
		compareFedGASPDvsPrivacyFedGASPD();
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public AggregatorServer(int numberOfNodes,int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit) {
		 HashMap<String, Character> globalActions = new HashMap<String, Character>();
		 sdf = new SimpleDateFormat("hh:mm:ss:SSS");
		 clients = new ArrayList<ClientOptimiser>();
	     logParser = new LogParser(fileDirectory);
	     logParser.extractEvent(globalActions);
	     for(int i=0;i<numberOfNodes;i++)
	     {
	    	 clients.add(new ClientOptimiser(i, 0.1, globalActions, iteration, population, optName, ParetoFront, OPTFlag, lower, upper, Frontier_List_Size, symbol, time, seconds, bkgt, optModel, sizeLimit));
	     }
	     retriveOrginalModels();
	     FPTA aggFPTA=runAlgorithm();
	     System.out.println("size before "+PerformanceAnalyser.calculateModelSize(aggFPTA));
	     FPTA compFPTA= compressModels(aggFPTA);
	     System.out.println("size end "+PerformanceAnalyser.calculateModelSize(compFPTA));

	     DecimalFormat df = new DecimalFormat("0.000");
	     double avgEr=0;
	     double sharingSize=0;
	     int activeClients=0;
	     for(ClientOptimiser co:clients)
	     {
	    	 if(co.getState())
	    	 {
	    		 co.setGlobalModel(compFPTA);
	    		 activeClients++;
	    		 avgEr+=co.getGlobalModel().getCurrentFitness();
	    		 System.out.println("Client "+ co.getClientId()+"  Gmodel Er(" +df.format(co.getGlobalModel().getCurrentFitness())+") size of global model("+co.getGlobalModel().getSize()+ ") Lmodel Er("+df.format(co.getOptimiser().getBestFrontier().getFitness()[0])+") Size of Submodel ("+co.getSubgraphSize()+")"+" threshold("+co.getThreshold()/co.getOptimiser().getBestFrontier().getFitness()[1]+"%)"+" orginal size("+co.getOptimiser().getBestFrontier().getFitness()[1]+" --- "+co.getOptimiser().getBestFrontier().getFitness()[1]+")");
	    		 sharingSize+=co.getThreshold()/co.getOptimiser().getBestFrontier().getFitness()[1];
				// System.out.println("anonymity-->"+co.calculateAnonymity(compFPTA));

	    	 }
	    }
	     System.out.println("avgER("+avgEr/activeClients+")"+" avg sharing("+sharingSize/clients.size()+")");
	    getBaseLineAppaorch();
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void getBaseLineAppaorch() {
		
		List<FPTA> subgraphs= new ArrayList<FPTA>();
		for(ClientOptimiser co:clients)
			 subgraphs.add(co.getOptimiser().getBestFrontier().getFpta());
		 FPTA aggFPTA = unionModels(subgraphs);
		 FPTA comFPTA =  compressModels(aggFPTA);
	
		 int index=0;
		 double avgEr=0;
		 int activeClient=0;
		 
		for(ClientOptimiser co:clients)
		{
			HashMap<String,Double> res = co.evaluateAggregatedModel(comFPTA);
			int size = PerformanceAnalyser.calculateModelSize(comFPTA);
			// comFPTA.show(comFPTA,index+" after"+PerformanceAnalyser.calculateModelSize(comFPTA));
			//System.out.println("anonymity--> merge all-->"+co.calculateAnonymity(comFPTA));

			activeClient++;
				// aggFPTA.show(aggFPTA, ""+res.get("Size"));
			System.out.println("Client "+(index++) +" Size ("+res.get("Size")+") Er("+res.get("Entropic Relevance")+")"+" real size( "+size+")");
			avgEr+=res.get("Entropic Relevance");
		}
		System.out.println("ave Er without privacy("+avgEr/activeClient+")");
		
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public FPTA runAlgorithm() {
		 boolean flag=false;
		 FPTA aggFPTA = null ;
		 while(!flag)
		 {
			 retriveSubModels();
			 List<FPTA> subgraphs= new ArrayList<FPTA>();
			 for(ClientOptimiser co:clients)
				 subgraphs.add(co.getOptimiser().getSubFPTAModel());
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
		List<String> list= ALERGIA.getSubRoots(mergedDffa,ALERGIA.listNonCycle1(mergedDffa));
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
         System.out.println("Clients extracted their models ... "+ sdf.format(new Date()));
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
	        System.out.println("Clients started extracting their submodels ... "+ sdf.format(new Date()));
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
	         System.out.println("Clients extracted their models ... "+ sdf.format(new Date()));
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
		 
		 
		  for(double threshold=0.05;threshold<=0.951;threshold+=0.05)
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
				 // System.out.println("anonymity-->"+co.calculateAnonymity(comFPTA));
			  }			
			  
			  System.out.println(threshold+" Erg("+avgEr+") subEr("+avesubEr+")->"+((avesubEr-avgEr)/clients.size())+ "avg size "+avgSize+" pSize "+avgPSize);
		  }
		  System.out.println(1.0000000+" Erg("+avgEr+") subEr("+avglast+")->"+((avglast-avgEr)/clients.size()));

		  
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
	        }
	    	return parameterizedInput;
	    }
}
