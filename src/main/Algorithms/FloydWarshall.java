package main.Algorithms;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import main.graphStruct.Graph;
import main.graphStruct.Edge;

public class FloydWarshall extends MinCostFlowSolver {

    public FloydWarshall(Graph graph) {
        super(graph);
    }

    @Override
    public int solve() {
        int totalCost = graph.calculateCurrentTotalCost();

        notifyMajor("Floyd-Warshall Initialization",
                "Based on a MaxFlow. We will now search for negative cost cycles using the Floyd-Warshall All-Pairs shortest path matrix.",
                "-", "-", "-",false);

        while (true) {
            graph.clearVisuals();
            int n = graph.n;
            double[][] dist = new double[n][n];
            int[][] next = new int[n][n];
            int[][] bestEdge = new int[n][n];

            for (int i = 0; i < n; i++) {
                Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
                Arrays.fill(next[i], -1);
                Arrays.fill(bestEdge[i], -1);
                dist[i][i] = 0;
            }

            for (int u = 0; u < n; u++) {
                for (int iEdge = 0; iEdge < graph.adj[u].size(); iEdge++) {
                    Edge e = graph.adj[u].get(iEdge);
                    if (e.capacity - e.flow > 0) {
                        if (dist[u][e.dest_node] > e.cost) {
                            dist[u][e.dest_node] = e.cost;
                            next[u][e.dest_node] = e.dest_node;
                            bestEdge[u][e.dest_node] = iEdge;
                        }
                    }
                }
            }

            notifyMajor("Computing All-Pairs Paths",
                    "The algorithm iteratively checks if passing through an intermediate node 'k' offers a shorter path between nodes 'i' and 'j'.",
                    "-", "-", "-",false);

            for (int k = 0; k < n; k++) {
                graph.activeNode = k;
                String matrixString = formatMatrix(dist);
                notifyMinor("Pivot Node: " + k,
                        "Updating matrix entries using node " + k + " as a potential bridge.",
                        "-", "-", matrixString,false);

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (dist[i][k] != Double.POSITIVE_INFINITY &&
                                dist[k][j] != Double.POSITIVE_INFINITY &&
                                dist[i][k] + dist[k][j] < dist[i][j]) {

                            dist[i][j] = dist[i][k] + dist[k][j];
                            next[i][j] = next[i][k]; // Update path pointer
                        }
                    }
                }
            }

            notifyMinor("Final Diagonal Inspection",
                    "We check the diagonal (dist[i][i]). If any value is < 0, a negative cycle exists because a node found a 'shortcut' to itself.",
                    "-", "-", formatMatrix(dist),false);

            int cycleStartNode = -1;
            for (int i = 0; i < n; i++) {
                if (dist[i][i] < 0) {
                    cycleStartNode = i;
                    break;
                }
            }

            if (cycleStartNode == -1) {
                main.Algorithms.MinCutCalc.findMinCut(graph);
                notifyMajor("Optimization Finished",
                        "All diagonal values are >= 0. No negative cycles remain. Final Total Cost: $" + graph.calculateCurrentTotalCost(),
                        "-", "-", formatMatrix(dist),true);
                break;
            }

            int curr = cycleStartNode;
            List<Integer> pathNodes = new ArrayList<>();
            while (true) {
                pathNodes.add(curr);
                curr = next[curr][cycleStartNode];
                if (pathNodes.contains(curr)) {
                    cycleStartNode = curr;
                    break;
                }
            }

            List<Edge> cycleEdges = new ArrayList<>();
            List<Integer> cycleTrace = new ArrayList<>();
            curr = cycleStartNode;
            do {
                cycleTrace.add(curr);
                int nxt = next[curr][cycleStartNode];
                Edge e = graph.adj[curr].get(bestEdge[curr][nxt]);
                cycleEdges.add(e);
                curr = nxt;
            } while (curr != cycleStartNode);
            cycleTrace.add(cycleStartNode);

            int flow = Integer.MAX_VALUE;
            for (Edge e : cycleEdges) {
                flow = Math.min(flow, e.capacity - e.flow);
            }

            graph.clearVisuals();
            StringBuilder sb = new StringBuilder("Cycle: ");
            for (int i = 0; i < cycleTrace.size(); i++) {
                sb.append(cycleTrace.get(i)).append(i < cycleTrace.size() - 1 ? " -> " : "");
            }

            for (Edge e : cycleEdges) {
                e.isPath = true;
            }

            String cyclePathAndMatrix = sb.toString() + "\n\n" + formatMatrix(dist);

            notifyMajor("Negative Cycle Detected!",
                    "Node " + cycleStartNode + " has a diagonal < 0. We can reduce total cost by pushing " + flow + " units of flow.", String.valueOf(flow), "-", cyclePathAndMatrix,false);

            for (Edge e : cycleEdges) {

                notifyMinor("Re-routing Flow",
                        "Sending " + flow + " cost through " + e.source_node + " -> " + e.dest_node + ". Cost: " + e.cost + " per unit.",
                        "-", "-", cyclePathAndMatrix,false);
                e.isPath = false;
                e.flow += flow;
                graph.adj[e.dest_node].get(e.reverse).flow -= flow;
            }

            totalCost = graph.calculateCurrentTotalCost();
            notifyMajor("Cost Optimization Result",
                    "Flow has been re-routed. The new optimized Total Cost is: $" + totalCost,
                    "-", "-",cyclePathAndMatrix,false);
        }

        graph.activeNode = -1;
        return totalCost;
    }

    private String formatMatrix(double[][] dist) {
        StringBuilder sb = new StringBuilder();
        int n = dist.length;

        sb.append("      ");
        for (int j = 0; j < n; j++) sb.append(String.format("[%d]  ", j));
        sb.append("\n");

        for (int i = 0; i < n; i++) {
            sb.append(String.format("[%d] ", i));
            for (int j = 0; j < n; j++) {
                if (dist[i][j] == Double.POSITIVE_INFINITY) {
                    sb.append("  ∞  ");
                } else {
                    sb.append(String.format("%3.0f  ", dist[i][j]));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}