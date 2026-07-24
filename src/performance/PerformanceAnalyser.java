package performance;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import model.FPTA;

public class PerformanceAnalyser {

    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public PerformanceAnalyser()
	{
		
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateFitness1(FPTA model, HashMap<String, Double> eventLog,String absent) {
		
		int totalSeenTrace =0;
		int totalLog=0;
		for(String s: eventLog.keySet())
		{
			totalSeenTrace+= calculateProbeblits(model,s,absent)*eventLog.get(s);
			totalLog += eventLog.get(s);
		}
		return ((double)totalSeenTrace/(double)totalLog)*100;
	}
	 /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
		public double calculateFitness1(FPTA model, HashMap<String, Double> eventLog) {
			
			int totalSeenTrace =0;
			int totalLog=0;
			for(String s: eventLog.keySet())
			{
				totalSeenTrace+= calculateProbeblits(model,s)*eventLog.get(s);
				totalLog += eventLog.get(s);
			}
			return ((double)totalSeenTrace/(double)totalLog)*100;
		}
	  /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
		public double calculateDFGFitness1(FPTA model, HashMap<String, Double> eventLog) {
			
			int totalSeenTrace =0;
			int totalLog=0;
			for(String s: eventLog.keySet())
			{
				totalSeenTrace+= calculateDFGProbeblits(model,s)*eventLog.get(s);
				totalLog += eventLog.get(s);
			}
			return ((double)totalSeenTrace/(double)totalLog)*100;
		}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateFitness(FPTA model, HashMap<String, Integer> eventLog) {
		double result =0;
		HashMap<String, Integer> modelState = new HashMap<String, Integer>();
		for(String s:model.states)
			modelState.put(s, 1);
		double moveM=calculateMove(model, modelState);
		double moveL= calculateMove(model, eventLog);
		double fCost = calculateFCost(model, eventLog);
		double logSize = 0;
		for(String s:eventLog.keySet())
			logSize+=eventLog.get(s);
		result = 1- (fCost/(moveL+ (logSize * moveM)));
		return result;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateMove(FPTA model, HashMap<String, Integer> eventLog)
	{
		double move=0;
		for(String s:eventLog.keySet())
			move+=s.length()*eventLog.get(s);
		return move;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateFCost(FPTA model, HashMap<String, Integer> eventLog) {
		double fcost=0;
		for(String trace:eventLog.keySet())
			fcost +=calculateDistance(model, trace)*eventLog.get(trace);
		return fcost;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public HashMap<String, Double> ReturnArrayOfFitness(FPTA fpta,HashMap<String, String> escapeList,HashMap<String, Double> eventLog){
		HashMap<String,Double> list = new HashMap<String, Double>();
		for(String s:fpta.getTransitionFunction().keySet())
			if(escapeList.containsKey(s))
			{
				list.put(s.substring(0,s.length()-1)+"-"+fpta.getTransitionFunction().get(s), calculateFitness1(fpta,eventLog,s));
			}
		return list;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public HashMap<String, String> calculateUnvisited(FPTA model,HashMap<String, Double> eventLog)
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
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateDistance(FPTA model, String trace) {
		
			String state ="";
			for(int i=0;i<trace.length();i++)
			{
				
				state = model.getTransitionFunction().get(state+trace.charAt(i));
				if (state == null)
				{
					return (trace.length())-i;
				}
			}
		
		return 0;

	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public int recCalculateSize(FPTA model) {
	    int result = 0;
	    Queue<String> queue = new ArrayDeque<>();
	    Set<String> visited = new HashSet<>();
	    if(model.states.contains(""))
	    	queue.offer("");  // Start from the initial state
	    else
	    	queue.offer("I"); 

	    while (!queue.isEmpty()) {
	        String curr = queue.poll();
	        if (!visited.add(curr)) continue; // Already visited
	        result++;
	        for (String symbol : model.alphabet) {
	            String key = curr + symbol;
	            if (model.transitionFunction.containsKey(key)) {
	                String next = model.transitionFunction.get(key);
	                result++;
	                if (!visited.contains(next)) {
	                    queue.offer(next);
	                }
	            }
	        }
	    }
	    return result;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static double calculateShannonEntropy(FPTA model,HashMap<String,String> states,HashMap<String, Double> eventlog,double totalFre) {
    	double result=0;
    	model.calculateTransitionPercentage(model);
    	HashMap<String,List<String>>paths= new HashMap<String,List<String>>();
    	for(String path:states.keySet())
    	{
    		
    		if(model.getFinalFrequency(states.get(path))>0)
    		{
    			List<String> newPaths= new ArrayList<String>();
    			countPaths(model,"",states.get(path),15,newPaths);
    			paths.put(path,newPaths);
    		}
    	}
    	HashMap<String,Double>ent= new HashMap<String,Double>();
    	double entrop=0;
    	double si;
double total=0;
    	for(String s:paths.keySet())
    	{
    		
    		List <String> tp = paths.get(s);
    		 si= tp.size();
    		 if(si!=0)
    		entrop+=(1.0-(1.0/si));
    		total+=eventlog.get(s);
    		
    		//System.out.println((paths.get(s)).size()+" "+s+" "+entrop+" "+(1-(1/paths.get(s).size())));

    	/*	List<Double> probab = new ArrayList<Double>();
    		for(String s1:paths.get(s))
    		{
    			probab.add(calculatePathProbability(model,s1));
    			total+=probab.get(probab.size()-1);
    		}
    		List<Double> normprobab = new ArrayList<Double>();
    	
    		for(int i = 0;i<probab.size();i++)
    		{
    			
    			normprobab.add(probab.get(i)/total);
    		}

    		//	entrop+=(-1*(normprobab.get(i)*Math.log(normprobab.get(i)))/Math.log(2));
    		entrop+=(1-(1/probab.size()));*/
    	}
    	
    	return entrop/paths.size();
    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static double calculatePathProbability(FPTA model,String path) {
    	double result=1;
    	StringTokenizer st = new StringTokenizer(path,",");
    	String curr="";
    	while(st.hasMoreTokens())
    	{
    		String state=st.nextToken();
    		char sym = state.charAt(state.indexOf("(")+1);
    		if(state.indexOf("(")>=0)
    		{
    			String next=model.transitionFunction.get(curr+sym);
        		result*=model.transitionPercentage.get(curr).get(sym+"").get(next);
        		curr=next;
    		}
    		else
    			result*=model.finalProbabilities.get(curr);
    		
    	}
    	  DecimalFormat df = new DecimalFormat("0.0000000");
    	return result;
    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static HashMap<String,String> getListofFinalState(FPTA model,HashMap<String, Double> eventLog,List<Double> freqList){
    	HashMap<String,String> finalState= new HashMap<String, String>();
    	for(String s:eventLog.keySet())
    	{
    		String state = "";
    		boolean flag=false;
    		for(char symbol:s.toCharArray())
    		{
    			String next=model.transitionFunction.get(state+symbol);
    			if(next==null)
    			{
    				flag=true;
    				break;
    			}
    			state=next;
    		}
    		if(!flag)
    		{
    			finalState.put(s, state);
    			freqList.add(eventLog.get(s));
    		}
    	}
    	return finalState;
    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static void countPaths(FPTA model, String fromState, String toState, int maxLength,List<String> path) {
      //  countPathsHelper(model, fromState, toState, maxLength,paths,fromState);
    	Map<String, List<String>> memo = new HashMap<>();

    	List<String> paths = new ArrayList<>();
    	countPathsHelper(model, fromState, toState, maxLength,
    	                 paths, new StringBuilder(fromState));
    	path.addAll(paths);
    	//getPaths(model, fromState, toState, maxLength,paths);
    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
 /*   private static void countPathsHelper(FPTA model, String current, String target, int remainingLength,List<String> paths,String currentpath) {
      
      
        if (current.equals(target)) {
        	paths.add(currentpath);			
        	return;
        }
        if (remainingLength <= 0) {
            return ;
        }
        for(String symbol:model.alphabet)
        {
        		if(model.transitionFunction.containsKey(current+symbol))
        		{
        			String next = model.transitionFunction.get(current+symbol);
        			{
        				if(remainingLength>0)
        				{
        					countPathsHelper(model, next, target, (remainingLength - 1),paths,currentpath+"("+symbol+"),"+next);	
        				}
        			}
        		}
        }
        return;
    }*/
    private static void countPathsHelper(
            FPTA model,
            String current,
            String target,
            int remainingLength,
            List<String> paths,
            StringBuilder currentPath)
    {
        if (current.equals(target))
        {
            paths.add(currentPath.toString());
            return;
        }

        if (remainingLength <= 0)
            return;

        for (String symbol : model.alphabet)
        {
            String next = model.transitionFunction.get(current + symbol);

            if (next != null)
            {
                int oldLength = currentPath.length();

                currentPath.append("(")
                           .append(symbol)
                           .append("),")
                           .append(next);

                countPathsHelper(
                        model,
                        next,
                        target,
                        remainingLength - 1,
                        paths,
                        currentPath);

                currentPath.setLength(oldLength); // backtrack
            }
        }
    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static Map.Entry<String, Double> returnLastState(FPTA model,String trace) {
		String state="";
		String state1="";
		double value=0;
		AbstractMap.SimpleEntry text;
		for(char c:trace.toCharArray())
		{	
			state1 = model.getTransitionFunction().get(state+c);			
			if (state == null || !model.states.contains(state))
			{
				value =0;
				state=null;
				return new AbstractMap.SimpleEntry<>(state, value);
			}
		
		}
    	return new AbstractMap.SimpleEntry<>(state, value);

    }
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public static int calculateDFvMModelSize(FPTA model)
	{
		   int result = 0;
		    Queue<String> queue = new ArrayDeque<>();
		    Set<String> visited = new HashSet<>();
		    queue.offer("I");  // Start from the initial state

		    while (!queue.isEmpty()) {
		        String curr = queue.poll();
		        if (!visited.add(curr)) continue; // Already visited

		        result++;
		        for (String symbol : model.alphabet) {
		            String key = curr + symbol;
		            if (model.transitionFunction.containsKey(key)) {
		                String next = model.transitionFunction.get(key);
		                result++;
		                if (!visited.contains(next)) {
		                    queue.offer(next);
		                }
		            }
		        }
		    }
		    return result;
	}
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

	public static int calculateModelSize(FPTA model)
	{
		   int result = 0;
		    Queue<String> queue = new ArrayDeque<>();
		    Set<String> visited = new HashSet<>();
		    queue.offer("");  // Start from the initial state

		    while (!queue.isEmpty()) {
		        String curr = queue.poll();
		        if (!visited.add(curr)) continue; // Already visited

		        result++;
		        for (String symbol : model.alphabet) {
		            String key = curr + symbol;
		            if (model.transitionFunction.containsKey(key)) {
		                String next = model.transitionFunction.get(key);
		                result++;
		                if (!visited.contains(next)) {
		                    queue.offer(next);
		                }
		            }
		        }
		    }
		    return result;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public int calculateSize(FPTA model) {
	/*	int result =0;	
		for(String state:model.states)
		{
			result++;
			for(String symbol:model.alphabet)
				if(model.transitionFunction.containsKey(state+symbol))
				{
					//String next = model.transitionFunction.get(state+symbol);
					//if(model.transitionFrequencies.get(state).get(symbol).get(next)!=null)
					result++;
					try {
					//	String target = model.transitionFunction.get(state+symbol);
					//	if(model.getTransitionFrequencies().get(state).get(symbol).get(target)>0)
					//		result++;
					}
					catch(Exception e)
					{
						
					}
				}
		}*/

		return recCalculateSize(model);
		//return result;		
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    public double calculatePercision(FPTA model, HashMap<String, Double> eventLog) {
    	
    	double result=0;
    
    	HashMap<String, List<String>> stateTotalTransition = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> escapingTransition = new HashMap<String, List<String>>();
    	int TotalEdges=0;
    	List<String>temp;
    	for(String s:model.states)
    	{
    		stateTotalTransition.put(s, new ArrayList<String>());
    		escapingTransition.put(s, new ArrayList<String>());
    		for(String c:model.alphabet)
    		{
    			if(model.getTransitionFunction().get(s+c)!=null)
    			{
    				stateTotalTransition.get(s).add(c);
    				escapingTransition.get(s).add(c);
    				TotalEdges++;
    			}
    		}		
    	}
    	String state;
    	for(String e:eventLog.keySet())
    	{
    		state ="";
    		for(char c: e.toCharArray())
    		{
    			if(model.getTransitionFunction().containsKey(state+c))
    			{
    				try {
    					escapingTransition.get(state).remove(c+"");				
    					state= model.getTransitionFunction().get(state+c);		
    				}
    				catch(Exception e1)
    				{
    					break;
    				}
    			}
    		}
    	}
    	int totalEscapeing=0;
    	for(String s: escapingTransition.keySet())
    		totalEscapeing+=escapingTransition.get(s).size();
    	return ((double)1-(double)((double)totalEscapeing/(double)TotalEdges)) * 100;
    }
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateGeneraliation( FPTA model,HashMap<String, Integer> eventLog) {
		double result = 0;
  
    	HashMap<String,Integer> visitedStates= new HashMap<>();
    	HashMap<String,Integer> events = new HashMap<>();
    	for(String state:model.states)
    		for(String action: model.alphabet)
    			if(model.transitionFunction.get(state+action) != null)
    			{
    				if(!visitedStates.containsKey(state))
    					visitedStates.put(state,1);
    				else
    					visitedStates.put(state, visitedStates.get(state)+1);
    			}
    	double totalEvent=0;
    	for(String s: eventLog.keySet())
    	{
    		String state="";
    		totalEvent+= eventLog.get(s)*s.length();
    		for(char c:s.toCharArray())
    		{
    			state = model.getTransitionFunction().get(state+c);
    			if(state !=null)
    			{
    				if(!events.containsKey(state))
    					events.put(state,1);
    				else
    					events.put(state,events.get(state)+ eventLog.get(s));
    			}
    		}
    	} 
    	for(String state: events.keySet())
    		result = calculatePNEW(visitedStates.get(state)!=null?visitedStates.get(state):0,events.get(state));
    	result = result/totalEvent;
    	result = 1-result;
    	result = result * 100;
    
    	return result;
	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculatePNEW(double a,double b) {
		if(b>1)
			return (a*(a+1))/(b*(b-1));
		else
			return 1;
	}	
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateProbeblits(FPTA model,String trace) {
		String state="",state1="";
	
		for(char c:trace.toCharArray())
		{			
			
			state = model.getTransitionFunction().get(state+c);			
			if (state == null || !model.states.contains(state))
			{
				return 0;
			}
		}
		return 1;
	}
	/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
	public double calculateProbeblits(FPTA model,String trace,String absentLink) {
		String state="",state1="";
		
		for(char c:trace.toCharArray())
		{			
			state1 =state;
			state = model.getTransitionFunction().get(state+c);	
			
			if (absentLink.compareTo(state1+c)==0 || state == null || !model.states.contains(state))
			{
			
				return 0;
			}
		}

		return 1;
	}
	 /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/
		public double calculateDFGProbeblits(FPTA model,String trace) {
			String state="I",state1="I";
			trace+="O";
			for(char c:trace.toCharArray())
			{			
				
				state = model.getTransitionFunction().get(state+c);			
				if (state == null || !model.states.contains(state))
				{
					return 0;
				}
			}
			return 1;
		}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/


}
