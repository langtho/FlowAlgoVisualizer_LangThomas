package main.Algorithms;
import main.graphStruct.Edge;
import main.graphStruct.Graph;

import java.util.LinkedList;
import java.util.Queue;

public class MinCutCalc {
    public static void findMinCut(Graph graph) {
        for (int u = 0; u < graph.n; u++) {
            for (Edge e : graph.adj[u]) {
                e.isMinCut = false;
            }
        }
        boolean[] reachable = new boolean[graph.n];
        Queue<Integer> queue = new LinkedList<>();

        queue.add(graph.source);
        reachable[graph.source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge e : graph.adj[u]) {
                if (e.capacity - e.flow > 0 && !reachable[e.dest_node]) {
                    reachable[e.dest_node] = true;
                    queue.add(e.dest_node);
                }
            }
        }

        for (int u = 0; u < graph.n; u++) {
            if (reachable[u]) {
                for (Edge e : graph.adj[u]) {
                    if (e.capacity > 0 && !reachable[e.dest_node]) {
                        e.isMinCut = true;
                    }
                }
            }
        }
    }
}
