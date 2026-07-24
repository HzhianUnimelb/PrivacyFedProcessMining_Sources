package utilities;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import model.ALERGIA;
import model.DFFA;
import model.FPTA;
import model.SDAG;
import nodes.ClientOptimiser;
import optimization.DFvMOptimisation;
import optimization.Frontier;
import optimization.Optimization;
import optimization.evolutionBasedSolutions.DifferentialEvolution;
import optimization.evolutionBasedSolutions.GASPD;
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
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;


public class SolutionScanner {
    Optimization optimiser;
    private PerformanceAnalyser performanceAnalyser;
    private EntropicRelevanceCalculator entropicRelevanceCalculator;
    private LogParser logParser;
    private SimpleDateFormat sdf ;
    private boolean OPTFlag;
    private List<String> nonCycleList;
    public static void removeUnvisited(FPTA dtemp, HashMap<String, Double> eventLog) {
    		HashMap<String, String> xxxx = calculateUnvisited(dtemp,eventLog);
    		HashMap<String,Double> up = new HashMap<String, Double>();
    		//System.out.println(xxxx.size());
    		for(String x:xxxx.keySet())
    		{
    			try {
    			String temp = dtemp.transitionFunction.get(x);
    			String state = x.substring(0, x.length()-1);
    			String symbol = x.substring(x.length()-1);
    		//	System.out.println(state+" "+symbol+" "+temp);
    			dtemp.transitionFrequencies.get(state).get(symbol).remove(temp);
    			double value = dtemp.transitionPercentage.get(state).get(symbol).remove(temp);
    			if(up.containsKey(state))
    			{
    				up.put(state, up.get(state)+value);
    			}
    			else {
    				up.put(state,value);
    			}
    			dtemp.transitionFunction.remove(x);		
    			}catch(Exception e)
    			{
    				System.out.println(e);
    			}
    		}
    		for(String s:up.keySet())
    		{

    			double value=up.get(s);
    			double total=0;
    			for(String symbol:dtemp.alphabet)
    			{
    				
    				if(dtemp.getTransitionFunction().get(s+symbol)!=null)
    				{
    				
    					double prob = dtemp.transitionPercentage.get(s).get(symbol).get(symbol);
    					double remain=100-value;
    					double newProb=(prob*100/remain);
    				
    					dtemp.setTransitionPercentage(s, symbol, symbol, newProb);
    				}
    			}
    		}
    }
    
    
    public static HashMap<String, String> calculateUnvisited(FPTA model,HashMap<String, Double> eventLog)
	{
		HashMap<String, String> escapingTransition = new HashMap<String, String>();
		for(String s:model.getTransitionFunction().keySet())
		{
			
			escapingTransition.put(s, model.getTransitionFunction().get(s));
		}
		for(String s:eventLog.keySet())
		{
			s+='O';
			String state="I",state1="I";

			for(char c:s.toCharArray())
			{	
				state1=state;
				state = model.getTransitionFunction().get(state+c);			
				if (state == null || !model.states.contains(state))
				{
					
					break;
				}
				else
					escapingTransition.remove(state1+c, state);
			}
		}
		return escapingTransition;
	}
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static FPTA extractModel(double i, double j,double z,HashMap<String, Double>  eventLog,int action,SimpleDateFormat sdf) {
    	System.out.println("start z-->"+z);
		

		HashMap<String, Double> filterEventLog = FrequencyBasedFiltering.filterEventLog(eventLog, z);		 
	 	 System.out.println("ALERGIA STARTED... "+ sdf.format(new Date()));

		FPTA currentFPTA = new ALERGIA(i, j,filterEventLog,eventLog,action).run(); 
		System.out.println("Conversion TO SDAG... "+ sdf.format(new Date()));
 	    
 	    return currentFPTA;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
   public static double evalulateModel(FileWriter myWriter,double i,double j, double z, FPTA currentFPTA,FPTA dtemp,HashMap<String, Double> eventLog,int actions) throws IOException {
	   
   
  
	PerformanceEstimator PEDFFA = new PerformanceEstimator(BackGroundType.U);
	HashMap<String,Double> pedffa =PEDFFA.calculatePerformanceMetrics(currentFPTA, eventLog,actions);

	 PerformanceEstimator PE = new PerformanceEstimator(BackGroundType.U);
	HashMap<String,Double> pe =PE.calculatePerformanceDFGMetrics(dtemp, eventLog,actions);
	double DFGfitness[]= {pe.get("Entropic Relevance"),pe.get("Size")};
	double bestv = Double.MAX_VALUE;
    double DFFAsize = pedffa.get("Size");
	myWriter.write("("+i+" "+j+" "+z+")("+ DFGfitness[0]+" "+DFGfitness[1]+")\n");

    return DFFAsize;
   }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static void scanProcessDiscovery(String[] args) {
    	HashMap<String,String> parms = processInput(args);
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
		String fileDirectory = parms.get("LOG_DIRECTORY"); 
          HashMap<String, Character> actions = new HashMap<String, Character>();
          LogParser logParser =  new LogParser(fileDirectory);
          HashMap<String, Double> eventLog = logParser.extractEvent(actions);
          FrequencyBasedFiltering filtering = new FrequencyBasedFiltering();
          
 		// fixFPTA.show(fixFPTA, "first model");
      
		BackGroundType bkgt = BackGroundType.U; 

		
		if(parms.get("Entropic Relevance Background Model").compareTo("U")==0)
			bkgt=BackGroundType.U;
		List<double[]> pareto = new ArrayList<double[]>();
		List<FPTA> fptaList = new ArrayList<FPTA>();
		
    	for(double i=0.0000000000001;i<=0.0000000000001;i+=0.0000000000001)
    	{
    	
    		for(double j=100;j<= 100;j+=10)
    		{
    			System.out.println("start j-->"+j);
    			for(double z=0.5;z<=0.99;z+=0.01)
    			{
    				FileWriter myWriter = null;
    				try {
    					myWriter = new FileWriter("nasa3.txt",true);
    				}catch(Exception e)
    				{
    					
    				}
       		 	  	System.out.println("Conversion TO DFG "+ sdf.format(new Date()));
       		 	  	FPTA currentFPTA = new FPTA();
       		 	  	
       		 	  	  currentFPTA= extractModel(i,j,z,eventLog,actions.size(),sdf);
       		 	  
       		 	  	FPTA sdfg = SDAG.DFFAtoSDAG(currentFPTA);
       		 	  	
					FPTA dtemp = DFFA.getDFG(sdfg);
				 	removeUnvisited(dtemp,eventLog);

    		 //		 System.out.println("submodels extraction "+ sdf.format(new Date()));
    		 	
				    List<Double> values = new ArrayList<Double>();
				    List<Double> values1 = new ArrayList<Double>();
				    double DFFAsize = 0 ;
				    try {
				    	DFFAsize = evalulateModel(myWriter,i,j,z,currentFPTA,dtemp,eventLog,actions.size());
				    }catch(Exception e)
					{
						
					}
				    for(double threshold=0.001;threshold<=0.99;threshold+=0.01)
					{

							 	FPTA tempf =SubgraphSolver.solveDFFAOptimization(currentFPTA,(int)(threshold*DFFAsize)+1);
							 	
							 	FPTA stemp = SDAG.DFFAtoSDAG(tempf);
							 	FPTA dtemp1 = DFFA.getDFG(stemp);
							 	
							 	PerformanceEstimator PE2 = new PerformanceEstimator(BackGroundType.U);
							     HashMap<String,Double> pe2 =PE2.calculatePerformanceDFGMetrics(dtemp1,eventLog,actions.size());								
								 double beforefit[]= {pe2.get("Entropic Relevance"),pe2.get("Size")};
						 	removeUnvisited(dtemp1,eventLog);
						
                         //removeUnvisited(dtemp1, filterEventLog);
					    PE2 = new PerformanceEstimator(BackGroundType.U);
					 pe2 =PE2.calculatePerformanceDFGMetrics(dtemp1,eventLog,actions.size());								
						 double fit[]= {pe2.get("Entropic Relevance"),pe2.get("Size")};
						 
						 if((!values.contains(fit[0])||!values1.contains(fit[1])) && fit[1]>=1)
						 {
							 values.add(fit[0]);
							 values1.add(fit[1]);
							 try {
								 myWriter.write("("+i+","+j+","+z+")"+"("+fit[0]+" "+fit[1]+")\n");
							 }catch(Exception e)
							 {
								 
							 }
						}
						 	
					 }
					try {
						myWriter.close();
					}catch(Exception e)
					 {
						 
					 }
    			} 
    		}
    		System.out.println("i-->"+i);
    	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public SolutionScanner(int iteration,int population,String optName,String fileDirectory,boolean ParetoFront,boolean OPTFlag,double lower,double upper,int Frontier_List_Size,String symbol,LocalDateTime time,int seconds,BackGroundType bkgt,String optModel,int sizeLimit,double cof) {
    	sdf = new SimpleDateFormat("hh:mm:ss:SSS");
    	this.OPTFlag = OPTFlag;
    	performanceAnalyser = new PerformanceAnalyser();
        logParser = new LogParser(fileDirectory);
       
        HashMap<String, Character> actions = new HashMap<String, Character>();
        HashMap<String, Double> eventLog = logParser.extractEvent(actions);
        System.out.println("Extracting Log Started... "+ sdf.format(new Date()));
        if (OPTFlag) {
        	if (optName.compareTo("DFvM") == 0)
            	optimiser = new DFvMOptimisation(lower,upper,fileDirectory,Frontier_List_Size,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            if (optName.compareTo("PSO") == 0)
                optimiser = new PSO(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("GEN") == 0)
                optimiser = new Genetic(0, iteration, fileDirectory, population, 0.8, 0.0,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("GASPD") == 0)
                optimiser = new GASPD(0, iteration, fileDirectory, population, 0.8, 0.0,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("BEE") == 0) 
                 optimiser = new ACB(0, iteration, fileDirectory, population, 5,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("DE") == 0)
                 optimiser = new DifferentialEvolution(0, iteration, fileDirectory, population, 0.5, 0.9,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("GSA") == 0)
                 optimiser = new GravitationalSearch(0, iteration, fileDirectory, population, 100,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("ACO") == 0)
                 optimiser = new ACO(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("CS") == 0)
                 optimiser = new CS(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("WO") == 0)
                 optimiser = new  WO(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("BLH") == 0)
                 optimiser = new  BlackHole(0, iteration, fileDirectory, population, 0.1,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("SOS") == 0)
                 optimiser = new  SOS(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);
            else if (optName.compareTo("FA") == 0)
                 optimiser = new  FA(0, iteration, fileDirectory, population,lower,upper,Frontier_List_Size,time,seconds,actions,eventLog,actions.size(),bkgt,optModel,sizeLimit,cof);         
        } 
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<FPTA> rotateList(List<FPTA> fptaList){
    	Collections.rotate(fptaList, 1);
    	return fptaList;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public FPTA retoreModel() {
    	FPTA fpta = new FPTA();
    	try {
    		
			FileReader fr = new FileReader(".\\model\\actions.txt");
			BufferedReader in = new BufferedReader(fr);  
			Iterator<String> l;
			Stream<String> lines;
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				fpta.alphabet.add(l.next());
			}
			fr = new FileReader(".\\model\\state.txt");
			in = new BufferedReader(fr);  
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				String temp = l.next();
				if(temp.compareTo("I")==0)
					temp="";
				fpta.states.add(temp);
			}
			fr = new FileReader(".\\model\\TransitionState.txt");
			in = new BufferedReader(fr);  
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				StringTokenizer st = new StringTokenizer(l.next(),",");
				String source = st.nextToken();
				
				fpta.transitionFunction.put(source,st.nextToken());
			}
			fr = new FileReader(".\\model\\Transitionfrequency.txt");
			in = new BufferedReader(fr);  
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				String temp =l.next();
				StringTokenizer st = new StringTokenizer(temp,",");
				String source = st.nextToken();
				if(source.compareTo("I")==0)
					source ="";
				fpta.setTransitionFrequency(source,""+st.nextToken(), st.nextToken(),Double.parseDouble(st.nextToken()));
			}
			fr = new FileReader(".\\model\\Finalfrequency.txt");
			in = new BufferedReader(fr);  
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				StringTokenizer st = new StringTokenizer(l.next(),",");
				String source = st.nextToken();
				if(source.compareTo("I")==0)
					source ="";
				fpta.setFinalFrequency(source, Double.parseDouble(st.nextToken()));
			}
			fr = new FileReader(".\\model\\Initialfrequency.txt");
			in = new BufferedReader(fr);  
			lines = in.lines();
			l = lines.iterator();
			while(l.hasNext())
			{
				StringTokenizer st = new StringTokenizer(l.next(),",");
				String source = st.nextToken();
				if(source.compareTo("I")==0)
					source ="";
				fpta.setInitialFrequency(source, Double.parseDouble(st.nextToken()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return fpta;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void storeModel(FPTA fpta) {
    	try {
			FileWriter fw = new FileWriter(".\\model\\actions.txt");
			for(String S:fpta.alphabet)
	    	{
	    		fw.write(S+"\n");
	    	}
			
			fw.close();
			fw = new FileWriter(".\\model\\state.txt");
			for(String S:fpta.states)
	    	{
				if(S.compareTo("")==0)
					S="I";
	    		fw.write(S+"\n");
	    	}
			fw.close();
			fw = new FileWriter(".\\model\\TransitionState.txt");
			for(String S:fpta.transitionFunction.keySet())
	    	{
	    		fw.write(S+","+fpta.transitionFunction.get(S)+"\n");
	    	}
			fw.close();
			fw = new FileWriter(".\\model\\Transitionfrequency.txt");
			for(String S:fpta.transitionFrequencies.keySet())
	    	{
				String S1=S;
				if(S1.length()==0)
					S1="I"+S;
				
				for(String symbol:fpta.transitionFrequencies.get(S).keySet())
				{
					String target = fpta.transitionFunction.get(S+symbol);
					if(target!=null)
						fw.write(S1+","+symbol+","+target+","+fpta.transitionFrequencies.get(S).get(symbol).get(target)+"\n");
				}
	    	}
			fw.close();
			fw = new FileWriter(".\\model\\Finalfrequency.txt");
			for(String S:fpta.finalFrequencies.keySet())
	    	{
				String S1=S;
				if(S1.length()==0)
					S1="I"+S;
	    		fw.write(S1+","+fpta.finalFrequencies.get(S)+"\n");
	    	}
			fw.close();
			fw = new FileWriter(".\\model\\Initialfrequency.txt");
			for(String S:fpta.initialFrequencies.keySet())
	    	{
				String S1=S;
				if(S1.length()==0)
					S1="I"+S;
	    		fw.write(S1+","+fpta.initialFrequencies.get(S)+"\n");
	    	}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
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
            else if("-dci".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("DCI_SIZE", args[i+1]);
    			i++;
    		}
            else if("-erbm".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Entropic Relevance Background Model", args[i+1]);
    			i++;
    		}
            else if("-optm".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Optimal Model", args[i+1]);
    			i++;
    		}
            else if("-mms".equals(args[i]) && i + 1 < args.length) {
    			parameterizedInput.put("Maximum Model Size", args[i+1]);
    			i++;
    		}
        }
    	return parameterizedInput;
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
    public static void main(String[] args) {
    	
    	scanProcessDiscovery(args);
    }
    
   
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<Frontier> ExtractParetoList() {
    	System.out.println("Extracting Models Started... "+ sdf.format(new Date()));
        optimiser.run();
    	return optimiser.getFrontiers();
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void printReport(FPTA mergedDffa, HashMap<String, Double> log) {
    	System.out.println("Performance Analysing Started... "+ sdf.format(new Date()));
    	System.out.println("*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
    	System.out.println("Percision is: "+ performanceAnalyser.calculatePercision(mergedDffa, log));
		System.out.println("Size is "+performanceAnalyser.calculateSize(mergedDffa));
		System.out.println("fitness1 is: "+ performanceAnalyser.calculateFitness1(mergedDffa, log));
		HashMap<String, Character> readMapList = logParser.readMapList("actionMap.txt");
    	int actionListSize = readMapList.size();
		entropicRelevanceCalculator = new EntropicRelevanceCalculator(mergedDffa,log,actionListSize,BackGroundType.Z);
		System.out.println("ER is: "+ entropicRelevanceCalculator.getER());

    }
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
	public List<String> getNonCycleList() {
		return nonCycleList;
	}
	public void setNonCycleList(List<String> nonCycleList) {
		this.nonCycleList = nonCycleList;
	}
}
