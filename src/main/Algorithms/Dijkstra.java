package main.Algorithms;

import main.graphStruct.Edge;
import main.graphStruct.Graph;
import main.graphStruct.Node;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra extends MinCostFlowSolver {
    private int[] potential;

    public Dijkstra(Graph graph) {
        super(graph);
        this.potential = new int[graph.n];
    }

    public int solve() {
        int totalCost = 0;

        Arrays.fill(potential, 0);
        notifyMajor("Initialization: Node Potentials",
                "Dijkstra requires all edge costs to be non-negative. We initialize Node Potentials to 0.",
                "-", "-", "Potentials: " + Arrays.toString(potential));

        while (true) {
            graph.clearVisuals();
            int[] dist = new int[graph.n];
            int[] parent = new int[graph.n];
            int[] edgeIdx = new int[graph.n];

            notifyMajor("Searching for Cheapest Path",
                    "Running Dijkstra on the residual graph. To handle potential negative costs from back-edges, we use 'Reduced Costs' (c_new = c + p_u - p_v) to keep all values >= 0.",
                    "-", "-", "-");

            if (!runDijkstra(graph.source, graph.sink, dist, parent, edgeIdx)) {
                notifyMajor("Algorithm Finished",
                        "No more augmenting paths exist. The current flow is optimized for both volume and cost. Final Total Cost: $" + totalCost,
                        "-", "-", "FINAL MAX FLOW REACHED");
                break;
            }

            updatePotentials(dist);

            int flow = calculateBottleneck(parent, edgeIdx);

            // Highlight the final path before we augment the flow
            highlightFinalPath(graph.sink, parent, edgeIdx);
            String pathData = constructPathString(parent, graph.sink);

            notifyMajor("Cheapest Path Found!",
                    "We can send " + flow + " units through this path.",
                    String.valueOf(flow), "-", pathData + "\n\n" + formatDistancesAndPotentials(dist));

            // Use the local custom augment method to keep edges highlighted and show updates
            totalCost += augmentDijkstraFlow(parent, edgeIdx, flow, pathData + "\n\n" + formatDistancesAndPotentials(dist));

            notifyMajor("Flow Updated",
                    "The flow has been pushed. The residual graph is updated. Current Total Cost: $" + totalCost,
                    "-", "-", pathData + "\n\n" + formatDistancesAndPotentials(dist));
        }
        return totalCost;
    }

    private boolean runDijkstra(int s, int t, int[] dist, int[] parent, int[] edgeIdx) {
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[s] = 0;
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.d));
        pq.add(new Node(s, 0));

        while (!pq.isEmpty()) {
            Node top = pq.poll();

            if (top.d > dist[top.u]) continue;

            graph.activeNode = top.u;
            notifyMinor("Visiting Node " + top.u,
                    "This node is currently the closest unvisited node with a 'Reduced Distance' of " + top.d + ". We will inspect its neighbors.",
                    "-", "-", formatDistancesAndPotentials(dist));

            for (int i = 0; i < graph.adj[top.u].size(); i++) {
                Edge e = graph.adj[top.u].get(i);

                if (e.capacity - e.flow > 0) {
                    e.isExploring = true;

                    int reducedCost = e.cost + potential[top.u] - potential[e.dest_node];
                    e.reducedCost = reducedCost;
                    String mathFormula = "Reduced Cost = Real Cost(" + e.cost + ") + Pot(" + top.u + ") - Pot(" + e.dest_node + ") = " + reducedCost;

                    notifyMinor("Inspecting Edge " + top.u + " -> " + e.dest_node,
                            "Normalizing the cost: " + mathFormula + ". Because of our potentials, this result is guaranteed to be >= 0.",
                            "-", top.u + "->" + e.dest_node, formatDistancesAndPotentials(dist));

                    if (dist[e.dest_node] > dist[top.u] + reducedCost) {
                        int oldDist = dist[e.dest_node];
                        dist[e.dest_node] = dist[top.u] + reducedCost;
                        parent[e.dest_node] = top.u;
                        edgeIdx[e.dest_node] = i;
                        pq.add(new Node(e.dest_node, dist[e.dest_node]));

                        String currentPath = highlightAndFormatPath(e.dest_node, parent, edgeIdx, top.u, e);
                        String oldDistStr = (oldDist == Integer.MAX_VALUE) ? "INF" : String.valueOf(oldDist);
                        String mathExplanation = oldDistStr + " > " + dist[top.u] + " + (" + reducedCost + ")";

                        notifyMinor("Cheaper Route Found",
                                "Node " + e.dest_node + " can be reached more economically via Node " + top.u + ".\n" +
                                        "Math: " + mathExplanation + "\n" +
                                        "Updated total reduced distance: " + dist[e.dest_node],
                                "-", top.u + "->" + e.dest_node,
                                "Path: " + currentPath + "\n\n" + formatDistancesAndPotentials(dist));
                    } else {
                        notifyMinor("Path Ignored",
                                "The existing path to Node " + e.dest_node + " is cheaper than going through Node " + top.u + ".",
                                "-", "-", formatDistancesAndPotentials(dist));
                    }

                    graph.clearVisuals();
                    graph.activeNode = top.u;
                }
            }
        }
        graph.activeNode = -1;
        return dist[t] != Integer.MAX_VALUE;
    }

    private void updatePotentials(int[] dist) {
        for (int i = 0; i < graph.n; i++) {
            if (dist[i] != Integer.MAX_VALUE) {
                potential[i] += dist[i];
            }
        }
        notifyMinor("Updating Node Potentials",
                "We add the calculated shortest path distances to the current node potentials. This re-levels the network altitudes, ensuring that all costs remain non-negative for the next iteration.",
                "-", "-", "New Potentials:\n" + Arrays.toString(potential));
    }


    private int augmentDijkstraFlow(int[] parent, int[] edgeIdx, int flow, String displayData) {
        int pathCost = 0;
        int curr = graph.sink;
        while (curr != graph.source) {
            int p = parent[curr];
            Edge e = graph.adj[p].get(edgeIdx[curr]);

            notifyMinor("Re-routing Flow",
                    "Sending " + flow + " units through " + p + " -> " + curr + ". Real cost: " + e.cost + " per unit.",
                    "-", "-", displayData);

            e.isPath = false;

            e.flow += flow;
            graph.adj[curr].get(e.reverse).flow -= flow;
            pathCost += flow * e.cost;

            curr = p;
        }
        return pathCost;
    }

    private String highlightAndFormatPath(int endNode, int[] parent, int[] edgeIdx, int activeNode, Edge currentEdge) {
        graph.clearVisuals();
        graph.activeNode = activeNode;
        currentEdge.isExploring = true;

        java.util.LinkedList<Integer> path = new java.util.LinkedList<>();
        int curr = endNode;

        while (curr != -1) {
            path.addFirst(curr);
            int p = parent[curr];
            if (p != -1) {
                Edge e = graph.adj[p].get(edgeIdx[curr]);
                e.isPath = true; // Highlight edges leading up to this cost
            }
            if (curr == graph.source) break;
            curr = p;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : "");
        }
        return sb.toString();
    }

    private void highlightFinalPath(int target, int[] parent, int[] edgeIdx) {
        graph.clearVisuals();
        int curr = target;
        while (curr != graph.source && curr != -1) {
            int p = parent[curr];
            if (p != -1) {
                Edge e = graph.adj[p].get(edgeIdx[curr]);
                e.isPath = true;
            }
            curr = p;
        }
    }

    private String formatDistancesAndPotentials(int[] dist) {
        StringBuilder sb = new StringBuilder("Distance & Potential Table:\n");
        sb.append("------------------------------------\n");
        for (int i = 0; i < dist.length; i++) {
            String d = (dist[i] == Integer.MAX_VALUE) ? "INF" : String.valueOf(dist[i]);
            sb.append(String.format("Node %d : Dist = %s | Pot = %d\n", i, d, potential[i]));
        }
        return sb.toString();
    }

    private String constructPathString(int[] parent, int target) {
        if (parent[target] == -1 && target != graph.source) return "No path found";
        java.util.LinkedList<Integer> path = new java.util.LinkedList<>();
        int curr = target;
        while (curr != -1) {
            path.addFirst(curr);
            if (curr == graph.source) break;
            curr = parent[curr];
        }
        StringBuilder sb = new StringBuilder("Selected Path: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : "");
        }
        return sb.toString();
    }
}