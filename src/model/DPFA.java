package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.apache.commons.math3.fraction.Fraction;

public class DPFA extends Model {
	
	public Map<String, Fraction> finalProbabilities;
    public Map<String, Map<String, Map<String, Fraction>>> transitionProbabilities;
    protected Map<String, Fraction> initialProbabilities;
    
	public DPFA()
	{
		 super(MODELTYPE.DPFA);
		 this.initialProbabilities = new HashMap<>();
	        this.finalProbabilities = new HashMap<>();
	        this.transitionProbabilities = new HashMap<>();
	}
    public DPFA(Set<String> states, Set<String> alphabet) {
 
        super(states, alphabet,MODELTYPE.DPFA);
        this.initialProbabilities = new HashMap<>();
        this.initialFrequencies = new HashMap<>();
        this.finalProbabilities = new HashMap<>();
        this.transitionProbabilities = new HashMap<>();
        this.transitionFunction = new HashMap<>();
        this.finalFrequencies = new HashMap<>();
        
    }

    public static void show(DPFA dpfa) {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        // Show DFFA
       
        // Show DPFA
        for (String state : dpfa.states) {
            String vertexName = state + ":FP(" + dpfa.finalProbabilities.get(state) + ")";
            if (dpfa.initialProbabilities.containsKey(state) && dpfa.initialProbabilities.get(state).doubleValue() > 0) {
                vertexName = "IP(" + dpfa.initialProbabilities.get(state) + "):" + vertexName;
            }
            graph.insertVertex(parent, null, vertexName, 0, 0, 100, 30);
        }

        for (String fromState : dpfa.transitionProbabilities.keySet()) {
            for (String symbol : dpfa.transitionProbabilities.get(fromState).keySet()) {
                for (String toState : dpfa.transitionProbabilities.get(fromState).get(symbol).keySet()) {
                    String fromVertexName = fromState + ":FP(" + dpfa.finalProbabilities.get(fromState) + ")";
                    if (dpfa.initialProbabilities.containsKey(fromState) && dpfa.initialProbabilities.get(fromState).doubleValue() > 0) {
                        fromVertexName = "IP(" + dpfa.initialProbabilities.get(fromState) + "):" + fromVertexName;
                    }
                    String toVertexName = toState + ":FP(" + dpfa.finalProbabilities.get(toState) + ")";
                    if (dpfa.initialProbabilities.containsKey(toState) && dpfa.initialProbabilities.get(toState).doubleValue() > 0) {
                        toVertexName = "IP(" + dpfa.initialProbabilities.get(toState) + "):" + toVertexName;
                    }
                    String edgeName = symbol + ":" + dpfa.transitionProbabilities.get(fromState).get(symbol).get(toState) ;
                    Object fromVertex = getVertex(graph, parent, fromVertexName);
                    Object toVertex = getVertex(graph, parent, toVertexName);
                    graph.insertEdge(parent, null, edgeName, fromVertex, toVertex);
                }
            }
        }

        setVertexPositions(graph, parent);
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        JFrame frame = new JFrame("Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    private static Object getVertex(mxGraph graph, Object parent, String vertexName) {
        for (Object vertex : graph.getChildVertices(parent)) {
            if (graph.getModel().getValue(vertex).equals(vertexName)) {
                return vertex;
            }
        }
        return null;
    }
    public static DFFA convertFromDFFA(DFFA dffa) {

      
    	dffa.setInitialProbability("",1);
        for(String state:dffa.states)
        {
        	//dpfa.states.add(state);
        	double total = dffa.calculatetotalFrequency(dffa, state);
        	dffa.setFinalProbability(state, dffa.getFinalFrequency(state)/total);
        	for(String symbol:dffa.alphabet)
        	{
        		
        		//dpfa.alphabet.add(symbol);
        		if(dffa.getTransitionFunction().containsKey(state+symbol))
        		{
        			String next = dffa.getTransitionFunction().get(state+symbol);
        			double value = dffa.getTransitionFrequencies().get(state).get(symbol).get(next);
        			dffa.setTransitionFrequency(state, symbol, next, value);
        	
        			dffa.setTransitionprobability(state, symbol, next, new Fraction((double)value/(double)total,4));
        		}
        	}
        }
        // Calculate FREQ for each state
       /* Map<String, Double> freq = new HashMap<>();
        for (String state : dffa.getStates()) {
        	
            freq.put(state, (double)dffa.getFinalFrequencies().getOrDefault(state, (double) 0));
            for (String symbol : dffa.getAlphabet()) {
                String nextState = dffa.getTransitionFunction().get(state + symbol);
                if (nextState != null) {
                    freq.put(state, freq.get(state) + dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState));
                }
            }
        }

        // Set initial probabilities
        for (String state : dffa.getStates()) {
            double numerator = dffa.getInitialFrequencies().getOrDefault(state, (double)0);
            double denominator = freq.get(state);
            if(denominator==0)
            	denominator=1;
      
            dpfa.setInitialProbability(state, new Fraction(numerator, denominator,4));

            
        }

        // Set final probabilities
        for (String state : dffa.getStates()) {
        	dpfa.setFinalFrequency(state, 0);
            double numerator = dffa.getFinalFrequencies().getOrDefault(state,(double) 0);
            double denominator = freq.get(state);
            if (denominator==0)
            	denominator=1;
            try {
            	dpfa.setFinalProbability(state, new Fraction(numerator, denominator,4));
            }catch(Exception e) {
            	dpfa.setFinalProbability(state,new Fraction(0));
            }
        }

        // Set transition probabilities
        for (String state : dffa.getStates()) {
            for (String symbol : dffa.getAlphabet()) {
                String nextState = dffa.getTransitionFunction().get(state + symbol);
                if (nextState != null) {
                    double numerator = dffa.getTransitionFrequencies().get(state).get(symbol).get(nextState);
                    double denominator = freq.get(state);
                    if(denominator==0)
                    	denominator=1;
                    try {
                    dpfa.setTransitionProbability(state, symbol, nextState, new Fraction(numerator, denominator,4));
                    dpfa.setTransitionFrequency(state, symbol, state, denominator);
                    }catch(Exception e) {
                        dpfa.setTransitionProbability(state, symbol, nextState, new Fraction(0));

                    }
                    dpfa.setTransitionFunction(state, symbol, nextState);
                }
            }
        }*/

        return dffa;
    }
    private static void setVertexPositions(mxGraph graph, Object parent) {
        int numVertices = graph.getChildVertices(parent).length;
        int levels = (int) Math.log(numVertices) + 1;
        int xSpacing = 150;
        int ySpacing = 100;
        int xOffset = 50;
        int yOffset = 50;

        int level = 0;
        int index = 0;

        for (Object vertex : graph.getChildVertices(parent)) {
            int x = xOffset + index * xSpacing;
            int y = yOffset + level * ySpacing;

            graph.getModel().setGeometry(vertex, new mxGeometry(x, y, 100, 30));
            
            index++;
            if (index >= Math.pow(2, level)) {
                index = 0;
                level++;
            }
        }
    }
    public static void main(String[] args) {
    /*	Set<String> states = new HashSet<>(Arrays.asList("q1", "q2", "q3", "q4"));
    	Set<String> alphabet = new HashSet<>(Arrays.asList("a", "b"));

    	DPFA dpfa = new DPFA(states, alphabet);
    	dpfa.setInitialProbability("q1", 1.0);
    	dpfa.setInitialProbability("q2", 0.0);
    	dpfa.setInitialProbability("q3", 0.0);
    	dpfa.setInitialProbability("q4", 0.0);
    	dpfa.setFinalProbability("q1", 0.0);
    	dpfa.setFinalProbability("q2", 0.0);
    	dpfa.setFinalProbability("q3", 0.9);
    	dpfa.setFinalProbability("q4", 0.5);
    	dpfa.setTransitionProbability("q1", "a", "q2", 1);
    	
    	dpfa.setTransitionProbability("q2", "b", "q4",1);
    	dpfa.setTransitionProbability("q3", "b", "q4", 0.1);
    	dpfa.setTransitionProbability("q4", "b", "q3", 0.5);

    	if (dpfa.isDPFA()) {
    	    System.out.println("The PFA is a DPFA");
    	 //   System.out.println(dpfa.generateString()+"<--          ");
    	} else {
    	    System.out.println("The PFA is not a DPFA");
    	}
    	String x ="ab";
    	System.out.println( dpfa.computeStringProbability(x));
    	double forwardProbability = dpfa.computeStringProbability(x);
        double backwardProbability = dpfa.computeStringProbabilityWithBackward(x);
        assert Math.abs(forwardProbability - backwardProbability) < 1e-6;
        System.out.println("Probability of string '" + x + "' (FORWARD): " + forwardProbability);
        System.out.println("Probability of string '" + x + "' (BACKWARD): " + backwardProbability);*/

    	    // Print the result
    	 Set<String> states = new HashSet<>(Arrays.asList("q1", "q2", "q3", "q4"));
         Set<String> alphabet = new HashSet<>(Arrays.asList("a", "b"));

      
    	
    }
    public  Map<String, Map<String, Map<String, Fraction>>> getTransitionProbabilities()
    {
    	return transitionProbabilities;
    }
    public void setInitialProbability(String state, Fraction probability) {
        initialProbabilities.put(state, probability);
    }

    public void setFinalProbability(String state, Fraction probability) {
        finalProbabilities.put(state, probability);
    }
    public Fraction getFinialProbeblity(String state)
    {
    	return finalProbabilities.get(state);
    }
    public Fraction getTransitionProbeblity(String source,String symbol,String destination) {
    	return transitionProbabilities.get(source).get(symbol).get(destination);
    }
    public void setTransitionProbability(String fromState, String symbol, String toState, Fraction probability) {
        transitionProbabilities.computeIfAbsent(fromState, k -> new HashMap<>())
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .put(toState, probability);
    }

}