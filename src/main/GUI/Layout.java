package main.GUI;

import main.graphStruct.Edge;
import main.graphStruct.Graph;
import java.awt.Point;
import java.util.*;

public class Layout {
    public Point[] nodePositions;

    public Layout(Graph g, int width, int height) {
        nodePositions = new Point[g.n];


        int[] levels = new int[g.n];
        Arrays.fill(levels, -1);
        List<List<Integer>> layers = new ArrayList<>();

        Queue<Integer> queue = new LinkedList<>();
        queue.add(g.source);
        levels[g.source] = 0;

        int maxLevel = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            int currentLevel = levels[u];
            maxLevel = Math.max(maxLevel, currentLevel);

            while (layers.size() <= currentLevel) layers.add(new ArrayList<>());
            if (!layers.get(currentLevel).contains(u)) layers.get(currentLevel).add(u);

            for (Edge e : g.adj[u]) {
                // BFS ignores cycles and back-edges automatically!
                if (e.capacity > 0 && levels[e.dest_node] == -1) {
                    levels[e.dest_node] = currentLevel + 1;
                    queue.add(e.dest_node);
                }
            }
        }

        for (int i = 0; i < g.n; i++) {
            if (levels[i] == -1) {
                if (i == g.sink) {
                    levels[g.sink] = maxLevel + 1;
                    maxLevel++;
                } else {
                    levels[i] = maxLevel / 2;
                }
                while (layers.size() <= levels[i]) layers.add(new ArrayList<>());
                layers.get(levels[i]).add(i);
            }
        }
        layers.removeIf(List::isEmpty);

      for (int sweep = 0; sweep < 4; sweep++) {
            for (int l = 1; l < layers.size(); l++) {
                List<Integer> prevLayer = layers.get(l - 1);
                List<Integer> currLayer = layers.get(l);

                Map<Integer, Double> barycenter = new HashMap<>();
                for (int u : currLayer) {
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < prevLayer.size(); i++) {
                        int parent = prevLayer.get(i);
                        for (Edge e : g.adj[parent]) {
                            if (e.dest_node == u && e.capacity > 0) {
                                sum += i;
                                count++;
                            }
                        }
                    }
                    barycenter.put(u, count == 0 ? 0 : sum / count);
                }
                currLayer.sort(Comparator.comparingDouble(u -> barycenter.getOrDefault(u, 0.0)));
            }
        }


        int maxNodesInLayer = 0;
        for (List<Integer> layer : layers) {
            maxNodesInLayer = Math.max(maxNodesInLayer, layer.size());
        }

        int paddingLeft = 70;
        int paddingTop = 60;
        int paddingRight = 80;

        int activeWidth = Math.min(width - paddingLeft - paddingRight, Math.max(1, layers.size() - 1) * 220);
        double xStep = (double) activeWidth / Math.max(1, layers.size() - 1);
        int verticalGap = 130;

        for (int l = 0; l < layers.size(); l++) {
            List<Integer> nodesInLayer = layers.get(l);
            int numNodes = nodesInLayer.size();

            // Right-to-Left placement
            int x = paddingLeft + activeWidth - (int) (l * xStep);
            int centeringOffset = (maxNodesInLayer - numNodes) * verticalGap / 2;

            for (int i = 0; i < numNodes; i++) {
                int nodeId = nodesInLayer.get(i);
                int y = paddingTop + (i * verticalGap) + centeringOffset;
                nodePositions[nodeId] = new Point(x, y);
            }
        }
    }

    public Point getPosition(int id) {
        return nodePositions[id];
    }
}