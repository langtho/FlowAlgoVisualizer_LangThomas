package main.Algorithms;

import main.controller.AlgorithmController;
import main.controller.StepListener;
import main.graphStruct.Graph;
import main.graphStruct.Edge;

public abstract class MinCostFlowSolver {
    public Graph graph;
    protected StepListener listener;

    public MinCostFlowSolver(Graph g) {
        this.graph = g;
    }

    public void setListener(StepListener l) {
        this.listener = l;
    }

    public abstract int solve();

    public int calculateBottleneck(int[] parent, int[] edgeIdx) {
        int flow = Integer.MAX_VALUE;
        for (int v = graph.sink; v != graph.source; v = parent[v]) {
            int u = parent[v];
            Edge e = graph.adj[u].get(edgeIdx[v]);
            flow = Math.min(flow, e.capacity - e.flow);
        }
        notifyStep("Bottleneck calculated: " + flow, false);
        return flow;
    }

    public int augmentFlow(int[] parent, int[] edgeIdx, int flow) {
        int pathCost = 0;
        for (int v = graph.sink; v != graph.source; v = parent[v]) {
            int u = parent[v];
            Edge e = graph.adj[u].get(edgeIdx[v]);
            e.flow += flow;
            graph.adj[v].get(e.reverse).flow -= flow;
            pathCost += flow * e.cost;
            notifyStep("Updating flow on edge " + u + " -> " + v, false);
        }
        return pathCost;
    }

    protected void notifyStep(String message, boolean isMajor) {
        if (listener != null) {
            listener.onStep(message, isMajor);
        }
    }

    protected void notifyMajor(String title, String explanation, String bottle, String bottleEdge, String path, boolean minCutR ) {
        if (listener != null) {
            listener.onStep(title, true);

            int flow = (graph != null) ? graph.calculateCurrentTotalFlow() : 0;
            int cost = (graph != null) ? graph.calculateCurrentTotalCost() : 0;

            ((AlgorithmController)listener).updateExplanation(title, explanation);
            ((AlgorithmController)listener).updateInfo(flow, cost, bottle, bottleEdge, path);
            ((AlgorithmController)listener).updateVisuals(null, explanation, flow, bottle, bottleEdge, "-", minCutR);

        }
    }

    protected void notifyMinor(String subTitle, String explanation, String bottle, String bottleEdge,String path,boolean minCutR) {
        if (listener != null) {
            listener.onStep(subTitle, false);

            int flow = (graph != null) ? graph.calculateCurrentTotalFlow() : 0;
            int cost = (graph != null) ? graph.calculateCurrentTotalCost() : 0;

            ((AlgorithmController)listener).updateExplanation(subTitle, explanation);
            ((AlgorithmController)listener).updateInfo(flow, cost, bottle, bottleEdge, path);
            ((AlgorithmController)listener).updateVisuals(null, explanation, flow, bottle, bottleEdge, path, minCutR);
        }
    }
}