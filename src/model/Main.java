package model;

import java.util.*;

class Main {
    static int[] parent;
    static int[] rank;

    static class Edge {
        int u, v, weight;
        Edge(int u, int v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }
    }

    static int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    static void union(int x, int y) {
        int xRoot = find(x), yRoot = find(y);
        if (rank[xRoot] < rank[yRoot]) {
            parent[xRoot] = yRoot;
        } else if (rank[xRoot] > rank[yRoot]) {
            parent[yRoot] = xRoot;
        } else {
            parent[yRoot] = xRoot;
            rank[xRoot] = rank[xRoot] + 1;
        }
    }

    static void iterativeCycleBreaking(List<Edge> edges, int n) {
        Collections.sort(edges, (a, b) -> a.weight - b.weight);
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
        for (Edge edge : edges) {
            int x = find(edge.u);
            int y = find(edge.v);
            if (x != y) {
                union(x, y);
                System.out.println("Edge: " + edge.u + " - " + edge.v + " Weight: " + edge.weight);
            }
        }
    }
    public static void main(String[] args) {
    	   List<Edge> edges = new ArrayList<>();
           edges.add(new Edge(0, 1, 10));
           edges.add(new Edge(0, 2, 6));
           edges.add(new Edge(0, 3, 5));
           edges.add(new Edge(1, 3, 15));
           edges.add(new Edge(2, 3, 4));
           iterativeCycleBreaking(edges, 4);
    }

}