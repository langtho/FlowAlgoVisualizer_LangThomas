package main.Algorithms;

import main.controller.AlgorithmController;
import main.graphStruct.Edge;
import main.graphStruct.Graph;
import main.controller.StepListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Ford_Fulkerson {
    private Graph graph;
    private StepListener listener;

    public Ford_Fulkerson(Graph g){
        graph=g;
    }

    public void setListener(StepListener l) { this.listener = l; }

    public int run() {
        int maxFlow = 0;
        int pathCount = 0;
        int[] parent = new int[graph.n];
        int[] edgeFrom = new int[graph.n];

        // INITIAL STEP
        notifyMajor("Algorithm Start",
                "The goal is to push as much volume as possible from Source to Sink. We will look for Augmenting Paths.",
                maxFlow, "-", "-", "-", false);

        while (bfs(parent, edgeFrom, maxFlow)) {
            pathCount++;

            String pathStr = "";
            int pathFlow = Integer.MAX_VALUE;
            StringBuilder allEdgesInfo = new StringBuilder();

            for (int v = graph.sink; v != graph.source; v = parent[v]) {
                int u = parent[v];
                Edge e = graph.adj[u].get(edgeFrom[v]);
                e.isPath = true;

                int residual = e.capacity - e.flow;
                if (residual < pathFlow) {
                    pathFlow = residual;
                }
                allEdgesInfo.insert(0, u + "->" + v + " (" + residual + "); ");
            }

            notifyMajor("Augmenting Path Found",
                    "Path #" + pathCount + " identified. The bottleneck is " + pathFlow ,
                    maxFlow, String.valueOf(pathFlow), "", allEdgesInfo.toString(), false);

            // AUGMENT FLOW
            for (int v = graph.sink; v != graph.source; v = parent[v]) {
                int u = parent[v];
                int idx = edgeFrom[v];
                Edge e = graph.adj[u].get(idx);
                int revIdx = e.reverse;

                e.flow += pathFlow;
                graph.adj[v].get(revIdx).flow -= pathFlow; // Update residual back-edge
                e.isPath = false;

                notifyMinor("Updating Flow",
                        maxFlow, String.valueOf(pathFlow),  "", u + "->" + v , false);
            }

            maxFlow += pathFlow;
            graph.clearVisuals();
            notifyMinor("Path Processed",
                    maxFlow, "-", "-",  "" , false);
        }

        notifyMajor("No More Paths",
                "BFS can no longer reach the Sink. Current flow is the Maximum Flow possible.",
                maxFlow, "MAXED", "N/A", "N/A", false);

        MinCutCalc.findMinCut(graph);
        StringBuilder minCutEdgesInfo = new StringBuilder();
        for (int u = 0; u < graph.n; u++) {
            for (Edge e : graph.adj[u]) {
                if (e.isMinCut) {
                    minCutEdgesInfo.append(u).append("->").append(e.dest_node).append(" ");
                }
            }
        }

        String finalEdges = minCutEdgesInfo.length() > 0 ? minCutEdgesInfo.toString() : "-";
        notifyMajor("Min-Cut Identified",
                "The Min-Cut consists of the saturated edges that block any flow.",
                maxFlow, "MIN-CUT", finalEdges, "N/A", true);

        return maxFlow;
    }

    private boolean bfs(int[] parent, int[] edgeFrom, int currentMaxFlow) {
        graph.clearVisuals();
        int[] nodeBottleneck = new int[graph.n];
        String[] limitingEdge = new String[graph.n];
        Arrays.fill(nodeBottleneck, Integer.MAX_VALUE);
        Arrays.fill(limitingEdge, "-");

        notifyMajor("BFS Path Discovery",
                "Exploring the residual graph to find the shortest path.",
                currentMaxFlow, "-", "-", "-", false);

        Arrays.fill(parent, -1);
        Queue<Integer> queue = new LinkedList<>();

        queue.add(graph.source);
        parent[graph.source] = graph.source;
        graph.visitedNodes[graph.source] = true;

        while (!queue.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) return false;

            int u = queue.poll();
            graph.activeNode = u;

            String pathSoFar = constructPathString(parent, u);
            notifyMinor("Visiting Node " + u,
                    currentMaxFlow, (u == graph.source ? "-" : String.valueOf(nodeBottleneck[u])), limitingEdge[u],  pathSoFar, false);

            for (int i = 0; i < graph.adj[u].size(); i++) {
                if (Thread.currentThread().isInterrupted()) return false;

                Edge e = graph.adj[u].get(i);
                int residual = e.capacity - e.flow;

                if (residual > 0 && parent[e.dest_node] == -1) {
                    e.isExploring = true;

                    int potentialBottleneck = Math.min(nodeBottleneck[u], residual);
                    String potentialEdge = (residual < nodeBottleneck[u]) ? (u + "->" + e.dest_node) : limitingEdge[u];

                    notifyMinor("Checking Edge " + u + "->" + e.dest_node,
                            currentMaxFlow, String.valueOf(potentialBottleneck), potentialEdge, pathSoFar + " -> " + e.dest_node, false);

                    parent[e.dest_node] = u;
                    edgeFrom[e.dest_node] = i;
                    nodeBottleneck[e.dest_node] = potentialBottleneck;
                    limitingEdge[e.dest_node] = potentialEdge;
                    graph.visitedNodes[e.dest_node] = true;

                    if (e.dest_node == graph.sink) {
                        e.isExploring = false;
                        return true;
                    }
                    queue.add(e.dest_node);
                    e.isExploring = false;
                }
            }
        }
        return false;
    }

    private String constructPathString(int[] parent, int target) {
        if (target == graph.source) return String.valueOf(graph.source);
        if (parent[target] == -1) return "? -> " + target;
        LinkedList<Integer> path = new LinkedList<>();
        int curr = target;
        while (curr != graph.source && curr != -1) {
            path.addFirst(curr);
            curr = parent[curr];
            if (curr == graph.source) path.addFirst(graph.source);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : "");
        }
        return sb.toString();
    }

    private void notifyMajor(String title, String sub, int flow, String bottle, String bottleEdge, String path, boolean mincutReady) {
        if (listener != null) {
            listener.onStep(title, true);
            AlgorithmController ctrl = (AlgorithmController)listener;
            ctrl.updateVisuals(title, sub, flow, bottle, bottleEdge, path, mincutReady);
        }
    }

    private void notifyMinor(String sub, int flow, String bottle, String bottleEdge, String path, boolean mincutReady) {
        if (listener != null) {
            listener.onStep(sub, false);
            AlgorithmController ctrl = (AlgorithmController)listener;
            ctrl.updateVisuals(null, sub, flow, bottle, bottleEdge, path, mincutReady);
        }
    }
}