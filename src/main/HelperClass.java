package main;

import main.graphStruct.Edge;
import main.graphStruct.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class HelperClass {

    public static Graph loadGraph(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        int n = sc.nextInt();
        int m = sc.nextInt();
        int source = sc.nextInt();
        int sink = sc.nextInt();

        Graph graph = new Graph(n, source, sink);

        for (int i = 0; i < m; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            int cap = sc.nextInt();
            int cost = sc.nextInt();

            graph.addEdge(u, v, cap, cost);
        }

        sc.close();
        return graph;
    }

    public static void exportGraphToDot(Graph g, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("digraph FlowNetwork {\n");

            writer.write("  rankdir=LR;\n");
            writer.write("  node [shape=circle, style=filled, fillcolor=white, fontname=\"Arial\"];\n");
            writer.write("  edge [fontname=\"Arial\", fontsize=10];\n\n");

            writer.write(String.format("  %d [fillcolor=\"#a2d149\", shape=doublecircle];\n", g.source));
            writer.write(String.format("  %d [fillcolor=\"#f77669\", shape=doublecircle];\n\n", g.sink));

            for (int u = 0; u < g.n; u++) {
                for (Edge edge : g.adj[u]) {
                    if (edge.capacity > 0) {
                        String color = "black";
                        int penwidth = 1;

                        if (edge.flow == edge.capacity && edge.capacity > 0) {
                            color = "\"#d32f2f\"";
                            penwidth = 2;
                        } else if (edge.flow > 0) {
                            color = "\"#1976d2\"";
                            penwidth = 2;
                        }

                        writer.write(String.format("  %d -> %d [label=\" %d/%d (c:%d) \", color=%s, penwidth=%d];\n",
                                u, edge.dest_node, edge.flow, edge.capacity, edge.cost, color, penwidth));

                    } else if (edge.capacity == 0 && edge.flow < 0) {
                       int residualCapacity = -edge.flow;
                        writer.write(String.format("  %d -> %d [label=\" res:%d (c:%d) \", color=\"#9e9e9e\", style=\"dashed\"];\n",
                                u, edge.dest_node, residualCapacity, edge.cost));
                    }
                }
            }
            writer.write("}\n");
        }
    }
}
