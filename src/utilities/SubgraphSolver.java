package utilities;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import model.DFFA;
import model.FPTA;

import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.math3.fraction.Fraction;

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
    		
    			graph.addVertex(counter, x);
    			counter++;
    	
    		
    	}
    	graph.rootVertex = stateToNum.get("I");
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
    
    public static FPTA extractMaxFrequencySubtree(DFFA dffa, int maxVertices) {
    	 HashMap<String, Integer> stateToNum = new HashMap<String, Integer>();
         HashMap<Integer, String> numToState  = new HashMap<Integer, String>();
    	DFFAGraph graph = convertDFFAstructureToGraph(dffa,stateToNum);
        int n = graph.numVertices;
        int root = graph.rootVertex;
        FPTA subDffa = new FPTA();
        if (maxVertices <= 0) {
            throw new IllegalArgumentException("maxVertices must be > 0");
        }
        if (maxVertices > n) {
            maxVertices = n;
        }

        // --- build children list and parent array (tree is directed from parent -> child) ---
        @SuppressWarnings("unchecked")
        java.util.List<Integer>[] children = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++) {
            children[i] = new java.util.ArrayList<>();
        }
        int[] parent = new int[n];
        java.util.Arrays.fill(parent, -1);

        for (Edge e : graph.edges) {
            children[e.from].add(e.to);
            parent[e.to] = e.from;
        }

        // --- build a traversal order and then reverse it to get bottom-up order ---
        java.util.ArrayList<Integer> order = new java.util.ArrayList<>();
        java.util.Deque<Integer> stack = new java.util.ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            order.add(u);
            for (int v : children[u]) {
                stack.push(v);
            }
        }
        // reverse to get children before parents
        java.util.Collections.reverse(order);

        // --- DP arrays: dp[u][k] = best sum for subtree rooted at u with exactly k nodes including u ---
        double[][] dp = new double[n][maxVertices + 1];
        int[] maxKAtNode = new int[n];  // maximum k for which dp[u][k] is defined

        for (int i = 0; i < n; i++) {
            java.util.Arrays.fill(dp[i], Double.NEGATIVE_INFINITY);
        }

        // --- bottom-up DP over nodes in postorder ---
        for (int u : order) {
            double[] cur = new double[maxVertices + 1];
            java.util.Arrays.fill(cur, Double.NEGATIVE_INFINITY);

            double w = graph.vertexFrequencies.get(u);
            cur[1] = w;          // take only u itself
            int curMax = 1;

            // merge each child one by one (knapsack-like)
            for (int v : children[u]) {
                double[] next = new double[maxVertices + 1];
                java.util.Arrays.fill(next, Double.NEGATIVE_INFINITY);

                int childMax = maxKAtNode[v];
                for (int used = 1; used <= curMax; used++) {
                    if (cur[used] == Double.NEGATIVE_INFINITY) continue;

                    // option 1: skip entire child v
                    if (cur[used] > next[used]) {
                        next[used] = cur[used];
                    }

                    // option 2: take t nodes from child's subtree
                    int maxT = Math.min(childMax, maxVertices - used);
                    for (int t = 1; t <= maxT; t++) {
                        if (dp[v][t] == Double.NEGATIVE_INFINITY) continue;
                        double val = cur[used] + dp[v][t];
                        if (val > next[used + t]) {
                            next[used + t] = val;
                        }
                    }
                }

                cur = next;
                curMax = Math.min(maxVertices, curMax + childMax);
            }

            maxKAtNode[u] = curMax;
            for (int k = 1; k <= curMax; k++) {
                dp[u][k] = cur[k];
            }
        }

        // --- pick best size k at the root ---
        double bestVal = Double.NEGATIVE_INFINITY;
        int bestK = 0;
        for (int k = 1; k <= maxVertices; k++) {
            if (dp[root][k] > bestVal) {
                bestVal = dp[root][k];
                bestK = k;
            }
        }

        // --- reconstruction (iterative, no extra methods) ---
        boolean[] used = new boolean[n];

        java.util.Deque<Integer> nodeStack = new java.util.ArrayDeque<>();
        java.util.Deque<Integer> kStack = new java.util.ArrayDeque<>();
        nodeStack.push(root);
        kStack.push(bestK);

        while (!nodeStack.isEmpty()) {
            int u = nodeStack.pop();
            int k = kStack.pop();
            used[u] = true;

            java.util.List<Integer> ch = children[u];
            int m = ch.size();

            // local DP for reconstruction, only up to k
            double[][] f = new double[m + 1][k + 1];
            for (int i = 0; i <= m; i++) {
                java.util.Arrays.fill(f[i], Double.NEGATIVE_INFINITY);
            }
            int[] curMaxStage = new int[m + 1];

            double w = graph.vertexFrequencies.get(u);
            f[0][1] = w;
            curMaxStage[0] = 1;

            // forward pass: same idea as above but limited to k
            for (int i = 0; i < m; i++) {
                int v = ch.get(i);
                double[] prev = f[i];
                double[] cur = f[i + 1];
                java.util.Arrays.fill(cur, Double.NEGATIVE_INFINITY);

                int childMax = Math.min(maxKAtNode[v], k);
                int prevMax = curMaxStage[i];

                for (int usedCnt = 1; usedCnt <= prevMax; usedCnt++) {
                    if (prev[usedCnt] == Double.NEGATIVE_INFINITY) continue;

                    // skip child
                    if (prev[usedCnt] > cur[usedCnt]) {
                        cur[usedCnt] = prev[usedCnt];
                    }

                    // take t nodes from child v
                    int maxT = Math.min(childMax, k - usedCnt);
                    for (int t = 1; t <= maxT; t++) {
                        if (dp[v][t] == Double.NEGATIVE_INFINITY) continue;
                        double val = prev[usedCnt] + dp[v][t];
                        if (val > cur[usedCnt + t]) {
                            cur[usedCnt + t] = val;
                        }
                    }
                }

                curMaxStage[i + 1] = Math.min(k, prevMax + childMax);
            }

            // backward pass: decide for each child how many nodes we took
            int curK = k;
            for (int i = m; i >= 1; i--) {
                int v = ch.get(i - 1);
                int childMax = Math.min(maxKAtNode[v], k);
                boolean decided = false;

                for (int t = 0; t <= childMax; t++) {
                    int prevK = curK - t;
                    if (prevK < 1) continue;

                    double add = (t == 0 ? 0.0 : dp[v][t]);
                    double prevVal = f[i - 1][prevK];
                    if (prevVal == Double.NEGATIVE_INFINITY) continue;

                    if (Math.abs((prevVal + add) - f[i][curK]) < 1e-9) {
                        if (t > 0) {
                            nodeStack.push(v);
                            kStack.push(t);
                        }
                        curK = prevK;
                        decided = true;
                        break;
                    }
                }

                if (!decided) {
                    // fallback: assume we did not take this child
                }
            }
            // after this, curK should be 1 (only node u itself counted)
        }

        // --- build result DFFAGraph using original indices ---
        DFFAGraph result = new DFFAGraph(graph.numVertices, graph.rootVertex);
        for (int v = 0; v < n; v++) {
            if (used[v]) {
                double freq = graph.vertexFrequencies.get(v);
                result.addVertex(v, freq);
            }
        }
        for (Edge e : graph.edges) {
            if (used[e.from] && used[e.to]) {
                result.addEdge(e.from, e.to, e.frequency, e.label);
            }
        }
       
        numToState.put(0,"");
        for(String s:stateToNum.keySet())
        {
        	numToState.put(stateToNum.get(s), s);
        }
        for (int v = 0; v < n; v++) if (used[v]) 
        {
        	subDffa.states.add(numToState.get(v));
        	subDffa.setFinalFrequency(numToState.get(v), 1);
        }
        	//System.out.print(v + " ");
       // System.out.println();

       // System.out.print("Selected Edges (y[e] > 0.5): ");
        for (Edge e : graph.edges) if (used[e.from] && used[e.to]) {
        	
        	
            subDffa.setTransitionFunction(numToState.get(e.from), e.label, numToState.get(e.to));
            subDffa.setTransitionFrequency(numToState.get(e.from), e.label, numToState.get(e.to), e.frequency);
            //  System.out.print("(" + edge.from + "->" + edge.to + ", " + edge.label + ") ");
        }
       // rebalancefrequencies(dffa,subDffa);

        return subDffa;
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
    		DecimalFormat df = new DecimalFormat("0.0000");
        }
       // rebalancefrequencies(dffa,subDffa);
       // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
        return subDffa;
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
    public static Set<String> solvePruneOptimization(
            DFFA dffa,
            HashMap<Integer, HashMap<String, Double>> clientErEffects,
            HashMap<Integer, Double> clientEr,
            double tau) {

        Loader.loadNativeLibraries();

        // --- Step 1: Convert DFFA to graph ---
        HashMap<String, Integer> stateToNum = new HashMap<>();
        HashMap<Integer, String> numToState = new HashMap<>();
        DFFAGraph graph = convertDFFAstructureToGraph(dffa, stateToNum);
        Set<String> qtStates = clientErEffects.values().iterator().next().keySet();

        for (String s : stateToNum.keySet()) {
            numToState.put(stateToNum.get(s), s);
        }

        // --- Step 2: Build parent map ---
        Map<String, List<String>> parentMap = new HashMap<>();
        for (String s : qtStates) parentMap.put(s, new ArrayList<>());

        for (Edge e : graph.edges) {
            String from = numToState.get(e.from);
            String to = numToState.get(e.to);

            if (from != null && to != null &&
                    qtStates.contains(from) && qtStates.contains(to)) {
                parentMap.get(to).add(from);
            }
        }

        // --- Step 3: OR-Tools solver ---
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) throw new RuntimeException("Could not create solver.");

        // x_q = 1 → prune, 0 → keep
        Map<String, MPVariable> xVars = new HashMap<>();
        for (String q : qtStates) {
            xVars.put(q, solver.makeIntVar(0, 1, "x_" + q));
        }

        // --- Step 4: Hierarchy constraint ---
        // If parent is pruned → child must be pruned
        // x_parent ≤ x_child
        for (String child : qtStates) {
            for (String parent : parentMap.get(child)) {
                MPConstraint c = solver.makeConstraint(
                        Double.NEGATIVE_INFINITY, 0,
                        "hierarchy_" + parent + "_" + child);

                c.setCoefficient(xVars.get(parent), 1);
                c.setCoefficient(xVars.get(child), -1);
            }
        }

        // --- Step 5: Error budget constraint ---
        // sum(effect_q * x_q) ≤ tau * ER
        for (Integer clientId : clientEr.keySet()) {
      
            MPConstraint c = solver.makeConstraint(
                    Double.NEGATIVE_INFINITY,
                    tau * clientEr.get(clientId),
                    "error_budget_" + clientId);

            for (String q : qtStates) {
                double effect = clientErEffects.get(clientId).get(q);
                c.setCoefficient(xVars.get(q), effect);
            }
        }

        // --- Step 6: Objective ---
        // Maximize number of pruned states
        MPObjective objective = solver.objective();
        for (String q : qtStates) {
            objective.setCoefficient(xVars.get(q), 1.0);
        }
        objective.setMaximization();

        // --- Step 7: Solve ---
        MPSolver.ResultStatus resultStatus = solver.solve();
        if (resultStatus != MPSolver.ResultStatus.OPTIMAL &&
            resultStatus != MPSolver.ResultStatus.FEASIBLE) {
            throw new RuntimeException("No feasible solution found.");
        }

        // --- Step 8: Build resulting FPTA ---
        FPTA result = new FPTA();
        Set<String> keptStates = new HashSet<>();
        Set<String> prunedStates = new HashSet<>();

        for (String q : qtStates) {
            if (xVars.get(q).solutionValue() > 0.5) {
                prunedStates.add(q);
            } else {
                result.states.add(q);
                keptStates.add(q);
                result.setFinalFrequency(q, 1);
            }
        }

       

        // Add transitions between kept states only
        for (Edge e : graph.edges) {
            String from = numToState.get(e.from);
            String to = numToState.get(e.to);

            if (from != null && to != null &&
                    result.states.contains(from) &&
                    result.states.contains(to)) {

                result.setTransitionFunction(from, e.label, to);
                result.setTransitionFrequency(from, e.label, to, e.frequency);
            }
        }

        return prunedStates;
    }


    	// --- Helper: Detect if a vertex has a path to itself (loop detection) ---
    	private static boolean hasPathToSelf(DFFAGraph graph, int vertex) {
    	    return hasPathToSelfDFS(graph, vertex, vertex, new HashSet<>(), new HashSet<>());
    	}
    	private static boolean hasPathToSelfDFS(DFFAGraph graph, int current, int target, HashSet<Integer> visited, HashSet<Integer> recStack) {
    	    if (recStack.contains(current)) return current == target;
    	    if (visited.contains(current)) return false;
    	    visited.add(current);
    	    recStack.add(current);
    	    for (Edge edge : graph.edges) {
    	        if (edge.from == current) {
    	            if (edge.to == target && !visited.contains(target)) return true;
    	            if (hasPathToSelfDFS(graph, edge.to, target, visited, recStack)) return true;
    	        }
    	    }
    	    recStack.remove(current);
    	    return false;
    	}

    	// --- Helper: Get all ancestors of a vertex ---
    	private static HashSet<Integer> getAncestors(DFFAGraph graph, int vertex) {
    	    HashSet<Integer> ancestors = new HashSet<>();
    	    Queue<Integer> queue = new LinkedList<>();
    	    HashSet<Integer> visited = new HashSet<>();
    	    for (Edge edge : graph.edges) {
    	        if (edge.to == vertex) {
    	            queue.add(edge.from);
    	            ancestors.add(edge.from);
    	        }
    	    }
    	    while (!queue.isEmpty()) {
    	        int current = queue.poll();
    	        if (visited.contains(current)) continue;
    	        visited.add(current);
    	        for (Edge edge : graph.edges) {
    	            if (edge.to == current && !ancestors.contains(edge.from)) {
    	                ancestors.add(edge.from);
    	                queue.add(edge.from);
    	            }
    	        }
    	    }
    	    return ancestors;
    }
        /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

    public static FPTA solveDFvMOptimization(DFFA dffa,int maxElements) {
    	
    	/*-+-+-+-+-*/
    	 dffa.showDFvM(dffa, "before opt"+maxElements);
    	 Loader.loadNativeLibraries();
         HashMap<String, Integer> stateToNum = new HashMap<String, Integer>();
         HashMap<Integer, String> numToState  = new HashMap<Integer, String>();
         DFFAGraph graph = convertDFvMstructureToGraph(dffa,stateToNum);
         numToState.put(0,"I");
         for(String s:stateToNum.keySet())
         {
         	numToState.put(stateToNum.get(s), s);
         }
         int V = graph.numVertices;
         int E = graph.edges.size();
         int root = graph.rootVertex;
         FPTA subDffa = new FPTA();
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
         MPConstraint sinkInclusion = solver.makeConstraint(1.0, 1.0, "sink_inclusion");

         rootInclusion.setCoefficient(x[root], 1.0);
         sinkInclusion.setCoefficient(x[stateToNum.get("O")], 1.0);
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
     		DecimalFormat df = new DecimalFormat("0.0000");
     		return subDffa;
             //dffa.show(dffa, " "+maxElements);
         }
         rebalancefrequencies(dffa,subDffa);
         subDffa.showDFvM(subDffa, "after opt"+maxElements);
        // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
         return subDffa;

     
    }
    /*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

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

           // System.out.print("Selected Edges (y[e] > 0.5): ");
            for (int e = 0; e < E; e++) if (y[e].solutionValue() > 0.5) {
            	
                Edge edge = graph.edges.get(e);
                subDffa.alphabet.add(edge.label);
                
                subDffa.setTransitionFunction(numToState.get(edge.from), edge.label, numToState.get(edge.to));
                double freq=dffa.getTransitionFrequencies().get(numToState.get(edge.from)).get(edge.label).get(numToState.get(edge.to));
                	//System.out.println(numToState.get(edge.from)+" "+edge.label+" "+numToState.get(edge.to)+" "+freq);
                subDffa.setTransitionFrequency(numToState.get(edge.from), edge.label, numToState.get(edge.to), freq);
            }
     //       System.out.println();

        } else {
    		DecimalFormat df = new DecimalFormat("0.0000");
            dffa.show(dffa, " "+maxElements);
        }
      //  subDffa.show(subDffa, "subDffa");
        FPTA prunFPTA = rebalancefrequencies(dffa,subDffa);
       /* List<String> rmlist = new ArrayList<String>();
        for(String s:prunFPTA.states)
        {
        	double x1=prunFPTA.calculateIncomingArc(prunFPTA, s);
        	if(x1<1)
        	{
        		rmlist.add(s);  		
        	}
        }
        subDffa.states.removeAll(rmlist);*/
       // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
        return prunFPTA;
    }
    
    public static FPTA solveFPTADFFAOptimization(DFFA dffa,int maxElements) {
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
    		DecimalFormat df = new DecimalFormat("0.0000");
            dffa.show(dffa, " "+maxElements);
        }
        rebalancefrequencies(dffa,subDffa);
       // System.out.println(subDffa.states.size()+"<sub size"+" "+dffa.states.size());
        return subDffa;
    }
    public static FPTA rebalancefrequencies(DFFA dffa,DFFA subgraph)
    {
    	
    	dffa.rebalancePercentages(dffa);
    
    	boolean flag=false;
    	if(dffa.getInitialFrequencies().get("")!=null)
    	{
    		subgraph.setInitialFrequency("", dffa.getInitialFrequencies().get(""));
    		HashMap<String,Double> inputfrequency = new HashMap<String, Double>();
    		inputfrequency.put("", dffa.getInitialFrequencies().get(""));
    	}
    	else
    	{
    		subgraph.setInitialFrequency("I", dffa.getInitialFrequencies().get("I"));
    		HashMap<String,Double> inputfrequency = new HashMap<String, Double>();
    		inputfrequency.put("I", dffa.getInitialFrequencies().get("I"));
    	}
    	for(String state:subgraph.states)
    	{
    		double total=0;
    		for(String sym:subgraph.alphabet)
    		{
    			if(subgraph.getTransitionFunction().containsKey(state+sym))
    			{
    				String next=subgraph.getTransitionFunction().get(state+sym);
    				
    				
    				double val = dffa.transitionPercentage.get(state).get(sym).get(next);
    			//	if(next.compareTo("AEFCBDGHJDCBBCBBBBB")==0)
    			//	System.out.println(state+" "+sym+" "+next+" "+val);
    				total +=val;
    				subgraph.setTransitionPercentage(state, sym, next, val);
    				subgraph.setTransitionprobability(state, sym, state, new Fraction(val));
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
   	
    	 FPTA dffa2=DFFA.firstLevelPercentageConversion(subgraph);
    	 List<String> x =subgraph.extractEquations1(dffa2);
    	 Map<String,Double> y = CoefficientMatrix.findCoefficient(x);
    		 
    		 FPTA prunFPTA = subgraph.updateTransitionFrequency1(subgraph,y);
    		 prunFPTA.rebalancePercentages(prunFPTA);
    		// subgraph.showDFG(prunFPTA, "");
    	
    	
    	 for(String state:prunFPTA.states)
     	{
    		 double in = prunFPTA.calculateIncomingFrequencies(prunFPTA, state);
    	     double out = prunFPTA.calculateOutgoingFrequencies(prunFPTA, state);
    	     if(in>=out )
    	    	 prunFPTA.setFinalFrequency(state, in-out);
    	     else if(out-in<in/100)
    	     {
    	    	 prunFPTA.setFinalFrequency(state, 0);
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
    	 return prunFPTA;
    }
}
