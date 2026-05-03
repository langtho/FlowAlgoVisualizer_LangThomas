package main.graphStruct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Graph {
    public List<Edge>[] adj;
    public int n, source, sink;
    public int activeNode = -1;
    public boolean[] visitedNodes;

    public Graph(int n, int src, int snk){
        this.n=n;
        source=src;
        sink=snk;
        adj = new ArrayList[n];
        for (int i = 0; i<n; i++) adj[i] =  new ArrayList<>();
    }

    public static class GraphState {
        public int[] flows;
        public boolean[] isExploring;
        public boolean[] isPath;
        public boolean[] isMinCut;
        public int activeNode;
        public boolean[] visitedNodes;
    }

    public void addEdge(int from, int to, int cap, int cost){
        adj[from].add(new Edge(to,from,cap,cost,adj[to].size()));
        adj[to].add(new Edge(from,source, 0, -cost, adj[from].size() - 1));
    }

    public void showAdjList(){
        for(int x=0;x<n;x++){
            System.out.print("Node " + x + " : ");
            for (int y=0;y<adj[x].size();y++){
                System.out.print("| "+adj[x].get(y).printEdge()+" ");
            }
            System.out.println("|");
        }
    }

    public void resetAllFlows() {
        for (int i = 0; i < n; i++) {
            for (Edge e : adj[i]) {
                e.flow = 0;
            }
        }
    }

    public int[] captureFlows() {
        int totalEdges = 0;
        if (adj != null) {
            for (int i = 0; i < n; i++) {
                if (adj[i] != null) {
                    totalEdges += adj[i].size();
                }
            }
        }

        int[] flows = new int[totalEdges];
        int index = 0;
        if (adj != null) {
            for (int i = 0; i < n; i++) {
                if (adj[i] != null) {
                    for (Edge e : adj[i]) {
                        flows[index++] = e.flow;
                    }
                }
            }
        }
        return flows;
    }

    public void applyFlows(int[] flows) {
        if (flows == null || adj == null) return;

        int index = 0;
        for (int i = 0; i < n; i++) {
            if (adj[i] != null) {
                for (Edge e : adj[i]) {
                    if (index < flows.length) {
                        e.flow = flows[index++];
                    }
                }
            }
        }
    }

    public void clearVisuals() {
        this.activeNode = -1;
        if (this.visitedNodes != null) {
            java.util.Arrays.fill(this.visitedNodes, false);
        } else {
            this.visitedNodes = new boolean[n];
        }
        for (int i = 0; i < n; i++) {
            for (Edge e : adj[i]) {
                e.isExploring = false;
                e.isPath = false;
                e.isMinCut = false;
            }
        }
    }

    public GraphState captureState() {
        GraphState state = new GraphState();
        int totalEdges = 0;
        if (adj != null) {
            for (int i = 0; i < n; i++) if (adj[i] != null) totalEdges += adj[i].size();
        }

        state.flows = new int[totalEdges];
        state.isExploring = new boolean[totalEdges];
        state.isPath = new boolean[totalEdges];
        state.isMinCut = new boolean[totalEdges];

        int index = 0;
        if (adj != null) {
            for (int i = 0; i < n; i++) {
                if (adj[i] != null) {
                    for (Edge e : adj[i]) {
                        state.flows[index] = e.flow;
                        state.isExploring[index] = e.isExploring;
                        state.isPath[index] = e.isPath;
                        state.isMinCut[index] = e.isMinCut;
                        index++;
                    }
                }
            }
        }
        state.activeNode = this.activeNode;
        if (this.visitedNodes != null) state.visitedNodes = this.visitedNodes.clone();
        else state.visitedNodes = new boolean[n];

        return state;
    }

    public void applyState(GraphState state) {
        if (state == null || adj == null) return;

        int index = 0;
        for (int i = 0; i < n; i++) {
            if (adj[i] != null) {
                for (Edge e : adj[i]) {
                    if (index < state.flows.length) {
                        e.flow = state.flows[index];
                        e.isExploring = state.isExploring[index];
                        e.isPath = state.isPath[index];
                        e.isMinCut = state.isMinCut[index];
                        index++;
                    }
                }
            }
        }
        this.activeNode = state.activeNode;
        if (state.visitedNodes != null) this.visitedNodes = state.visitedNodes.clone();
    }

    public int calculateCurrentTotalFlow() {
        int totalFlow = 0;
        if (adj != null && adj[source] != null) {
            for (Edge e : adj[source]) {
                if (e.capacity > 0) {
                    totalFlow += e.flow;
                }
            }
        }
        return totalFlow;
    }

    public int calculateCurrentTotalCost() {
        int totalCost = 0;
        if (adj != null) {
            for (int i = 0; i < n; i++) {
                if (adj[i] != null) {
                    for (Edge e : adj[i]) {
                        if (e.capacity > 0) {
                            totalCost += (e.flow * e.cost);
                        }
                    }
                }
            }
        }
        return totalCost;
    }
}
