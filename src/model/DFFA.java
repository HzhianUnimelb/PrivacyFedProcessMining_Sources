package model;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JFrame;


import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import performance.EntropicRelevanceCalculator;
import performance.EntropicRelevanceCalculator.BackGroundType;
import performance.PerformanceAnalyser;
import performance.PerformanceEstimator;
import utilities.CoefficientMatrix;
import utilities.SubgraphSolver;

public class DFFA extends Model{
   
    public HashMap<String, Double> getEventLog() {
		return getEventLog();
	}
   
    public DFFA(Set<String> states, Set<String> alphabet)
    {
    	super(states,alphabet,MODELTYPE.DFFA);
    }
    public DFFA() {
    	super(MODELTYPE.DFFA);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public double calculateOutcommingArcs(DFFA dffa1,String state,Map<String,Double> outValues,Map<String,String> outSymbol) {
    	long result=0;
    	for(String a: dffa1.alphabet)
    	{
    		for(String nextstate:dffa1.states)
    		{
    			if(dffa1.getTransitionFunction().get(state + a)!=null&&dffa1.getTransitionFunction().get(state + a).compareTo(nextstate)==0)
    			{	
    				double x=0;
    				if(dffa1.getTransitionFrequencies().get(state).get(a).get(dffa1.getTransitionFunction().get(state + a))==null)
    					dffa1.setTransitionFrequency(state, a, dffa1.getTransitionFunction().get(state + a), 0);
    				else
    					x = dffa1.getTransitionFrequencies().get(state).get(a).get(dffa1.getTransitionFunction().get(state + a));
    				result +=x;
    				outValues.put(a, x);
    				outSymbol.put(a, dffa1.getTransitionFunction().get(state + a));
    			}
    		}
    	}
    	return result;
    }
    
    public double calculateOutcommingArcs(DFFA dffa1,String state) {
    	long result=0;
    	for(String a: dffa1.alphabet)
    	{
    			if(dffa1.getTransitionFunction().get(state + a)!=null)
    			{	
    				double x=0;
    				if(dffa1.getTransitionFrequencies().get(state).get(a).get(dffa1.getTransitionFunction().get(state + a))==null)
    					dffa1.setTransitionFrequency(state, a, dffa1.getTransitionFunction().get(state + a), 0);
    				else
    					x = dffa1.getTransitionFrequencies().get(state).get(a).get(dffa1.getTransitionFunction().get(state + a));
    				result +=x;
    				
    			}
    	}
    	result+=dffa1.finalFrequencies.get(state)!=null?dffa1.finalFrequencies.get(state):0;
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public long calculateIncomingArcs(DFFA dffa1,String state,Map<String,Double> inValues,Map<String,String> insymbol) {
    	int result=0;
    	for(String a: dffa1.alphabet)
    	{
    		for(String prevState:dffa1.states)
    		if( dffa1.getTransitionFunction().get(prevState + a) != null && dffa1.getTransitionFunction().get(prevState + a).compareTo(state)==0&& prevState.compareTo(state)!=0)
    		{
    			double x = dffa1.getTransitionFrequencies().get(prevState).get(a).get(state);
    			result += x;
    			inValues.put(prevState, x);
    			insymbol.put(prevState, a);
    		}
    	}
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
  

    public double calculateIncomingArc(DFFA dffa1,String state,Map<String,Double> symbolcof,Map<String,String> symbolOut) {
    	int result=0;
    	for(String a: dffa1.alphabet)
    	{
    		for(String prevState:dffa1.states)
    		if( dffa1.getTransitionFunction().get(prevState + a) != null && dffa1.getTransitionFunction().get(prevState + a).compareTo(state)==0)
    		{
    			double x = dffa1.transitionPercentage.get(prevState).get(a).get(state);		
    			symbolcof.put(a,x);
    			symbolOut.put(a,prevState);
    		}
    	}
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public Map<String, String> getTransitionFunction() {
        return transitionFunction;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void show(DFFA dffa,String name) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        ;
        List<String> sortedStates = dffa.getStates().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
 /*       for(String s:sortedStates)
        {
        	if(s.charAt(0)=='λ')
        	{
        		sortedStates.remove(s);
        		sortedStates.add(0, s);
        		break;
        	}
        }*/
        
        int x = 500;
        int y = 100;
        int level = 0;
        int vertexWidth = 80;
        int vertexHeight = 20;
        int horizontalGap = 20;
        int verticalGap = 20;

        Map<String, Object> vertices = new HashMap<>();

        for (String state : sortedStates) {
            String vertexName = (state.isEmpty() ? "λ" : state) + ":" + String.format("%.3f",dffa.getFinalFrequencies().getOrDefault(state, (double)0)) + "";
            if (dffa.getInitialFrequencies().containsKey(state)) {
                vertexName = "IP(" + dffa.getInitialFrequencies().get(state) + "):" + vertexName;
            }
           
            if (state.length() == 0) {
                Object vertex = graph.insertVertex(parent, null, vertexName, x, y, vertexWidth, vertexHeight);
      
                vertices.put(state, vertex);
            } else  {
 
                String prefix = state.substring(0, state.length() - 1);
                Object prefixVertex = vertices.get(prefix);
                if(prefixVertex==null)
                	prefixVertex = vertices.get("");
                	
                	int prefixVertexWidth = (int) graph.getCellGeometry(prefixVertex).getWidth();
                	int prefixVertexHeight = (int) graph.getCellGeometry(prefixVertex).getHeight();
                	int currentVertexWidth = Math.max(vertexWidth, vertexName.length() * 10);
                	int currentVertexHeight = vertexHeight;             
                	Object vertex = graph.insertVertex(parent, null, vertexName, (int) graph.getCellGeometry(prefixVertex).getX() + (prefixVertexWidth + horizontalGap), (int) graph.getCellGeometry(prefixVertex).getY() + (prefixVertexHeight + verticalGap), currentVertexWidth, currentVertexHeight);
                    vertices.put(state, vertex);      
                
            }
        }

        for (String fromState : dffa.getStates()) {

            for (String symbol : dffa.getAlphabet()) {   
                String nextState = dffa.getTransitionFunction().get(fromState + symbol);
                if (nextState != null) {
                    String fromVertexName = (fromState.isEmpty() ? "λ" : fromState) + ":" + String.format("%.3f",dffa.getFinalFrequencies().getOrDefault(fromState, (double)0)) + "";
                    if (dffa.getInitialFrequencies().containsKey(fromState)) {
                        fromVertexName = "IP(" + dffa.getInitialFrequencies().get(fromState) + "):" + fromVertexName;
                    }
                    String toVertexName = (nextState.isEmpty() ? "λ" : nextState) + ":" + String.format("%.3f",dffa.getFinalFrequencies().getOrDefault(nextState, (double)0)) + "";
                    if (dffa.getInitialFrequencies().containsKey(nextState)) {
                        toVertexName = "IP(" + dffa.getInitialFrequencies().get(nextState) + "):" + toVertexName;
                    }
                    String edgeName = symbol + ":" + String.format("%.3f",dffa.getTransitionFrequencies().get(fromState).get(symbol).get(nextState));
                    Object fromVertex = getVertex(graph, parent, fromVertexName);
                    Object toVertex = getVertex(graph, parent, toVertexName);
                    graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
                }
            }
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
       
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public void showDFvM(DFFA dffa,String name) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        ;
        List<String> sortedStates = dffa.getStates().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
 /*       for(String s:sortedStates)
        {
        	if(s.charAt(0)=='λ')
        	{
        		sortedStates.remove(s);
        		sortedStates.add(0, s);
        		break;
        	}
        }*/
        
        int x = 500;
        int y = 100;
        int level = 0;
        int vertexWidth = 80;
        int vertexHeight = 20;
        int horizontalGap = 20;
        int verticalGap = 20;

        Map<String, Object> vertices = new HashMap<>();
        for (String state : sortedStates) {
        	 String vertexName = state;
             Object vertex = graph.insertVertex(parent, null, vertexName, x, y, vertexWidth, vertexHeight);
             vertices.put(state, vertex);
             int prefixVertexWidth = (int) graph.getCellGeometry(vertex).getWidth();
             int prefixVertexHeight = (int) graph.getCellGeometry(vertex).getHeight();
          	 int currentVertexWidth = Math.max(vertexWidth, vertexName.length() * 10);
          	 int currentVertexHeight = vertexHeight;             
         // 	 vertex = graph.insertVertex(parent, null, vertexName, (int) graph.getCellGeometry(vertex).getX() + (prefixVertexWidth + horizontalGap), (int) graph.getCellGeometry(vertex).getY() + (prefixVertexHeight + verticalGap), currentVertexWidth, currentVertexHeight);      	 
        }
        for (String state : sortedStates) {
        	for(String state1:sortedStates)
        	{
        		if(dffa.transitionFunction.get(state+state1)!=null)
        		{
        			Object prefixVertex = vertices.get(state);
        			String edgeName =""+dffa.getTransitionFrequencies().get(state).get(state1).get(state1);
        			Object fromVertex = getVertex(graph, parent, state);
        			Object toVertex = getVertex(graph, parent, state1);
        			graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
       		 
        		}
        	}
        }
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
       
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void showfirst(DFFA dffa,String name) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        ;
        List<String> sortedStates = dffa.getStates().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
 /*       for(String s:sortedStates)
        {
        	if(s.charAt(0)=='λ')
        	{
        		sortedStates.remove(s);
        		sortedStates.add(0, s);
        		break;
        	}
        }*/
        
        int x = 500;
        int y = 100;
        int level = 0;
        int vertexWidth = 80;
        int vertexHeight = 20;
        int horizontalGap = 20;
        int verticalGap = 20;

        Map<String, Object> vertices = new HashMap<>();

        for (String state : sortedStates) {
            String vertexName = (state.isEmpty() ? "I" : state);
            Object vertex = graph.insertVertex(parent, null, vertexName, x, y, vertexWidth, vertexHeight);
            vertices.put(state, vertex);
        }
        for (String fromState : dffa.getStates()) {

            for (String symbol : dffa.getAlphabet()) {   
                String nextState = dffa.getTransitionFunction().get(fromState + symbol);
                if (nextState != null) {
                    String fromVertexName = (fromState.isEmpty() ? "I" : fromState) ;
                 
                    String toVertexName = (nextState.isEmpty() ? "I" : nextState) ;
                 
                    String edgeName = symbol + ":" + dffa.transitionFrequencies.get(fromState).get(symbol).get(nextState);
                    Object fromVertex = getVertex(graph, parent, fromVertexName);
                    Object toVertex = getVertex(graph, parent, toVertexName);
                    graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
                }
            }
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
       
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public void showDFG(DFFA dffa,String name) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        ;
        List<String> sortedStates = dffa.getStates().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
 /*       for(String s:sortedStates)
        {
        	if(s.charAt(0)=='λ')
        	{
        		sortedStates.remove(s);
        		sortedStates.add(0, s);
        		break;
        	}
        }*/
        
        int x = 500;
        int y = 100;
        int level = 0;
        int vertexWidth = 80;
        int vertexHeight = 20;
        int horizontalGap = 20;
        int verticalGap = 20;

        Map<String, Object> vertices = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.###");
        for (String state : sortedStates) {
            String vertexName = state;
            Object vertex = graph.insertVertex(parent, null, vertexName, x, y, vertexWidth, vertexHeight);
        }

        for (String fromState : dffa.getStates()) {

            for (String symbol : dffa.getAlphabet()) {   
            	
                String nextState = dffa.getTransitionFunction().get(fromState + symbol);
                if (nextState != null) {
                    String fromVertexName = fromState;
                   
                 
                  
                    String toVertexName = nextState;
                 
                    String edgeName = df.format(dffa.transitionFrequencies.get(fromState).get(symbol).get(nextState));
                    Object fromVertex = getVertex(graph, parent, fromVertexName);
                    Object toVertex = getVertex(graph, parent, toVertexName);
                    graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
                }
            }
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
       
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public void showPercentage(DFFA dffa,String name) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        DecimalFormat df = new DecimalFormat("0.000");
        List<String> sortedStates = dffa.getStates().stream()
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
 /*       for(String s:sortedStates)
        {
        	if(s.charAt(0)=='λ')
        	{
        		sortedStates.remove(s);
        		sortedStates.add(0, s);
        		break;
        	}
        }*/
        
        int x = 500;
        int y = 100;
        int level = 0;
        int vertexWidth = 80;
        int vertexHeight = 20;
        int horizontalGap = 20;
        int verticalGap = 20;
        Map<String, Object> vertices = new HashMap<>();
   
        for (String state : sortedStates) {
            String vertexName = state;
            Object vertex = graph.insertVertex(parent, null, vertexName, x, y, vertexWidth, vertexHeight);
        }
  
        for (String fromState : dffa.getStates()) {

            for (String symbol : dffa.getAlphabet()) {   
            	
                String nextState = dffa.getTransitionFunction().get(fromState + symbol);
                if (nextState != null) {
                    String fromVertexName = fromState;
                   
                 
                  
                    String toVertexName = nextState;
                 
                    String edgeName = df.format(dffa.transitionPercentage.get(fromState).get(symbol).get(nextState));
                    Object fromVertex = getVertex(graph, parent, fromVertexName);
                    Object toVertex = getVertex(graph, parent, toVertexName);
                    graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
                }
            }
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
       
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    private static Object getVertex(mxGraph graph, Object parent, String vertexName) {
        for (Object vertex : graph.getChildVertices(parent)) {
            if (vertexName.equals(graph.getLabel(vertex))) {
                return vertex;
            }
        }
        return null;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public boolean isConsistent() {
        for (String state : states) {
            double leftSide = initialFrequencies.getOrDefault(state, (double)0);
            for (String nextState : states) {
                for (String symbol : alphabet) {
                    if (transitionFrequencies.containsKey(nextState) && transitionFrequencies.get(nextState).containsKey(symbol) && transitionFrequencies.get(nextState).get(symbol).containsKey(state)) {
                        leftSide += transitionFrequencies.get(nextState).get(symbol).get(state);
                    }
                }
            }

            double rightSide = finalFrequencies.getOrDefault(state, (double)0);
            for (String symbol : alphabet) {
                for (String nextState : states) {
                    if (transitionFrequencies.containsKey(state) && transitionFrequencies.get(state).containsKey(symbol) && transitionFrequencies.get(state).get(symbol).containsKey(nextState)) {
                        rightSide += transitionFrequencies.get(state).get(symbol).get(nextState);
                    }
                }
            }

            if (leftSide != rightSide) {
                return false;
            }
        }

        return true;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public String getMerge(String state)
    {
    	return mergeState.get(state);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public double getFrequency(String state) {
    	 double rightSide = finalFrequencies.getOrDefault(state, (double)0);
         for (String symbol : alphabet) {
             for (String nextState : states) {
                 if (transitionFrequencies.containsKey(state) && transitionFrequencies.get(state).containsKey(symbol) && transitionFrequencies.get(state).get(symbol).containsKey(nextState)) {
                     rightSide += transitionFrequencies.get(state).get(symbol).get(nextState);
                 }
             }
         }
         double leftSide = initialFrequencies.getOrDefault(state, (double)0);
         for (String nextState : states) {
             for (String symbol : alphabet) {
                 if (transitionFrequencies.containsKey(nextState) && transitionFrequencies.get(nextState).containsKey(symbol) && transitionFrequencies.get(nextState).get(symbol).containsKey(state)) {
                     leftSide += transitionFrequencies.get(nextState).get(symbol).get(state);
                 }
             }
         }
         return rightSide;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public double getTransitionFrequency(String state, String symbol) {
        if (getTransitionFrequencies().containsKey(state) && getTransitionFrequencies().get(state).containsKey(symbol)) {
            Map<String, Double> transitions = getTransitionFrequencies().get(state).get(symbol);
            for (String nextState : transitions.keySet()) {
                return transitions.get(nextState);
            }
        }
        return 0;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void convertDFFAtoDFGCopyActions1(DFFA dffa,String state,String curstate,String symbol,DFFA dfg,Map<String,Integer> stateCount,List<String> VisitedState) {
    	
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void convertDFFAtoDFGCopyActions(DFFA dffa,String state,String curstate,DFFA dfg,Map<String,Integer> stateCount,List<String> VisitedState,Map<String,String> mapState) {
    	for(String symbol:dffa.alphabet)
    	{
    		String nextState = dffa.getTransitionFunction().get(state+symbol);
    		if(nextState!=null)
    		{
    			double x0 = dffa.transitionPercentage.get(state).get(symbol).get(nextState);
				if(!dfg.states.contains(symbol)&& x0>0.0)
    			{
					dfg.states.add(symbol);
    				dfg.alphabet.add(symbol);
    				stateCount.put(symbol, 1);
    			}
				
				double in = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);
				Map<String,String> outs = new HashMap<String, String>();
				Map<String,Double> freq = new HashMap<String, Double>();
				calculateIncomingArc(dffa, state, freq, outs);
				double per = dffa.transitionPercentage.get(state).get(symbol).get(nextState);
				
				String aliaSymbol="";
				if(curstate.compareTo("")==0)
				{
					 aliaSymbol=symbol;
		               
						if(stateCount.get(symbol)>1)
						{
							aliaSymbol=symbol+(stateCount.get(symbol)+1);
						}
						else
							stateCount.put(symbol, stateCount.get(symbol)+1);
						if(nextState.compareTo(state)==0)
						{
							dfg.setTransitionPercentage(aliaSymbol, symbol, aliaSymbol, per);
							dfg.setTransitionFrequency(aliaSymbol, symbol, aliaSymbol, in);
							dfg.setTransitionFunction(aliaSymbol, symbol, aliaSymbol);
							mapState.put(nextState+"_"+symbol, aliaSymbol);
							if(!dfg.states.contains(aliaSymbol))
								dfg.states.add(aliaSymbol);
						}
						
						dfg.setTransitionPercentage(curstate, symbol, aliaSymbol, per);
						dfg.setTransitionFrequency(curstate, symbol, aliaSymbol, in);
						dfg.setTransitionFunction(curstate, symbol, aliaSymbol);
						mapState.put(curstate+"_"+symbol, aliaSymbol);
						if(!dfg.states.contains(curstate))
							dfg.states.add(curstate);
						if(!VisitedState.contains(nextState+","+symbol))
						{
							VisitedState.add(nextState+","+symbol);	
							convertDFFAtoDFGCopyActions(dffa,nextState,aliaSymbol,dfg,stateCount,VisitedState,mapState); 		
						}
						
				}	
				else
				{
					for(String s:outs.keySet())
					{
						//if(mapState.containsKey(nextState+"_"+symbol))
						//	System.out.println(curstate+" "+mapState.get(nextState+"_"+symbol)+"<----------------");
						if(!VisitedState.contains(nextState+","+symbol))
						{
							VisitedState.add(nextState+","+symbol);	
							convertDFFAtoDFGCopyActions(dffa,nextState,aliaSymbol,dfg,stateCount,VisitedState,mapState); 		
						}
					}
				}
				
			}
    	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void convertDFFAtoDFG(DFFA dffa,String state,DFFA dfg,List<String> visitedstates) {
    	for(String symbol:dffa.alphabet)
    	{
    		String nextState = dffa.getTransitionFunction().get(state+symbol);
    		double x0 =0;
    		try {
    			x0 = dffa.transitionPercentage.get(state).get(symbol).get(nextState)!=null?dffa.transitionPercentage.get(state).get(symbol).get(nextState):0;
    			}catch(Exception e)
    			{


    			}
    		if(nextState!=null && x0==0)
    			dffa.getTransitionFunction().remove(state+symbol);
    		if(nextState!=null&&x0>=0)
    		{
    			
    			
    			if(!dfg.states.contains(symbol)&& x0>0.0)
    			{
					dfg.states.add(symbol);
    				dfg.alphabet.add(symbol);
    			}
				Map<String,Double> symcof = new HashMap<>();
				Map<String,String> symout = new HashMap<>();
				calculateIncomingArc(dffa, state, symcof,symout);
			
				for(String s:symout.keySet())
				{
					if(x0>0.0)
					{
						double in = 0;
						try {
						in = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);
						}catch(Exception e)
						{
							
						}
						if(in>0)
						{
							if(dfg.getTransitionFunction().get(s+symbol)!=null )
							{
								double trans= dfg.getTransitionFrequencies().get(s).get(symbol).get(symbol);
								double x1=dfg.transitionPercentage.get(s).get(symbol).get(symbol);			
								x1+=(in/trans)*x0;
								dfg.setTransitionPercentage(s, symbol, symbol, x1);
								dfg.setTransitionFrequency(s, symbol, symbol, in+trans);
							}
							else
							{
								dfg.setTransitionFrequency(s, symbol, symbol, in);
								dfg.setTransitionPercentage(s,symbol,symbol, x0); 
							}
							dfg.setTransitionFunction(s, symbol, symbol);
						}
					}
				}
				if(state.compareTo("I")==0)
				{
					if(x0>0)
					{
						double in = 0;
						try {
							in = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);
						}catch(Exception e)
						{
						}
						if(dfg.transitionFunction.get(state+symbol)!=null)
						{
							double trans= dfg.getTransitionFrequencies().get(state).get(symbol).get(symbol);
							double x1=dfg.transitionPercentage.get(state).get(symbol).get(symbol);			
							x1+=(in/trans)*x0;
				
							dfg.setTransitionPercentage(state, symbol, symbol, x1);
							dfg.setTransitionFrequency(state, symbol, symbol, trans+in);
						}
						else
						{
							dfg.setTransitionPercentage(state, symbol, symbol, x0);    	
							dfg.setTransitionFrequency(state, symbol, symbol, in);
						}
						dfg.setTransitionFunction(state, symbol, symbol);
					}
				}
				if(!visitedstates.contains(nextState))
				{
					visitedstates.add(nextState);
					convertDFFAtoDFG(dffa,nextState,dfg,visitedstates); 
				}
    		}
    	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
  /*  public void convertDFFAtoDFG1(DFFA dffa,String state,DFFA dfg,List<String> visitedstates) {
    	for(String symbol:dffa.alphabet)
    	{
    		String nextState = dffa.getTransitionFunction().get(state+symbol);
    		if(nextState!=null)
    		{
				long tranfreq = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);
				if(!dfg.states.contains(symbol))
    			{
					dfg.states.add(symbol);
    				dfg.alphabet.add(symbol);
    			}
				Map<String,Long> inList = new HashMap<String, Long>();
				long in=calculateIncomingArc(dffa, state, inList);		
				double x0 = dffa.transitionPercentage.get(state).get(symbol).get(nextState);
				for(String s:inList.keySet())
				{
					String ksymbol = s;
					long x2 = 0 ;
					try {
						x2 = dfg.transitionFrequencies.get(ksymbol).get(symbol).get(symbol);
					}
					catch(Exception e)
					{
						
					}
					//dfg.setTransitionFrequency(ksymbol,symbol,symbol, x1+x2);    	
					dfg.setTransitionFunction(ksymbol, symbol, symbol);
				}
				if(state.compareTo("")==0)
				{
					dfg.setTransitionFrequency(state,symbol,symbol, tranfreq);    	
					dfg.setTransitionFunction(state, symbol, symbol);
				}
				if(!visitedstates.contains(nextState))
				{
					visitedstates.add(nextState);
					convertDFFAtoDFG(dffa,nextState,dfg,visitedstates); 
				}
    		}
    	}
    }*/
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
 /*   public void convertDFFAtoDFG(DFFA dffa,String state,DFFA dfg,String dfgState,List<String> visitedstates,Map<String, Long> incomming) {
    	for(String symbol:dffa.alphabet)
    	{
    		String nextState = dffa.getTransitionFunction().get(state+symbol);
    		if(nextState!=null)
    		{
				long tranfreq = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);

    			if(!dfg.states.contains(symbol))
    			{
    				dfg.states.add(symbol);
    				dfg.alphabet.add(symbol);
    				dfg.setFinalFrequency(symbol, 0L);
    				incomming.put(symbol, tranfreq);
    			}
    			else
    			{
    				long x = incomming.get(symbol);
    				x +=tranfreq;
    				incomming.put(symbol, x);
    			}
    			if(symbol.compareTo("O")!=0)
    				dfg.setTransitionFunction(dfgState,symbol,symbol);    			
    				long x =0;
    				try {
    					x = dfg.getTransitionFrequencies().get(dfgState).get(symbol).get(symbol)!=null? dfg.getTransitionFrequencies().get(dfgState).get(symbol).get(symbol):0;
    				}catch(Exception e)
    				{
    					x = 0;
    				}
    				x +=tranfreq;
    				if(state.compareTo(nextState)==0)
    				{
    	    			dfg.setTransitionFunction(symbol,symbol,symbol);
    	    			dfg.setTransitionFrequency(symbol,symbol,symbol,x); 
    				}
    				else
    				{
    						Map<String,Long> inList = new HashMap<String, Long>();
    						long in=calculateIncomingArc(dffa, state, inList);
    						long tot =0;
    						String lastSym ="++";
    						long x0 = dffa.transitionFrequencies.get(state).get(symbol).get(nextState);
    						
    						for(String s:inList.keySet())
    						{
    							
    							long x1 = inList.get(s)*x0/in;
    							tot += x1;
    							String ksymbol = s;
        						dfg.setTransitionFrequency(ksymbol,symbol,symbol, x1);    	
        						dfg.setTransitionFunction(ksymbol, symbol, symbol);
        						lastSym = ksymbol;
    						}
    						if(in-tot >0)
    						{
    							long x3 = in-tot;
    							if(lastSym.compareTo("++")!=0)
    							{
    								long x4 = dfg.transitionFrequencies.get(lastSym).get(symbol).get(symbol);
    								dfg.setTransitionFrequency(lastSym,symbol,symbol, x4 + x3);
    							}
    						}
    				}
    			
    			if(!visitedstates.contains(nextState))
    			{
    				visitedstates.add(nextState);
    				convertDFFAtoDFG(dffa,nextState,dfg,symbol,visitedstates,incomming); 				
    			}
    		}
    	}
    }*/
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public String extractArcEquation(FPTA fpta,String state,String nextState,String symbol) {
    	String result="";
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void rebalancePercentages(DFFA fpta) {
    	for(String state:fpta.states)
    	{
    		double total =0;
    		Map<String,String> outSymbol = new HashMap<>();
    		Map<String,Double> Symbolcof = new HashMap<>();
    		for(String symbol:fpta.alphabet)
    		{
    			String nextState= fpta.transitionFunction.get(state+symbol);
    			if(nextState!=null)
    			{
    				outSymbol.put(symbol, nextState);
    				try{	Symbolcof.put(symbol, fpta.transitionPercentage.get(state).get(symbol).get(nextState)!=null?fpta.transitionPercentage.get(state).get(symbol).get(nextState):0);
    				total += fpta.transitionPercentage.get(state).get(symbol).get(nextState);
    				}catch(Exception e)
    				{
    					
    				}
    			}
    		}
    		if(total>1)
    		{
    			for(String s:outSymbol.keySet())
    			{
    				
    				fpta.setTransitionPercentage(state, s ,  outSymbol.get(s), Symbolcof.get(s)/total);
    			}
    		}
    	}
    }
    
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<String> extractEquations(DFFA dffa){
    
    	List<String> result = new ArrayList<String>();
    	for(String state : dffa.states)
    	{
    		String leftSide ="";
			String rightSide ="";
			Map<String,Double> arcEquation=  new HashMap<>();
    		for(String symbol:dffa.alphabet)
    		{
    		
    			String nextState=dffa.getTransitionFunction().get(state+symbol);
    		
    			if(nextState!=null)
    			{
    		
    				if(rightSide.compareTo("")!=0)
    					rightSide+="+";
    				String firstAlias = state.compareTo("")!=0?state:"I";
    				String secndAlias = nextState.compareTo("")!=0?nextState:"I";
    				rightSide+="f("+firstAlias+","+secndAlias+")";
    				try {
    				arcEquation.put("f("+firstAlias+","+secndAlias+")",dffa.transitionPercentage.get(state).get(symbol).get(nextState));
    				}catch(Exception e)
    				{
    					System.out.println(state+" "+symbol+" "+nextState);
    					System.exit(0);
    				}
    				//System.out.println(arcEquation.get("f("+firstAlias+","+secndAlias+")"));
    		
    			}
    			
    			for(String prevState:dffa.states)
    			{
    					if(dffa.transitionFunction.get(prevState+symbol)!=null && dffa.transitionFunction.get(prevState+symbol).compareTo(state)==0)
    					{
    						
    						if(leftSide.compareTo("")!=0)
    	    					leftSide+="+";
    	    				String firstAlias = prevState.compareTo("")!=0?prevState:"I";
    	    				String secondAlias = state.compareTo("")!=0?state:"I";
    	    				leftSide+="f("+firstAlias+","+secondAlias+")";
    					}

    			}
    			
    		}
    		
    		if(rightSide.compareTo("")!=0)
			{
    			
    			if(state.compareTo("")==0)
    				leftSide=""+dffa.getInitialFrequencies().get("");
    			
    			if(leftSide.compareTo("")!=0)
    				result.add(leftSide+"="+rightSide);
    			
			}
    		
    		for(String s: arcEquation.keySet())
			{
    			
				StringTokenizer st = new StringTokenizer(leftSide,"+");
			
				String s1="";
				while(st.hasMoreTokens())
				{
					
					if(s1.compareTo("")!=0)
						s1+="+";
					
					String temp = st.nextToken();
					
					if(temp.charAt(0)!='f')
					{
						s1+=arcEquation.get(s)*Double.parseDouble(temp);
					}
					else
						s1+=arcEquation.get(s)+temp;
				}
				if(s1.compareTo("")!=0)
				result.add("1.0"+s+"="+s1);
			}
    	}
    	String leftSide="";
    	for(String state:dffa.states)
    	{
    		if(dffa.transitionFunction.get(state+"O")!=null)
    		{
    		
    			String alias=state;
    			if(state.compareTo("")==0)
    				alias="I";
    			if(leftSide.compareTo("")!=0)
    				leftSide+="+";
    			leftSide+="f("+state+",O)";
    		}
    	}
  
    	for(String alp:dffa.alphabet)
    		if(dffa.transitionFunction.containsKey("I"+alp))
    		{
    			String var1 = dffa.transitionFunction.get("I"+alp);
    			double value=dffa.getInitialFrequencies().get("I");
    			String s="f(I,"+var1+")="+dffa.transitionPercentage.get("I").get(alp).get(var1)*value;
    			result.add(s);
    		}
    	//	s=s.substring(0,s.length()-1);
    	//	result.add(s);
    		result.add(leftSide+"="+dffa.getInitialFrequencies().get("I"));
    	return result;
    }
  	
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public List<String> extractEquations1(DFFA dffa){
    	List<String> result = new ArrayList<String>();
    	for(String state : dffa.states)
    	{
    		String leftSide ="";
			String rightSide ="";
			Map<String,Double> arcEquation=  new HashMap<>();
    		for(String symbol:dffa.alphabet)
    		{
    			
    			String nextState=dffa.getTransitionFunction().get(state+symbol);
    			if(nextState!=null)
    			{
    				if(rightSide.compareTo("")!=0)
    					rightSide+="+";
    				String firstAlias = state.compareTo("")!=0?state:"I";
    				String secndAlias = nextState.compareTo("")!=0?nextState:"I";
    				rightSide+="f("+firstAlias+","+symbol+","+secndAlias+")";
    				arcEquation.put("f("+firstAlias+","+symbol+","+secndAlias+")",dffa.transitionPercentage.get(state).get(symbol).get(nextState));
    			}
    			for(String prevState:dffa.states)
    			{
    					if(dffa.transitionFunction.get(prevState+symbol)!=null && dffa.transitionFunction.get(prevState+symbol).compareTo(state)==0)
    					{
    						if(leftSide.compareTo("")!=0)
    	    					leftSide+="+";
    						
    	    				String firstAlias = prevState.compareTo("")!=0?prevState:"I";
    	    				String secondAlias = state.compareTo("")!=0?state:"I";
    	    				leftSide+="f("+firstAlias+","+symbol+","+secondAlias+")";
    					}

    			}
    			
    		}
    		
    		if(rightSide.compareTo("")!=0)
			{
    			if(state.compareTo("")==0)
    				leftSide=""+dffa.getInitialFrequencies().get("");
    			if(leftSide.compareTo("")!=0)
    				result.add(leftSide+"="+rightSide);
			}
    		for(String s: arcEquation.keySet())
			{
				StringTokenizer st = new StringTokenizer(leftSide,"+");
				String s1="";
				while(st.hasMoreTokens())
				{
					if(s1.compareTo("")!=0)
						s1+="+";
					String temp = st.nextToken();
					if(temp.charAt(0)!='f')
					{
						s1+=arcEquation.get(s)*Double.parseDouble(temp);
					}
					else
						s1+=arcEquation.get(s)+temp;
				}
				if(s1.compareTo("")!=0)
				result.add("1.0"+s+"="+s1);
			}
    	}
    	String leftSide="";
    	for(String state:dffa.states)
    	{
    		if(dffa.transitionFunction.get(state+"O")!=null)
    		{
    			String alias=state;
    			if(state.compareTo("")==0)
    				alias="I";
    			if(leftSide.compareTo("")!=0)
    				leftSide+="+";
    			leftSide+="f("+state+",O,O)";
    		}
    	}
    	for(String alp:dffa.alphabet)
    		if(dffa.transitionFunction.containsKey("I"+alp))
    		{
    			String s="f(I,"+alp+","+dffa.transitionFunction.get("I"+alp)+")="+dffa.transitionPercentage.get("I").get(alp).get(dffa.transitionFunction.get("I"+alp))*dffa.getInitialFrequencies().get("I");
    			result.add(s);
    		}
    	//	s=s.substring(0,s.length()-1);
    	//	result.add(s);
    		result.add(leftSide+"="+dffa.getInitialFrequencies().get("I"));
    	return result;
    }
  	
    
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void clearModel(DFFA dffa) {
    	List<String> remo = new ArrayList<String>();
    	for(String state : dffa.states)
    	{ 	
			double out=calculateOutcommingArcs(dffa, state);
			
			for(String symbol:dffa.alphabet)
    		{
    			if(out==0)
    			{
    				dffa.transitionFunction.remove(dffa.transitionFunction.get(state+symbol));
    				dffa.finalFrequencies.remove(state);
    				remo.add(state);
    			}
    		}
    	}
    
    	dffa.states.removeAll(remo);
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void calculateTransitionPercentage(DFFA dffa)
    {
    	DecimalFormat df = new DecimalFormat("0.000");
    	for(String state : dffa.states)
    	{
			Map<String,Double> outList = new HashMap<String, Double>();
			Map<String,String> outsymbol = new HashMap<String, String>();
			double out=calculateOutcommingArcs(dffa, state, outList,outsymbol);
			out +=dffa.getFinalFrequency(state);
    		for(String symbol:outList.keySet())
    		{
    			if(out==0)
    			{
    				dffa.transitionFunction.remove(dffa.transitionFunction.get(state+symbol));
    				dffa.transitionFrequencies.get(state).get(symbol).remove(outsymbol.get(symbol));
    			}
    			else    			
    			dffa.setTransitionPercentage(state, symbol, outsymbol.get(symbol),Double.parseDouble(df.format((double)outList.get(symbol)/(double)out)));
    		}

    		dffa.setFinalProbability(state, dffa.getFinalFrequency(state)/out);

    		
    	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    void rebalancing1(FPTA DFG,String qPrime,Map<String, Long> incomming,List<String> visitedStates) {

    	List<String> fixedStates = new ArrayList<String>();
    	boolean flag = true;
    	int count =0;
    

    }
   
  
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void calculateTransitionPercentage1(DFFA dffa)
    {
    	DecimalFormat df = new DecimalFormat("0.000");
    	for(String state : dffa.states)
    	{
			Map<String,Double> outList = new HashMap<String, Double>();
			Map<String,String> outsymbol = new HashMap<String, String>();
			double out=calculateOutcommingArcs(dffa, state, outList,outsymbol);
			//out +=dffa.getFinalFrequency(state);
			
    		for(String symbol:outList.keySet())
    		{
    			if(out==0)
    			{
    				dffa.transitionFunction.remove(dffa.transitionFunction.get(state+symbol));
    				dffa.transitionFrequencies.get(state).get(symbol).remove(outsymbol.get(symbol));
    			}
    			else
    			
    			dffa.setTransitionPercentage(state, symbol, outsymbol.get(symbol),Double.parseDouble(df.format((double)outList.get(symbol)/(double)out)));
    		}

    		dffa.setFinalProbability(state, dffa.getFinalFrequency(state)/out);

    		
    	}
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public static FPTA getDFG(FPTA dffa) {
    	
    	for(String state:dffa.states)
    		for(String symbol:dffa.alphabet)
    			if(dffa.transitionFunction.containsKey(state+symbol))
    			{
    				String next = dffa.transitionFunction.get(state+symbol);
    				try {
    					double x = dffa.transitionFrequencies.get(state).get(symbol).get(next);
    					if(x<0.1)
    					{
    						dffa.transitionFunction.remove(state+symbol);
    						dffa.transitionFrequencies.get(state).get(symbol).remove(next);
    					}
    				}catch(Exception e)
    				{
    					dffa.transitionFunction.remove(state+symbol);
    				}
    				
    			}
    	
    	dffa.calculateTransitionPercentage1(dffa);
    	FPTA DFG = new FPTA();
        DFG.alphabet = new HashSet<>();
        DFG.states = new HashSet<>();
        List<String> visitedStates = new ArrayList<String>();

        DFG.states.add("I");
        
        
        DFG.setInitialFrequency("I", dffa.getInitialFrequencies().get("I"));
        
        DFG.convertDFFAtoDFG(dffa,"I", DFG, visitedStates);
      
        DFG.rebalancePercentages(DFG);
        List<String> x =DFG.extractEquations(DFG);
        try {
        Map<String,Double> y = CoefficientMatrix.findSDAGCoefficient(x);
        DFG.updateTransitionFrequency(DFG,y);}
        catch(Exception e)
        {
        	
        }
        return DFG;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public double calculatetotalFrequency(DFFA dffa1,String state)
    {
    	double result =dffa1.getInitialFrequencies().get(state)!=null?dffa1.getInitialFrequencies().get(state):0;
    	for(String s:dffa1.states)
    		for(String sym:dffa1.alphabet)
    			if(dffa1.getTransitionFunction().containsKey(s+sym) && dffa1.getTransitionFunction().get(s+sym).compareTo(state)==0)
    			{
    				result+=dffa1.getTransitionFrequencies().get(s).get(sym).get(state);
    			}
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public double calculateIncomingFrequencies(DFFA dffa1,String state)
    {
    	double result =0;
    	for(String s:dffa1.states)
    		for(String sym:dffa1.alphabet)
    			if(dffa1.getTransitionFunction().containsKey(s+sym) && dffa1.getTransitionFunction().get(s+sym).compareTo(state)==0)
    			{
    					result += dffa1.getTransitionFrequencies().get(s).get(sym).get(state);
    			}
    	result+=dffa1.getInitialFrequencies().get(state)!=null?dffa1.getInitialFrequencies().get(state):0;
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public double calculateOutgoingFrequencies(DFFA dffa1,String state)
    {
    	double result =0;
    	for(String sym:dffa1.alphabet)
    		if(dffa1.getTransitionFunction().containsKey(state+sym) )
    			{
    				String next=dffa1.getTransitionFunction().get(state+sym);
    				result += dffa1.getTransitionFrequencies().get(state).get(sym).get(next);
    			}
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public double calculateIncomingArc(DFFA dffa1,String state)
    {
    	double result =0;
    	for(String s:dffa1.states)
    		for(String sym:dffa1.alphabet)
    			if(dffa1.transitionFunction.containsKey(s+sym))
    			{
    				
    				String next = dffa1.transitionFunction.get(s+sym);
    			//	if(state.compareTo("AEFCBDGHJJMCBDDDBCCDBCBD")==0 && state.compareTo(next)==0)
    				//	System.out.println(s+" "+sym+"-->"+state);
    				if(next.compareTo(state)==0)
    					result++;
    			}
    	return result;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public boolean IsLeafOrNot(DFFA dffa1,String state)
    {
    	for(String sym:dffa1.alphabet)
    	{
    		if(dffa1.getTransitionFunction().containsKey(state+sym))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static FPTA getDFG1(FPTA dffa) {
    	
    	
    	//FPTA dffa2 = FPTA.firstLevelConversion(dffa);
    	dffa.calculateTransitionPercentage(dffa);
    	FPTA fpta1 = new FPTA();
        fpta1.alphabet = new HashSet<>();
        fpta1.states = new HashSet<>();
        fpta1.states.add("I");
        fpta1.setInitialFrequency("I", 0);
    	FPTA DFG = new FPTA();
    	DFG.alphabet = new HashSet<>();
    	DFG.states = new HashSet<>();
    	DFG.states.add("I");
    	DFG.setInitialFrequency("I", 0);
    	Map<String,Integer> stateCount=new HashMap<String, Integer>();
    	Map<String,String> mapState=new HashMap<String, String>();
    	List<String> visitedState = new ArrayList<String>();
    	visitedState.add("I");
    	//dffa.convertDFFAtoDFG(dffa,"I","I",DFG,stateCount,visitedState,mapState);
    	DFG.calculateTransitionPercentage(DFG);
    	DFG.rebalancePercentages(DFG);

        return DFG;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static FPTA firstLevelPercentageConversion(DFFA dffa1)
    {
    	FPTA dffa2 = new FPTA();
    	dffa1.copy(dffa2);
    	dffa2.states.add("O");
    	dffa2.alphabet.add("O");
    	dffa2.setFinalFrequency("O", 0L);
    	dffa2.setInitialFrequency("I", dffa1.getInitialFrequencies().get("")!=null?dffa1.getInitialFrequencies().get(""):dffa1.getInitialFrequencies().get("I"));
    	for(String state:dffa1.states)
    	{
    		double finalFreq = dffa1.getFinalProbability(state);
    		if(finalFreq>0.01)
    		{
    			dffa2.setFinalFrequency(state,0L);
    			dffa2.setTransitionFunction(state, "O", "O");			
    			dffa2.setTransitionPercentage(state, "O", "O", finalFreq);
    		}	
    	}
    	dffa2.setFinalProbability("O",  dffa1.getInitialFrequencies().get("")!=null?dffa1.getInitialFrequencies().get(""):dffa1.getInitialFrequencies().get("I"));
    	return dffa2;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public static FPTA firstLevelConversion(DFFA dffa1)
    {
    	FPTA dffa2 = new FPTA();
    	dffa1.copy(dffa2);
    	dffa2.states.add("O");
    	dffa2.alphabet.add("O");
    	dffa2.setFinalFrequency("O", 0L);
    	for(String state:dffa1.states)
    	{
    		double finalFreq = dffa2.getFinalFrequency(state);
    		if(finalFreq>0)
    		{
    			dffa2.setFinalFrequency(state,0L);
    			dffa2.setTransitionFunction(state, "O", "O");			
    			dffa2.setTransitionFrequency(state, "O", "O", finalFreq);
    			dffa2.setFinalFrequency("O",dffa2.getFrequency("O")+finalFreq);
    		}
    	}
    	return dffa2;
    	
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public void updateTransitionFrequency(DFFA fpta,Map<String,Double> funcList) {

    	for(String s:funcList.keySet())
    	{
    		StringTokenizer st= new StringTokenizer(s,",");
    		String state=st.nextToken();
    	//	System.out.println(s);
    		String nextState=st.nextToken();
    		double frequency=funcList.get(s);
    		//if(state.compareTo("I")==0)
    		//	state="I";
    	//	System.out.println("before change"+state+" "+nextState+" "+fpta.transitionFunction.get(state+nextState.charAt(0)));
    
    	/*	if(fpta.states.contains("")&&state.compareTo("I")==0)
    		{ 
    			if(nextState.compareTo("O")==0)
    			{
    				fpta.setFinalFrequency("", frequency);
    			}
    			else	
    			fpta.setTransitionFrequency("", nextState.charAt(0)+"", nextState,Double.parseDouble(String.format("%.3f",frequency)));

    		}
    		else*/
    			fpta.setTransitionFrequency(state, nextState.charAt(0)+"", nextState,Double.parseDouble(String.format("%.3f",frequency)));
    	}
    
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public FPTA updateTransitionFrequency1(DFFA fpta,Map<String,Double> funcList) {
    	
    	FPTA res = new FPTA();
    	/*try {
    	if(fpta.initialFrequencies.get("")!=0)
    		res.setInitialFrequency("I", fpta.getInitialFrequencies().get(""));
    	}
    	catch(Exception e)
    	{
    		
    	}
    	try {
    	if(fpta.initialFrequencies.get("I")!=0)
    		res.setInitialFrequency("I", fpta.getInitialFrequencies().get("I"));
    	}
    	catch(Exception e)
    	{
		
    	}*/
    	if(fpta.initialFrequencies.get("")!=0)
    		res.setInitialFrequency("", fpta.getInitialFrequencies().get(""));
    	
    	for(String s:funcList.keySet())
    	{
    		
    		
    		
    		if(s.charAt(0)==',')
			{
				s="I"+s;
			}
    		if(s.charAt(s.length()-1)==',')
    			s=s+"I";
    			StringTokenizer st= new StringTokenizer(s,",");
    			String state=st.nextToken();
    			
    			String symbol = st.nextToken();
    			String nextState=st.nextToken();
    			
    			double frequency=funcList.get(s);
    			if(state.compareTo("I")==0)
    				state="";
    			if(nextState.compareTo("I")==0)
    				nextState="";
    			if(!res.states.contains(state))
    			{
    				
    				res.states.add(state);
    				res.setFinalFrequency(state, 0.0);
    			
    			}
    			if(!res.states.contains(nextState)&&nextState.compareTo("O")!=0)
    			{
    				res.states.add(nextState);
    				res.setFinalFrequency(nextState, 0.0);
    			}
    			if(!res.alphabet.contains(symbol)&&symbol.compareTo("O")!=0)
    				res.alphabet.add(symbol);
    			//if(state.compareTo("I")==0)
    		//	state="I";
    	//	System.out.println("before change"+state+" "+nextState+" "+fpta.transitionFunction.get(state+nextState.charAt(0)));
    			if(nextState.compareTo("O")!=0)
    			{
    				res.setTransitionFunction(state, symbol, nextState);
    				res.setTransitionFrequency(state, symbol, nextState,Double.parseDouble(String.format("%.3f",frequency)));
    			}
    			else
    			{
    				res.setFinalFrequency(state,Double.parseDouble(String.format("%.3f",frequency)));

    			}
    			//System.out.println(state+" -->"+symbol+"--> "+nextState+fpta.transitionFrequencies.get(state).get(symbol).get(nextState));
    	}
    	return res;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static void main(String[] args) {
    	HashMap<String, Double> log = new HashMap<String,Double>() ;
    	log.put("a", 8.0);
    	log.put("ab", 2.0);
    	log.put("bb", 5.0);
		log.put("b", 3.0);
		log.put("ba",2.0);

	/*	ALERGIA alergia = new ALERGIA(0.7,11,log,log,2);
		FPTA fpta = alergia.run();
		fpta.show(fpta, "ss");*/
		FPTA fpta1 = new FPTA();
    	fpta1.alphabet = new HashSet<String>();
    	fpta1.alphabet.add("a");
    	fpta1.alphabet.add("b");
    	fpta1.states = new HashSet<String>();
    	fpta1.states.add("");
    	fpta1.states.add("p1");
    	fpta1.states.add("p2");
    	fpta1.states.add("p3");
    	fpta1.finalFrequencies = new HashMap<String, Double>();
    	fpta1.initialFrequencies = new HashMap<String, Double>();
    	fpta1.setInitialFrequency("", 20);
    	fpta1.setFinalFrequency("", 0);
    	fpta1.setFinalFrequency("p1", 8);
    	fpta1.setFinalFrequency("p2", 7);
    	fpta1.setFinalFrequency("p3", 5);
    	fpta1.setTransitionFunction("", "a", "p1");
    	fpta1.setTransitionFrequency("", "a", "p1", 10);
    	fpta1.setTransitionFunction("", "b", "p2");
     	fpta1.setTransitionFrequency("", "b", "p2", 10);
    	fpta1.setTransitionFunction("p1", "b", "p2");
     	fpta1.setTransitionFrequency("p1", "b", "p2", 2);
    	fpta1.setTransitionFunction("p2", "a", "p1");
     	fpta1.setTransitionFrequency("p2", "a", "p1", 2);
    	fpta1.setTransitionFunction("p2", "b", "p3");
     	fpta1.setTransitionFrequency("p2", "b", "p3", 5);
     	fpta1.setTransitionFunction("p2", "a", "p2");
     	fpta1.setTransitionFrequency("p2", "a", "p2", 2);
     	fpta1.show(fpta1, "fpta1");
     	FPTA sdag = SDAG.DFFAtoSDAG(fpta1);
     	FPTA dfg = DFFA.getDFG(sdag);
     	dfg.showDFG(dfg, "DFG");
    	PerformanceEstimator PE1 = new PerformanceEstimator(BackGroundType.U);
	 	HashMap<String,Double> pe1 =PE1.calculatePerformanceDFGMetrics(sdag, log,2);
	 	System.out.println(pe1);
    	PerformanceEstimator PE2 = new PerformanceEstimator(BackGroundType.U);
	 	HashMap<String,Double> pe2 =PE2.calculatePerformanceDFGMetrics(dfg, log,2);
	 	System.out.println(pe2);

    	PerformanceEstimator PE3 = new PerformanceEstimator(BackGroundType.U);
	 	HashMap<String,Double> pe3 =PE3.calculatePerformanceMetrics(fpta1, log,2);
	 	System.out.println(pe3);
	 	/* 	HashMap<String, Double> log2 = new HashMap<String,Double>() ;
    	log2.put("a", 4.0);
		log2.put("aa", 4.0);
		log2.put("aab",2.0);
		log2.put("b",10.0);
		
  /*   	
     	FPTA fpta2 = new FPTA();
    	fpta2.alphabet = new HashSet<String>();
    	fpta2.alphabet.add("a");
    	fpta2.alphabet.add("b");
    	fpta2.states = new HashSet<String>();
    	fpta2.states.add("");
    	fpta2.states.add("q1");
    	fpta2.states.add("q2");
    	fpta2.states.add("q3");
    	fpta2.finalFrequencies = new HashMap<String, Double>();
    	fpta2.initialFrequencies = new HashMap<String, Double>();
    	fpta2.setInitialFrequency("", 20);
    	fpta2.setFinalFrequency("", 0);
    	fpta2.setFinalFrequency("q1", 6);
    	fpta2.setFinalFrequency("q2", 10);
    	fpta2.setFinalFrequency("q3", 4);
    	fpta2.setTransitionFunction("", "a", "q1");
    	fpta2.setTransitionFrequency("", "a", "q1", 10);
    	fpta2.setTransitionFunction("", "b", "q2");
     	fpta2.setTransitionFrequency("", "b", "q2", 10);
    	fpta2.setTransitionFunction("q1", "a", "q3");
     	fpta2.setTransitionFrequency("q1", "a", "q3", 6);
    	fpta2.setTransitionFunction("q3", "b", "q1");
     	fpta2.setTransitionFrequency("q3", "b", "q1", 2);
   	FPTA mergedDffa = fpta1;
	ALERGIA alergia = new ALERGIA(mergedDffa);	
	alergia.setAlpha(0.1);
	alergia.setFilterring(30);	
	alergia.mergeThirdModel(mergedDffa,fpta2);
	fpta2.show(fpta2, "model2");
	mergedDffa.show(mergedDffa, "merged");
	List<String> nodes = ALERGIA.listNonCycle(mergedDffa);
	HashMap<String, Double> x = new HashMap<String, Double>();
	double defaultER =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(mergedDffa, log, 2);
	double defaultER1 =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(mergedDffa, log2, 2);
System.out.println(defaultER+" "+defaultER1);
	HashMap<String,Double> o =extractNodeEffects(mergedDffa,log,2);
	   
	   
	   // System.out.println("merged "+i);    	    		
     	
     	 HashMap<Integer, Double> clientEr = new HashMap<Integer, Double>();
	     HashMap<Integer, HashMap<String, Double>> clientErEffects = new HashMap<Integer, HashMap<String,Double>>();
	     clientErEffects.put(0 ,extractNodeEffects(mergedDffa,log,2));
	     clientEr.put(0, defaultER);
		 
	     clientErEffects.put(1 ,extractNodeEffects(mergedDffa,log2,2));
	// 	FPTA sdag = SDAG.DFFAtoSDAG(mergedDffa);
	//     PerformanceEstimator PE1 = new PerformanceEstimator(BackGroundType.U);
//	 	HashMap<String,Double> pe1 =PE1.calculatePerformanceDFGMetrics(sdag, log,2);
	 //    PerformanceEstimator PE2 = new PerformanceEstimator(BackGroundType.U);
	 //	HashMap<String,Double> pe2 =PE2.calculatePerformanceDFGMetrics(sdag, log2,2);
	//   System.out.println("Client 1-->"+pe1.get("Size"));
	 ///    System.out.println("Client 1-->"+pe1.get("Entropic Relevance"));
	 //    System.out.println("Client 2-->"+pe2.get("Size"));
	 //    System.out.println("Client 2-->"+pe2.get("Entropic Relevance"));
	    
	 //    sdag.showDFG(sdag, "sdag");
//	     clientEr.put(1, defaultER1);
//	     System.out.println(clientEr);
//			Set<String> prunedList = SubgraphSolver.solvePruneOptimization(mergedDffa,clientErEffects,clientEr,0.1);
	//		for(String pnode:prunedList)
	//			mergedDffa.deleteState(mergedDffa, pnode);
			
//		 sdag = SDAG.DFFAtoSDAG(mergedDffa);
		//pe1 =PE1.calculatePerformanceDFGMetrics(sdag, log,2);
		//pe2 =PE2.calculatePerformanceDFGMetrics(sdag, log2,2);
	   // sdag.showDFG(sdag, "sdagafter");
		 //    System.out.println("Client 1-->"+pe1.get("Size"));
		  //   System.out.println("Client 1-->"+pe1.get("Entropic Relevance"));
		  //   System.out.println("Client 2-->"+pe2.get("Size"));
		 //    System.out.println("Client 2-->"+pe2.get("Entropic Relevance"));
	     // fpta.show(fpta, "fpta");
		// fpta1.show(fpta1, "ALERG");
		// FPTA sdag = SDAG.DFFAtoSDAG(fpta1);
	//	 sdag.showDFG(sdag, "sdag");
	//	 FPTA dfg = DFFA.getDFG(sdag);
	//	 dfg.showDFG(dfg, "dfg");
	/*	HashMap<String, Double> log1 = new HashMap<String,Double>() ;
    	log1.put("a", 40.0);
		log1.put("aa", 50.0);
		log1.put("aab",10.0);
		log1.put("b",100.0);*
		
    	
    /*	log.put("aaba", 10.0);
    	log.put("aabab", 10.0);
    	log.put("aabaa", 10.0);
    	log.put("aabb", 10.0);
    	log.put("aabbc", 10.0);
    	log.put("aabbcc", 10.0);
    	log.put("aabbcb", 1.0);
    	log.put("aa", 10.0);
    	log.put("a", 10.0);*/
    /*	FPTA fpta1 = new FPTA();
    	fpta1.alphabet = new HashSet<String>();
    	fpta1.alphabet.add("a");
    	fpta1.alphabet.add("b");
    	fpta1.states = new HashSet<String>();
    	fpta1.states.add("");
    	fpta1.states.add("p1");
    	fpta1.states.add("p2");
    	fpta1.states.add("p3");
    	fpta1.states.add("q3");
    	fpta1.states.add("q'3");
    	fpta1.states.add("q'1");
    	fpta1.states.add("q''1");
    	fpta1.finalFrequencies = new HashMap<String, Double>();
    	fpta1.initialFrequencies = new HashMap<String, Double>();
    	fpta1.setInitialFrequency("", 40);
    	fpta1.setFinalFrequency("", 0);
    	fpta1.setFinalFrequency("p1", 15);
    	fpta1.setFinalFrequency("p2", 15);
    	fpta1.setFinalFrequency("p3", 5);
    	fpta1.setFinalFrequency("q3", 3.66);
    	fpta1.setFinalFrequency("q'1", 0.67);
    	fpta1.setFinalFrequency("q'3", 0.44);
    	fpta1.setFinalFrequency("q''1", 0.23);
    	fpta1.transitionFunction = new HashMap<String, String>();
    	fpta1.transitionFrequencies = new HashMap<String, Map<String,Map<String,Double>>>();   	
    	fpta1.setTransitionFunction("", "a", "p1");
    	fpta1.setTransitionFrequency("", "a", "p1", 20);
    	fpta1.setTransitionFunction("", "b", "p2");
     	fpta1.setTransitionFrequency("", "b", "p2", 20);
    	fpta1.setTransitionFunction("p2", "b", "p3");
     	fpta1.setTransitionFrequency("p2", "b", "p3", 5);
    	fpta1.setTransitionFunction("p2", "a", "p1");
    	fpta1.setTransitionFrequency("p2", "a", "p1", 2);
    	fpta1.setTransitionFunction("p1", "b", "p2");
    	fpta1.setTransitionFrequency("p1", "b", "p2", 2);
    	fpta1.setTransitionFunction("p1", "a", "q3");
    	fpta1.setTransitionFrequency("p1", "a", "q3", 5);
    	fpta1.setTransitionFunction("q3", "b", "q'1");
    	fpta1.setTransitionFrequency("q3", "b", "q'1",1.34);
    	fpta1.setTransitionFunction("q'1", "a", "q'3");
    	fpta1.setTransitionFrequency("q'1", "a", "q'3", 0.67);
    	fpta1.setTransitionFunction("q'3", "b", "q''1");
    	fpta1.setTransitionFrequency("q'3", "b", "q''1", 0.23);
    	List<String> nodes = ALERGIA.listNonCycle(fpta1);
    	EntropicRelevanceCalculator er = new EntropicRelevanceCalculator(BackGroundType.Z);
    	double defaultER1 =new EntropicRelevanceCalculator(BackGroundType.Z).calculateEntropic(fpta1, log, 2);
    	double defaultER2 =new EntropicRelevanceCalculator(BackGroundType.Z).calculateEntropic(fpta1, log1, 2);
    	fpta1.show(fpta1, defaultER1+"----"+defaultER2);
    	HashMap<String, Double> x = new HashMap<String, Double>();
    	
      	for(String node : nodes)
      	{
      		//fpta.states.remove(node);
      		FPTA copy = fpta1.cloneFPTA();   		
      		copy.deleteState(copy, node);
      		System.out.println("----------remove "+node);
      		double dx1 = new EntropicRelevanceCalculator(BackGroundType.Z).calculateEntropic(copy, log, 2);
      		System.out.println(dx1+" "+defaultER1+" "+node);
      		x.put(node, dx1-defaultER1);	
      	
      		//fpta.states.add(node);
      	}
      	System.out.println("*********************************");
      	HashMap<Integer, HashMap<String, Double>> clientErEffects = new HashMap<Integer, HashMap<String,Double>>();
      	clientErEffects.put(1, x);
    	HashMap<Integer, Double> clientEr = new HashMap<Integer, Double>();
    	clientEr.put(1, defaultER1);
    	x = new HashMap<String, Double>();
    	
      	for(String node : nodes)
      	{
      		//fpta.states.remove(node);
      		FPTA copy = fpta1.cloneFPTA();   		
      		copy.deleteState(copy, node);
      		System.out.println("----------remove "+node);
      		double dx1 = new EntropicRelevanceCalculator(BackGroundType.Z).calculateEntropic(copy, log1, 2);
      		System.out.println(dx1+" "+defaultER2+" "+node);
      		x.put(node, dx1-defaultER2);	
      		
      		//fpta.states.add(node);
      	}
      	clientErEffects.put(2, x);
    	clientEr.put(2, defaultER2);
    	FPTA dDFG = SDAG.DFFAtoDDFG(fpta1);
    	dDFG.show(dDFG, "SDAG");
    	Set<String> prunedList = SubgraphSolver.solvePruneOptimization(fpta1,clientErEffects,clientEr,0.2);
    	for(String pnode:prunedList)
    		fpta1.deleteState(fpta1, pnode);
    	//defaultER1 =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(fpta1, log, 2);
    	//defaultER2 =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(fpta1, log1, 2);

    	fpta1.show(fpta1, defaultER1+"--after pruning--"+defaultER2);
   
    	/*FPTA fpta = new ALERGIA(0.8, 11,log).run(); 
    	List<String> nodes = ALERGIA.listNonCycle(fpta);
    	EntropicRelevanceCalculator er = new EntropicRelevanceCalculator(BackGroundType.U);
      	double defaultER =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(fpta, log, 3);
      	HashMap<Integer, HashMap<String, Double>> clientErEffects = new HashMap<Integer, HashMap<String,Double>>();
    	HashMap<String, Double> x = new HashMap<String, Double>();
    	
      	for(String node : nodes)
      	{
      		//fpta.states.remove(node);
      		FPTA copy = fpta.cloneFPTA();   		
      		copy.deleteState(copy, node);
      		double dx1 = new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(copy, log, 3);
       
      		x.put(node, dx1-defaultER);	
      		System.out.println(node+" "+dx1+" "+defaultER);
      		//fpta.states.add(node);
      	}
      	clientErEffects.put(1, x);
    	HashMap<Integer, Double> clientEr = new HashMap<Integer, Double>();
    	clientEr.put(1, defaultER);
    	System.out.println(x);
    	double tau = 2;
    	System.out.println(defaultER* tau);
    	fpta.show(fpta, "before pruning");
    	Set<String> prunedList = SubgraphSolver.solvePruneOptimization(fpta,clientErEffects,clientEr,tau);
    	for(String pnode:prunedList)
    		fpta.deleteState(fpta, pnode);
    	fpta.show(fpta, "after pruning");
    	double defaultER1 =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(fpta, log, 3);
    	System.out.println(defaultER+" "+defaultER1);
    //	FPTA sdag = SDAG.DFFAtoDDFG(fpta); 
    //	sdag.show(sdag, "SDAG");
    	/*FPTA fpta = new FPTA();
    	fpta.alphabet = new HashSet<>();
    	fpta.alphabet.add("a");
    	fpta.alphabet.add("b");
    	fpta.alphabet.add("c");
    	fpta.alphabet.add("e");
    	fpta.states = new HashSet<>();
    	fpta.states.add("");
    	fpta.states.add("n1");
    	fpta.states.add("n2");
    	fpta.states.add("n3");
    	fpta.states.add("n4");
    	fpta.states.add("n5");
    	fpta.setFinalFrequency("", 0.0);
    	fpta.setFinalFrequency("n1", 0.0);
    	fpta.setFinalFrequency("n2", 0.0);
    	fpta.setFinalFrequency("n3", 0.0);
    	fpta.setFinalFrequency("n4", 1187.4);
    	fpta.setFinalFrequency("n5", 305.6);
    	fpta.setInitialFrequency("", 1493.0);
    	fpta.setTransitionFunction("", "a","n1");
    	fpta.setTransitionFrequency("", "a", "n1", 1493.0);
    	fpta.setTransitionFunction("n1", "b","n2");
    	fpta.setTransitionFrequency("n1", "b", "n2", 253.7);
    	fpta.setTransitionFunction("n1", "c","n3");
    	fpta.setTransitionFrequency("n1", "c", "n3", 1239.3);
    	fpta.setTransitionFunction("n2", "b","n2");
    	fpta.setTransitionFrequency("n2", "b", "n2", 51.9);
    	fpta.setTransitionFunction("n2", "c","n3");
    	fpta.setTransitionFrequency("n2", "c", "n3", 253.7);
    	fpta.setTransitionFunction("n3", "e","n5");
    	fpta.setTransitionFrequency("n3", "e", "n5", 1493.0);
    	fpta.setTransitionFunction("n5", "c","n4");
    	fpta.setTransitionFrequency("n5", "c", "n4", 1187.4); 	
    	fpta.show(fpta, "first model");
   /* 	FPTA dffa2=FPTA.firstLevelConversion(fpta);

    	 dffa2.calculateTransitionPercentage(dffa2);

    	//List<String> x = dffa2.extractEquations(dffa2);
    	FPTA DFG = new FPTA();
        DFG.alphabet = new HashSet<>();
        DFG.states = new HashSet<>();
        List<String> visitedStates = new ArrayList<String>();

        
        Map<String,Integer> stateCount = new HashMap<String, Integer>();
        FPTA fpta1 = new FPTA();
        fpta1.alphabet = new HashSet<>();
        fpta1.states = new HashSet<>();
        fpta1.states.add("");
        fpta1.setInitialFrequency("", 0L);
        FPTA res = ALERGIA.alphaStochasticFold(fpta1, dffa2, 6);
        FPTA.removeZeroLink(res, "");
        res.calculateTransitionPercentage(res);
        DFG.states.add("");

        DFG.setInitialFrequency("", dffa2.getInitialFrequencies().get(""));
      //  DFG.convertDFFAtoDFGCopyActions(dffa2,"","",DFG,stateCount);

        DFG.rebalancePercentages(DFG);
      
    /*    List<String> formulates = new ArrayList<String>();
        formulates.add("1493.0=f(I,n1)");
        formulates.add("f(I,n1)=f(n1,n2)+f(n1,n3)");
    	formulates.add("f(n1,n3)+f(n2,n3)+f(n5,n3)=f(n3,n5)+f(n3,O)");
    	formulates.add("f(n3,n5)=f(n5,n3)+f(n5,O)");
    	formulates.add("f(n3,O)+f(n5,O)=1493.0");
    	formulates.add("f(n1,n2)=f(n2,n3)");
    	formulates.add("1.0f(n1,n2)=0.17f(I,n1)");
    	formulates.add("1.0f(n1,n3)=0.83f(I,n1)");
    	formulates.add("1.0f(n2,n2)=0.17f(n1,n2)+0.17f(n2,n2)");
    	formulates.add("1.0f(n2,n3)=0.83f(n1,n2)+0.83f(n2,n2)");
     	formulates.add("1.0f(n3,n5)=0.5f(n1,n3)+0.5f(n2,n3)+0.5f(n5,n3)");
    	formulates.add("1.0f(n3,O)=0.5f(n1,n3)+0.5f(n2,n3)+0.5f(n5,n3)");
    	formulates.add("1.0f(n5,n3)=0.8f(n3,n5)");
    	formulates.add("1.0f(n5,O)=0.2f(n3,n5)");
    	*/
        
  /*  	List<String> formulates=DFG.extractEquations(DFG);
    /*    System.out.println("List of Equations");*/
  /*      for(String s:formulates)
        	System.out.println(s);
        Map<String,Double> y = CoefficientMatrix.findCoefficient(formulates);
        Map<String,Double> yy = new HashMap<String, Double>();

      
     /*   yy.put("I,a", y.get("I,n1"));
        yy.put("a,b", y.get("n1,n2"));
        yy.put("b,c", y.get("n2,n3"));
        yy.put("e,O", y.get("n5,O"));
        yy.put("b,b", y.get("n2,n2"));
        yy.put("c,O", y.get("n3,O"));
        yy.put("a,c", y.get("n1,n3"));
        yy.put("c,e", y.get("n3,n5"));
        yy.put("e,c", y.get("n5,n3"));*/
   //     DFG.updateTransitionFrequency(DFG,yy);
    //    DFG.showPercentage(DFG, "DFG");
    /*    DecimalFormat df = new DecimalFormat("0.000");
        System.out.println("Solved");
        for(String s:y.keySet())
        	System.out.println("f("+s+") ---> "+df.format(y.get(s)));
        DFG.showPercentage(DFG, "DFG");*/
    	// dffa2.showfirst(dffa2, "dffa2");
  /*    DFFA dffa= new DFFA();
      dffa.alphabet = new HashSet<>();
      dffa.alphabet.add("a");
      dffa.alphabet.add("b");
      dffa.alphabet.add("c");
      dffa.alphabet.add("e");
      dffa.states = new HashSet<>();
      dffa.states.add("");
      dffa.states.add("n1");
      dffa.states.add("n2");
      dffa.states.add("n3");
      dffa.states.add("n4");
      dffa.states.add("n5");
      dffa.states.add("n6");
      dffa.states.add("n7");
      dffa.setFinalFrequency("", 0L);
      dffa.setFinalFrequency("n1", 0L);
      dffa.setFinalFrequency("n2", 100L);
      dffa.setFinalFrequency("n3", 300L);
      dffa.setFinalFrequency("n4", 100L);
      dffa.setFinalFrequency("n5", 400L);
      dffa.setFinalFrequency("n6", 0L);
      dffa.setFinalFrequency("n7", 100L);
      dffa.setInitialFrequency("", 1000L);
      dffa.setTransitionFunction("", "a","n1");
      dffa.setTransitionFrequency("", "a", "n1", 1000L);
      dffa.setTransitionFunction("n1", "b","n2");
      dffa.setTransitionFrequency("n1", "b", "n2", 900L);
      dffa.setTransitionFunction("n2", "c","n3");
      dffa.setTransitionFrequency("n2", "c", "n3", 200L);
      dffa.setTransitionFunction("n2", "a","n4");
      dffa.setTransitionFrequency("n2", "a", "n4", 500L);
      dffa.setTransitionFunction("n3", "a","n4");
      dffa.setTransitionFrequency("n3", "a", "n4", 200L);
      dffa.setTransitionFunction("n4", "c","n5");
      dffa.setTransitionFrequency("n4", "c", "n5", 400L);
      dffa.setTransitionFunction("n4", "b","n6");
      dffa.setTransitionFrequency("n4", "b", "n6", 200L);
      dffa.setTransitionFunction("n2", "b","n7");
      dffa.setTransitionFrequency("n2", "b", "n7", 100L);
      dffa.setTransitionFunction("n1", "a","n7");
      dffa.setTransitionFrequency("n1", "a", "n7", 100L);
      dffa.setTransitionFunction("n7", "c","n6");
      dffa.setTransitionFrequency("n7", "c", "n6", 100L);
      dffa.setTransitionFunction("n6", "a","n5");
      dffa.setTransitionFrequency("n6", "a", "n5", 300L);
      dffa.setTransitionFunction("n5", "b","n3");
      dffa.setTransitionFrequency("n5", "b", "n3", 300L);
      dffa.setTransitionFunction("n2", "b","n2");
      dffa.setTransitionFrequency("n2", "b", "n2", 100L);
      dffa.show(dffa, "DFFA MODEL");*/
    /*  FPTA fpta1 = new FPTA();
      fpta1.alphabet = new HashSet<>();
      fpta1.states = new HashSet<>();
      fpta1.states.add("");
      fpta1.setInitialFrequency("", 0L);
      FPTA res = ALERGIA.alphaStochasticFold(fpta1, fpta, 3);
      FPTA.removeZeroLink(res, "");
      res.show(res, "res");
      FPTA dffa2=fpta.firstLevelConversion(res);
      dffa2.showfirst(dffa2, "FirstDFFAtoDFGConversion");
      FPTA DFG = new FPTA();
      DFG.alphabet = new HashSet<>();
      DFG.states = new HashSet<>();
      List<String> visitedStates = new ArrayList<String>();
      DFG.states.add("");
      DFG.setInitialFrequency("", dffa2.getInitialFrequencies().get(""));
      Map<String, Long> incomming = new HashMap<>();
      incomming.put("", dffa2.getInitialFrequencies().get(""));
      fpta.convertDFFAtoDFG1(dffa2,"", DFG, visitedStates,incomming);
      incomming.put("O", dffa2.getInitialFrequencies().get(""));
      DFG.setFinalFrequency("O", dffa2.getInitialFrequencies().get(""));
      List<String> visitedList = new ArrayList<String>();
    //  dffa2.rebalancing(DFG,"", incomming,visitedList);
     // dffa2.rebalancing1(DFG,"", incomming,visitedList);
      DFG.showDFG(DFG,"DFG Model");*/
    }
    public static HashMap<String, Double> extractNodeEffects(FPTA fpta,HashMap<String, Double>  eventLog,int actionSize){
		List<String> nodes = ALERGIA.listNonCycle(fpta);
		HashMap<String, Double> x = new HashMap<String, Double>();
		double defaultER =new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(fpta, eventLog, actionSize);

		for(String node : nodes)
      	{
      		//fpta.states.remove(node);
      		FPTA copy = fpta.cloneFPTA();   		
      		copy.deleteState(copy, node);
      		double dx1 = new EntropicRelevanceCalculator(BackGroundType.U).calculateEntropic(copy, eventLog, actionSize);
      		x.put(node, dx1-defaultER);	
      	//	System.out.println(node+" "+x);
      		//fpta.states.add(node);
      	}
		return x;
	}
    
}