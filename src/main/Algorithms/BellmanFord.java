package main.Algorithms;

import main.graphStruct.Edge;
import main.graphStruct.Graph;
import java.util.Arrays;

public class BellmanFord extends MinCostFlowSolver {

    public BellmanFord(Graph graph) {
        super(graph);
    }

    public int solve() {
        int totalCost = graph.calculateCurrentTotalCost();

        notifyMajor("Cycle-Canceling Initialization",
                "Based on a MaxFlow (FordFulkerson). We will now search for negative cost cycles to reduce the total cost.",
                "-", "-", "-");

        while (true) {
            graph.clearVisuals();
            int[] parent = new int[graph.n];
            int[] edgeIdx = new int[graph.n];

            notifyMajor("Searching for Negative Cycles",
                    "Looking for cycles where the sum of costs is less than zero (cost < 0).",
                    "-", "-", "-");

            int cycleStartNode = findNegativeCycle(parent, edgeIdx);

            if (cycleStartNode == -1) {
                notifyMajor("Optimization Finished",
                        "No negative cycles remain in the residual graph. Final Total Cost: $" + graph.calculateCurrentTotalCost(),
                        "-", "-", "-");
                break;
            }

            int flow = calculateCycleBottleneck(cycleStartNode, parent, edgeIdx);
            graph.clearVisuals();
            String cycleData = formatAndHighlightDetectedCycle(cycleStartNode, parent, edgeIdx);
            notifyMajor("Negative Cycle Detected!",
                    "A negative cycle found. Pushing " + flow + " units.",
                    String.valueOf(flow), "-", cycleData);

            augmentCycleFlow(cycleStartNode, parent, edgeIdx, flow);
            totalCost = graph.calculateCurrentTotalCost();

            notifyMajor("Cost Optimization Result",
                    "Flow has been re-routed. The new optimized Total Cost is: $" + totalCost,
                    "-", "-", "-");
        }
        return totalCost;
    }

    private int findNegativeCycle(int[] parent, int[] edgeIdx) {
        int n = graph.n;
        int[] dist = new int[n];
        Arrays.fill(dist, 0);
        Arrays.fill(parent, -1);

        int lastUpdatedNode = -1;

        for (int i = 0; i < n; i++) {
            boolean changed = false;

            String loopExplanation = (i == n - 1)
                    ? "Pass " + (i + 1) + " of " + n + ": Final pass to check for Negative Cycles. If any cost improves now, a cycle exists!"
                    : "Pass " + (i + 1) + " of " + n + ": Relaxing all edges. A simple path can have at most " + (n - 1) + " edges.";

            notifyMinor("Relaxation Pass " + (i + 1),
                    loopExplanation,
                    "-", "-",formatDistances(dist));

            for (int u = 0; u < n; u++) {
                graph.activeNode = u;
                for (int iEdge = 0; iEdge < graph.adj[u].size(); iEdge++) {
                    Edge e = graph.adj[u].get(iEdge);

                    if (e.capacity - e.flow > 0) {
                        e.isExploring = true;

                        if (dist[e.dest_node] > dist[u] + e.cost) {
                            int oldDist = dist[e.dest_node];
                            dist[e.dest_node] = dist[u] + e.cost;
                            parent[e.dest_node] = u;
                            edgeIdx[e.dest_node] = iEdge;
                            changed = true;

                            String currentPath = highlightAndFormatPath(e.dest_node, parent, edgeIdx, u, e);
                            String mathExplanation = (oldDist == 0 && i == 0) ? "(Initial)" : oldDist + " > " + dist[u] + " + (" + e.cost + ")";

                            notifyMinor("Cost Improvement Found!",
                                    "Cheaper path to Node " + e.dest_node + " found via Node " + u + ".\n" +
                                            "Math: " + mathExplanation + "\n" +
                                            "New relative cost: " + dist[e.dest_node],
                                    "-", u + "->" + e.dest_node,
                                    "Path: " + currentPath + "\n\n" + formatDistances(dist));

                            if (i == n - 1) {
                                lastUpdatedNode = e.dest_node;
                            }
                        }
                        graph.clearVisuals();
                        graph.activeNode = u;
                        //e.isExploring = false;
                    }
                }
            }
            if (!changed) break;
        }

        graph.activeNode = -1;
        if (lastUpdatedNode == -1) return -1;

        int cycleNode = lastUpdatedNode;
        for (int i = 0; i < n; i++) cycleNode = parent[cycleNode];
        return cycleNode;
    }

    private int calculateCycleBottleneck(int cycleNode, int[] parent, int[] edgeIdx) {
        int flow = Integer.MAX_VALUE;
        int curr = cycleNode;
        do {
            int p = parent[curr];
            Edge e = graph.adj[p].get(edgeIdx[curr]);
            flow = Math.min(flow, e.capacity - e.flow);
            curr = p;
        } while (curr != cycleNode);
        return flow;
    }

    private void augmentCycleFlow(int cycleNode, int[] parent, int[] edgeIdx, int flow) {
        int curr = cycleNode;
        do {
            int p = parent[curr];
            Edge e = graph.adj[p].get(edgeIdx[curr]);

            notifyMinor("Re-routing Flow",
                    "Sending " + flow + " cost through " + p + " -> " + curr + ". This edge has a cost of " + e.cost + " per unit.",
                    "-", "-","");

            e.flow += flow;
            graph.adj[curr].get(e.reverse).flow -= flow;
            e.isPath = false;
            curr = p;
        } while (curr != cycleNode);
    }

    private String formatDistances(int[] dist) {
        StringBuilder sb = new StringBuilder("Current Distance Table (Bellman-Ford):\n");
        sb.append("------------------------------------------\n");
        for (int i = 0; i < dist.length; i++) {
            sb.append(String.format("Node %d : Relative Cost = %d\n", i, dist[i]));
        }
        return sb.toString();
    }

    private String highlightAndFormatPath(int endNode, int[] parent, int[] edgeIdx, int activeNode, Edge currentEdge) {
        graph.clearVisuals();
        graph.activeNode = activeNode;
        currentEdge.isExploring = true;

        java.util.LinkedList<Integer> path = new java.util.LinkedList<>();
        int curr = endNode;
        int safetyLimit = 0;

        while (curr != -1 && safetyLimit <= graph.n) {
            path.addFirst(curr);
            int p = parent[curr];
            if (p != -1 && safetyLimit < graph.n) {
                Edge e = graph.adj[p].get(edgeIdx[curr]);
                e.isPath = true; // Highlights the edges leading up to this cost
            }
            curr = p;
            safetyLimit++;
        }

        if (safetyLimit > graph.n) {
            return "Cycle forming...";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i)).append(i < path.size() - 1 ? " → " : "");
        }
        return sb.toString();
    }

    private String formatAndHighlightDetectedCycle(int cycleNode, int[] parent, int[] edgeIdx) {
        java.util.LinkedList<Integer> cycle = new java.util.LinkedList<>();
        int curr = cycleNode;
        do {
            cycle.addFirst(curr);
            int p = parent[curr];
            Edge e = graph.adj[p].get(edgeIdx[curr]);
            e.isPath = true;
            curr = p;
        } while (curr != cycleNode);
        cycle.addLast(cycleNode);

        StringBuilder sb = new StringBuilder("Cycle: ");
        for (int i = 0; i < cycle.size(); i++) {
            sb.append(cycle.get(i)).append(i < cycle.size() - 1 ? " → " : "");
        }
        return sb.toString();
    }


}