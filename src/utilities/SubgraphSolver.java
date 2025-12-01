package utilities;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import model.DFFA;
import model.FPTA;

import java.text.DecimalFormat;
import java.util.*;

public class SubgraphSolver {
    static class Edge {
        int from, to;
        double frequency;
        String label;
        int id;
        Edge(int id, int from, int to, double frequency, String label) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.frequency = frequency;
            this.label = label;
        }
    }
    public static DFFAGraph convertDFvMstructureToGraph(DFFA dffa,HashMap<String, Integer> stateToNum)
    {
    	DFFAGraph graph = new DFFAGraph(dffa.states.size(), 0);
    	
    	int counter=0;
    	for(String state:dffa.states)
    	{
    			stateToNum.put(state, counter);
    			double x = 0;
    			try{
    				
    			x = dffa.getTransitionFrequencies().get(state).get("O").get("O");
    			}catch(Exception e)
    			{
    				
    			}
    			graph.rootVertex = counter;
    			graph.addVertex(counter, x);
    			counter++;
    	
    		
    	}
    	for(String state:dffa.states)
    	{
    		
    		int source = stateToNum.get(state);
    		for(String symbol:dffa.alphabet)
    			if(dffa.transitionFunction.containsKey(state+symbol))
    			{
    				String next = dffa.transitionFunction.get(state+symbol);
    				int destination = stateToNum.get(next);
    				double freq = dffa.transitionFrequencies.get(state).get(symbol).get(next)!=null?dffa.transitionFrequencies.get(state).get(symbol).get(next):0;
    				graph.addEdge(source, destination, freq, symbol); // back 
    			}
    	}
    	return graph;
    }
    public static DFFAGraph convertDFFAstructureToGraph(DFFA dffa,HashMap<String, Integer> stateToNum)
    {
    	DFFAGraph graph = new DFFAGraph(dffa.states.size(), 0);
    	
    	int counter=0;
    	for(String state:dffa.states)
    	{
    		stateToNum.put(state, counter);
    		graph.addVertex(counter, dffa.getFinalFrequency(state));
    		counter++;
    	}
    	for(String state:dffa.states)
    	{
    		int source = stateToNum.get(state);
    		for(String symbol:dffa.alphabet)
    			if(dffa.transitionFunction.containsKey(state+symbol))
    			{
    				String next = dffa.transitionFunction.get(state+symbol);
    				int destination = stateToNum.get(next);
    				double freq = dffa.transitionFrequencies.get(state).get(symbol).get(next)!=null?dffa.transitionFrequencies.get(state).get(symbol).get(next):0;
    				graph.addEdge(source, destination, freq, symbol); // back 
    			}
    	}
    	return graph;
    }
  
    static class DFFAGraph {
        int numVertices;
        int rootVertex;
        List<Edge> edges = new ArrayList<>();
        Map<Integer, Double> vertexFrequencies = new HashMap<>();
        int numEdges = 0;

        DFFAGraph(int numVertices, int rootVertex) {
            this.numVertices = numVertices;
            this.rootVertex = rootVertex;
        }

        void addVertex(int v, double frequency) {
            vertexFrequencies.put(v, frequency);
        }

        void addEdge(int from, int to, double frequency, String label) {
            edges.add(new Edge(numEdges++, from, to, frequency, label));
        }
    }

    public static void main(String[] args) {
    }
    public static FPTA solvePFTAStructure(DFFA dffa,double minimumFrequency) {
        Loader.loadNativeLibraries();
        HashMap<String, Integer> stateToNum = new HashMap<String, Integer>();
        HashMap<Integer, String> numToState  = new HashMap<Integer, String>();
        DFFAGraph graph = convertDFvMstructureToGraph(dffa,stateToNum);
        numToState.put(0,"");
        for(String s:stateToNum.keySet())
        {
        	numToState.put(stateToNum.get(s), s);
        }
    	int V = graph.numVertices;
        int E = graph.edges.size();
        int root = stateToNum.get("I");
        System.out.println(root+" "+numToState.get(root));
        FPTA subDffa = new FPTA();
      //  System.out.println("Total elements (vertices + edges): " + (V + E));
      //  System.out.println("Allowed elements (integer): " + maxElements);

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.err.println("Could not create solver SCIP");
            return subDffa;
        }

        // Variables
        MPVariable[] x = new MPVariable[V]; // node selection
        MPVariable[] y = new MPVariable[E]; // edge selection
        MPVariable[] f = new MPVariable[E]; // flow on edges

        for (int v = 0; v < V; v++) {
            x[v] = solver.makeBoolVar("x_" + v);
        }
        for (int e = 0; e < E; e++) {
            y[e] = solver.makeBoolVar("y_" + e);
            f[e] = solver.makeNumVar(0.0, V - 1, "f_" + e);
        }

        // Objective
        MPObjective objective = solver.objective();
        for (int v = 0; v < V; v++) {
            objective.setCoefficient(x[v], 1.0);
        }
        for (int e = 0; e < E; e++) {
            objective.setCoefficient(y[e], 1.0);
        }
        objective.setMinimization();

        // Root inclusion
        MPConstraint rootInclusion = solver.makeConstraint(1.0, 1.0, "root_inclusion");
        MPConstraint sinkInclusion = solver.makeConstraint(1.0, 1.0, "sink_inclusion");

        rootInclusion.setCoefficient(x[root], 1.0);
        sinkInclusion.setCoefficient(x[stateToNum.get("O")], 1.0);
        // Size constraint: only upper bound
        MPConstraint freqConstraint = solver.makeConstraint(minimumFrequency, Double.POSITIVE_INFINITY, "freq_threshold");
        for (int e = 0; e < E; e++) {
        	freqConstraint.setCoefficient(y[e], graph.edges.get(e).frequency);
        }
        // Edge-vertex consistency
        for (int e = 0; e < E; e++) {
            Edge edge = graph.edges.get(e);
            MPConstraint edgeFrom = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "edge_from_" + e);
            edgeFrom.setCoefficient(y[e], 1.0);
            edgeFrom.setCoefficient(x[edge.from], -1.0);
            MPConstraint edgeTo = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "edge_to_" + e);
            edgeTo.setCoefficient(y[e], 1.0);
            edgeTo.setCoefficient(x[edge.to], -1.0);
        }

        // Flow capacity: flow only on selected edges
        for (int e = 0; e < E; e++) {
            MPConstraint flowCapacity = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "flow_capacity_" + e);
            flowCapacity.setCoefficient(f[e], 1.0);
            flowCapacity.setCoefficient(y[e], -(V - 1));
        }

        // Flow conservation at each node
        for (int v = 0; v < V; v++) {
            MPConstraint flowCons;
            if (v == root) {
                // At root: outgoing flow = total selected nodes minus 1 (the root itself)
                flowCons = solver.makeConstraint(0, 0, "flow_root");
                for (int e = 0; e < E; e++) {
                    if (graph.edges.get(e).from == root) flowCons.setCoefficient(f[e], 1.0);
                    if (graph.edges.get(e).to == root) flowCons.setCoefficient(f[e], -1.0);
                }
                for (int u = 0; u < V; u++) {
                    if (u != root) flowCons.setCoefficient(x[u], -1.0);
                }
            } else {
                // At other nodes: incoming - outgoing = x[v]
                flowCons = solver.makeConstraint(0, 0, "flow_conservation_" + v);
                for (int e = 0; e < E; e++) {
                    if (graph.edges.get(e).to == v) flowCons.setCoefficient(f[e], 1.0);
                    if (graph.edges.get(e).from == v) flowCons.setCoefficient(f[e], -1.0);
                }
                flowCons.setCoefficient(x[v], -1.0);
            }
        }

        // Solve
        MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
          //  System.out.println("Optimal Solution Found!");
            System.out.println("Objective Value: " + objective.value());

            // Detailed debug output of variable values
      //      System.out.println("\nSelected Vertices and their values:");
            for (int v = 0; v < V; v++) {
                System.out.printf("Vertex %d: x[%d] = %.3f\n", v, v, x[v].solutionValue());
            }
            System.out.println("--------------------------------------");

      //      System.out.println("\nSelected Edges and their values:");
      //      for (int e = 0; e < E; e++) {
      //          Edge edge = graph.edges.get(e);
      //          System.out.printf("Edge %d (%d->%d, label=%s): y[%d] = %.3f, flow f[%d] = %.3f\n",
      //                  e, edge.from, edge.to, edge.label, e, y[e].solutionValue(), e, f[e].solutionValue());
      //      }

            // Summary output
          //  System.out.print("\nSelected Vertices (x[v] > 0.5): ");
            for (int v = 0; v < V; v++) if (x[v].solutionValue() > 0.5) 
            {
            	subDffa.states.add(numToState.get(v));
            }
            	//System.out.print(v + " ");
           // System.out.println();

           // System.out.print("Selected Edges (y[e] > 0.5): ");
            for (int e = 0; e < E; e++) if (y[e].solutionValue() > 0.5) {
            	
                Edge edge = graph.edges.get(e);
                subDffa.alphabet.add(edge.label);
                subDffa.setTransitionFunction(numToState.get(edge.from), edge.label, numToState.get(edge.to));
                double freq=dffa.getTransitionFrequencies().get(numToState.get(edge.from)).get(edge.label).get(numToState.get(edge.to));
                subDffa.setTransitionFrequency(numToState.get(edge.from), edge.label, numToState.get(edge.to), freq);
                //  System.out.print("(" + edge.from + "->" + edge.to + ", " + edge.label + ") ");
            }
     //       System.out.println();

        } else {
            System.out.println("No feasible solution found.");
    		DecimalFormat df = new DecimalFormat("0.0000");
        }
       // rebalancefrequencies(dffa,subDffa);
       // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
        return subDffa;
    }

    public static FPTA solveDFFAOptimization(DFFA dffa,int maxElements) {
        Loader.loadNativeLibraries();
        HashMap<String, Integer> stateToNum = new HashMap<String, Integer>();
        HashMap<Integer, String> numToState  = new HashMap<Integer, String>();
        DFFAGraph graph = convertDFFAstructureToGraph(dffa,stateToNum);
        numToState.put(0,"");
        for(String s:stateToNum.keySet())
        {
        	numToState.put(stateToNum.get(s), s);
        }
    	int V = graph.numVertices;
        int E = graph.edges.size();
        int root = graph.rootVertex;
        FPTA subDffa = new FPTA();
      //  System.out.println("Total elements (vertices + edges): " + (V + E));
      //  System.out.println("Allowed elements (integer): " + maxElements);

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.err.println("Could not create solver SCIP");
            return subDffa;
        }

        // Variables
        MPVariable[] x = new MPVariable[V]; // node selection
        MPVariable[] y = new MPVariable[E]; // edge selection
        MPVariable[] f = new MPVariable[E]; // flow on edges

        for (int v = 0; v < V; v++) {
            x[v] = solver.makeBoolVar("x_" + v);
        }
        for (int e = 0; e < E; e++) {
            y[e] = solver.makeBoolVar("y_" + e);
            f[e] = solver.makeNumVar(0.0, V - 1, "f_" + e);
        }

        // Objective
        MPObjective objective = solver.objective();
        for (int v = 0; v < V; v++) {
            double freq = graph.vertexFrequencies.getOrDefault(v, 0.0);
            objective.setCoefficient(x[v], freq);
        }
        for (int e = 0; e < E; e++) {
            objective.setCoefficient(y[e], graph.edges.get(e).frequency);
        }
        objective.setMaximization();

        // Root inclusion
        MPConstraint rootInclusion = solver.makeConstraint(1.0, 1.0, "root_inclusion");
        rootInclusion.setCoefficient(x[root], 1.0);

        // Size constraint: only upper bound
        MPConstraint sizeConstraint = solver.makeConstraint(1, maxElements, "size_constraint");
        for (int v = 0; v < V; v++) sizeConstraint.setCoefficient(x[v], 1.0);
        for (int e = 0; e < E; e++) sizeConstraint.setCoefficient(y[e], 1.0);

        // Edge-vertex consistency
        for (int e = 0; e < E; e++) {
            Edge edge = graph.edges.get(e);
            MPConstraint edgeFrom = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "edge_from_" + e);
            edgeFrom.setCoefficient(y[e], 1.0);
            edgeFrom.setCoefficient(x[edge.from], -1.0);
            MPConstraint edgeTo = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "edge_to_" + e);
            edgeTo.setCoefficient(y[e], 1.0);
            edgeTo.setCoefficient(x[edge.to], -1.0);
        }

        // Flow capacity: flow only on selected edges
        for (int e = 0; e < E; e++) {
            MPConstraint flowCapacity = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "flow_capacity_" + e);
            flowCapacity.setCoefficient(f[e], 1.0);
            flowCapacity.setCoefficient(y[e], -(V - 1));
        }

        // Flow conservation at each node
        for (int v = 0; v < V; v++) {
            MPConstraint flowCons;
            if (v == root) {
                // At root: outgoing flow = total selected nodes minus 1 (the root itself)
                flowCons = solver.makeConstraint(0, 0, "flow_root");
                for (int e = 0; e < E; e++) {
                    if (graph.edges.get(e).from == root) flowCons.setCoefficient(f[e], 1.0);
                    if (graph.edges.get(e).to == root) flowCons.setCoefficient(f[e], -1.0);
                }
                for (int u = 0; u < V; u++) {
                    if (u != root) flowCons.setCoefficient(x[u], -1.0);
                }
            } else {
                // At other nodes: incoming - outgoing = x[v]
                flowCons = solver.makeConstraint(0, 0, "flow_conservation_" + v);
                for (int e = 0; e < E; e++) {
                    if (graph.edges.get(e).to == v) flowCons.setCoefficient(f[e], 1.0);
                    if (graph.edges.get(e).from == v) flowCons.setCoefficient(f[e], -1.0);
                }
                flowCons.setCoefficient(x[v], -1.0);
            }
        }

        // Solve
        MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
          //  System.out.println("Optimal Solution Found!");
          //  System.out.println("Objective Value: " + objective.value());

            // Detailed debug output of variable values
      //      System.out.println("\nSelected Vertices and their values:");
      //      for (int v = 0; v < V; v++) {
      //          System.out.printf("Vertex %d: x[%d] = %.3f\n", v, v, x[v].solutionValue());
      //      }

      //      System.out.println("\nSelected Edges and their values:");
      //      for (int e = 0; e < E; e++) {
      //          Edge edge = graph.edges.get(e);
      //          System.out.printf("Edge %d (%d->%d, label=%s): y[%d] = %.3f, flow f[%d] = %.3f\n",
      //                  e, edge.from, edge.to, edge.label, e, y[e].solutionValue(), e, f[e].solutionValue());
      //      }

            // Summary output
          //  System.out.print("\nSelected Vertices (x[v] > 0.5): ");
            for (int v = 0; v < V; v++) if (x[v].solutionValue() > 0.5) 
            {
            	subDffa.states.add(numToState.get(v));
            }
            	//System.out.print(v + " ");
           // System.out.println();

           // System.out.print("Selected Edges (y[e] > 0.5): ");
            for (int e = 0; e < E; e++) if (y[e].solutionValue() > 0.5) {
            	
                Edge edge = graph.edges.get(e);
                subDffa.alphabet.add(edge.label);
                subDffa.setTransitionFunction(numToState.get(edge.from), edge.label, numToState.get(edge.to));
                double freq=dffa.getTransitionFrequencies().get(numToState.get(edge.from)).get(edge.label).get(numToState.get(edge.to));
                subDffa.setTransitionFrequency(numToState.get(edge.from), edge.label, numToState.get(edge.to), freq);
                //  System.out.print("(" + edge.from + "->" + edge.to + ", " + edge.label + ") ");
            }
     //       System.out.println();

        } else {
            System.out.println("No feasible solution found.");
    		DecimalFormat df = new DecimalFormat("0.0000");
            dffa.show(dffa, " "+maxElements);
        }
        rebalancefrequencies(dffa,subDffa);
       // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
        return subDffa;
    }
    public static void rebalancefrequencies(DFFA dffa,DFFA subgraph)
    {
    	dffa.rebalancePercentages(dffa);
    	boolean flag=false;
    	subgraph.setInitialFrequency("", dffa.getInitialFrequencies().get(""));
    	HashMap<String,Double> inputfrequency = new HashMap<String, Double>();
    	inputfrequency.put("", dffa.getInitialFrequencies().get(""));
    	
    	for(String state:subgraph.states)
    	{
    		double total=0;
    		for(String sym:subgraph.alphabet)
    		{
    			if(subgraph.getTransitionFunction().containsKey(state+sym))
    			{
    				String next=subgraph.getTransitionFunction().get(state+sym);
    				double val = dffa.transitionPercentage.get(state).get(sym).get(next);
    				total +=val;
    				subgraph.setTransitionPercentage(state, sym, next, val);
    			}
    		}
    		subgraph.setFinalProbability(state, dffa.getFinalProbability(state));
    		total+=dffa.getFinalProbability(state);
    		if(total<1)
    			subgraph.setFinalProbability(state,subgraph.getFinalProbability(state)+(1-total));
    	}
   /* 	for(String state:subgraph.states)
    	{
    	//	for(String sym:subgraph.alphabet)
    	//	{
    		/*	if(subgraph.getTransitionFunction().containsKey(state+sym))
    			{
    				String next=subgraph.getTransitionFunction().get(state+sym);
    				double val = dffa.transitionFrequencies.get(state).get(sym).get(next);
    				double nextFreq = inputfrequency.get(next)!=null?inputfrequency.get(next)+val:val;
    				inputfrequency.put(next, nextFreq);
    			}
    		}*/
    		
    	//	double in = dffa.calculateIncomingFrequencies(subgraph, state);
    //		double out = dffa.calculateOutgoingFrequencies(subgraph, state);
    	
    //		if(in>=out)
    //		{
    //			subgraph.setFinalFrequency(state, in-out);
    //		}
    //		else
   // 		{
   // 			flag=true;
   // 			System.out.println("OOOOOOOOO Error !!!!");
   // 			System.out.println(state+" "+" in("+in+") out("+out+")");
   // 		}
   // 	}*/
    /*	for(String state:subgraph.states)
    	{
    		for(String sym:subgraph.alphabet)
    		{
    			if(subgraph.getTransitionFunction().containsKey(state+sym))
    			{
    				String next=subgraph.getTransitionFunction().get(state+sym);
    				double val = inputfrequency.get(state)*dffa.transitionPercentage.get(state).get(sym).get(next);

    			}
    		}
    	}*/
  
    	 FPTA dffa2=subgraph.firstLevelPercentageConversion(subgraph);
    	 List<String> x =subgraph.extractEquations1(dffa2);
    	
    	 try {
    		 Map<String,Double> y = CoefficientMatrix.findCoefficient(x);
    		 subgraph.updateTransitionFrequency1(subgraph,y);
    	 }catch(Exception e)
    	 {
    		 for(String ss:x)
    	    	System.out.println(ss);
    	 }
    	
    	 for(String state:subgraph.states)
     	{
    		 double in = subgraph.calculateIncomingFrequencies(subgraph, state);
    	     double out = subgraph.calculateOutgoingFrequencies(subgraph, state);
    	     if(in>=out )
    	    	 subgraph.setFinalFrequency(state, in-out);
    	     else if(out-in<in/100)
    	     {
    	    	 subgraph.setFinalFrequency(state, 0);
    	     }
    	     else
    	     {
    	    //	 System.out.println("OOOOOOOOO Error !!!!");
    	    //	 System.out.println(state+" "+in+" "+out);
    	    	
    	    	flag=true;
    	     }
    	    	 
     	}
    	 
    	 if(flag==true)
    	 {
    
    	//	 subgraph.show(subgraph, "first");
    	//	 subgraph.showPercentage(dffa2, "percentage");
    	//	 for(String z:x)
    	//	    System.out.println(z);
    	//	 System.out.println("*************************************");
    		
    		 
    	 }
    }
}
