package main.GUI;

import main.graphStruct.Edge;
import main.graphStruct.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;

public class GraphPainter extends JPanel {
    private Graph graph;
    private Layout layout;
    private static final int NODE_RADIUS = 20;
    private static final int ARROW_SIZE = 10;
    private boolean hideMinCutEdges = false;
    private boolean stepAllowsMinCut = false;
    private boolean dijkstraMode = false;

    private java.util.List<Runnable> labelDrawers = new java.util.ArrayList<>();

    public GraphPainter(Graph graph, Layout layout) {
        this.graph = graph;
        this.layout = layout;
        this.setBackground(Color.WHITE);
    }

    public void setHideMinCutEdges(boolean hide) {
        this.hideMinCutEdges = hide;
        this.repaint();
    }

    public void setStepAllowsMinCut(boolean allowed) {
        this.stepAllowsMinCut = allowed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null || layout == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        labelDrawers.clear();

        java.util.Map<String, Integer> totalVisibleEdges = new java.util.HashMap<>();
        java.util.Map<String, Integer> drawnEdgesCount = new java.util.HashMap<>();

        for (int u = 0; u < graph.n; u++) {
            for (Edge e : graph.adj[u]) {
                if (isVisible(e)) {
                    int min = Math.min(u, e.dest_node);
                    int max = Math.max(u, e.dest_node);
                    String key = min + "-" + max;
                    totalVisibleEdges.put(key, totalVisibleEdges.getOrDefault(key, 0) + 1);
                }
            }
        }

        for (int u = 0; u < graph.n; u++) {
            for (Edge e : graph.adj[u]) {
                drawOrientedEdge(g2, u, e.dest_node, e, totalVisibleEdges, drawnEdgesCount);
            }
        }

        for (int i = 0; i < graph.n; i++) {
            drawNode(g2, i, layout.nodePositions[i]);
        }

        for (Runnable drawLabel : labelDrawers) {
            drawLabel.run();
        }
    }

    private void drawOrientedEdge(Graphics2D g2, int u, int v, Edge e,
                                  java.util.Map<String, Integer> totalEdges,
                                  java.util.Map<String, Integer> drawnEdges){
        if(!isVisible(e)) return;

        Point c1 = layout.nodePositions[u];
        Point c2 = layout.nodePositions[v];

        double dx = c2.x - c1.x;
        double dy = c2.y - c1.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        double ux = dx / len;
        double uy = dy / len;

        double startX = c1.x + ux * NODE_RADIUS;
        double startY = c1.y + uy * NODE_RADIUS;
        double endX = c2.x - ux * NODE_RADIUS;
        double endY = c2.y - uy * NODE_RADIUS;

        int minNode = Math.min(u, v);
        int maxNode = Math.max(u, v);
        String pairKey = minNode + "-" + maxNode;

        int total = totalEdges.getOrDefault(pairKey, 1);
        double curvature = 0.0;


        int count = drawnEdges.getOrDefault(pairKey, 0);
        drawnEdges.put(pairKey, count + 1);

        double baseCurvature = 35.0 + (count / 2) * 70.0;
        double sign = (count % 2 == 0) ? 1.0 : -1.0;
        if (u > v) sign *= -1.0;

        curvature = baseCurvature * sign;


        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;

        double ctrlX = midX - curvature * uy;
        double ctrlY = midY + curvature * ux;

        QuadCurve2D.Float curve = new QuadCurve2D.Float((float)startX, (float)startY, (float)ctrlX, (float)ctrlY, (float)endX, (float)endY);

        Color color = Color.BLACK;
        if (e.isExploring) {
            color = Color.ORANGE;
            g2.setStroke(new BasicStroke(4));
        } else if (e.isPath) {
            color = new Color(0, 150, 255);
            g2.setStroke(new BasicStroke(4));
        } else {
            if (e.capacity > 0) {
                if (e.flow == e.capacity) color = Color.RED;
                else if (e.flow > 0) color = new Color(0, 150, 0);
                g2.setStroke(new BasicStroke(2));
            } else {
                color = Color.GRAY;
                float[] dashPattern = {6.0f, 4.0f};
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
            }
        }

        g2.setColor(color);
        g2.draw(curve);

        double arrowAngle = Math.atan2(endY - ctrlY, endX - ctrlX);
        drawArrow(g2, (int)endX, (int)endY, arrowAngle, color);

        double actualMidX = 0.25 * startX + 0.5 * ctrlX + 0.25 * endX;
        double actualMidY = 0.25 * startY + 0.5 * ctrlY + 0.25 * endY;

        int labelCenterX = (int) actualMidX;
        int labelCenterY = (int) actualMidY;



        final int finalCenterX = labelCenterX;
        final int finalCenterY = labelCenterY;

        final String flowCapLabel = (e.capacity > 0) ? (e.flow + "/" + e.capacity) : ""+(e.capacity - e.flow);

        String tempCost = "c: " + e.cost;
        if (dijkstraMode) {
            tempCost += " -> " + e.reducedCost ;
        }
        final String costLabel = tempCost;

        // Deferred rendering so labels are always on top
        labelDrawers.add(() -> {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();

            int flowCapWidth = fm.stringWidth(flowCapLabel);
            int costWidth = fm.stringWidth(costLabel);
            int maxWidth = Math.max(flowCapWidth, costWidth);
            int textHeight = fm.getHeight();

            int paddingX = 6;
            int paddingY = 4;
            int boxWidth = maxWidth + paddingX * 2;
            int boxHeight = (textHeight * 2) + paddingY;

            int boxX = finalCenterX - boxWidth / 2;
            int boxY = finalCenterY - boxHeight / 2;

            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

            g2.setColor(new Color(180, 180, 180, 150));
            g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

            g2.setColor(Color.BLACK);

            int costX = finalCenterX - costWidth / 2;
            int costY = boxY + paddingY + fm.getAscent() - 2;

            int flowCapX = finalCenterX - flowCapWidth / 2;
            int flowCapY = costY + textHeight;

            g2.drawString(costLabel, costX, costY);
            g2.drawString(flowCapLabel, flowCapX, flowCapY);
        });
    }

    private void drawArrow(Graphics2D g2, int x, int y, double angle, Color color) {
        AffineTransform tx = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle);
        g2.setColor(color);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-ARROW_SIZE, -ARROW_SIZE / 2);
        arrowHead.addPoint(-ARROW_SIZE, ARROW_SIZE / 2);
        g2.fill(arrowHead);

        g2.setTransform(tx);
    }

    private void drawNode(Graphics2D g2, int id, Point p) {
        Color nodeColor = new Color(200, 200, 200);
        if (id == graph.activeNode) {
            nodeColor = Color.YELLOW;
        }
        else if (graph.visitedNodes != null && graph.visitedNodes[id]) {
            nodeColor = new Color(173, 216, 230);
        }

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(p.x - NODE_RADIUS + 2, p.y - NODE_RADIUS + 2, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        g2.setColor(nodeColor);
        g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        String idStr = String.valueOf(id);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textX = p.x - fm.stringWidth(idStr) / 2;
        int textY = p.y + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(idStr, textX, textY);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(Color.DARK_GRAY);

        if (id == graph.source) {
            String label = "SOURCE";
            g2.drawString(label, p.x + NODE_RADIUS + 8, p.y + (fm.getAscent() / 2));
        } else if (id == graph.sink) {
            String label = "SINK";
            int labelWidth = fm.stringWidth(label);
            g2.drawString(label, p.x - NODE_RADIUS - labelWidth - 8, p.y + (fm.getAscent() / 2));
        }
    }

    public Graph getGraph() {
        return this.graph;
    }

    public void setGraphAndLayout(Graph g, Layout l) {
        this.graph = g;
        this.layout = l;
        this.repaint();
    }

    public void setDijkstraMode(boolean enabled) {
        this.dijkstraMode = enabled;
    }

    private boolean isVisible(Edge e) {
        if (stepAllowsMinCut && hideMinCutEdges && e.isMinCut) {
            return false;
        }
        if (e.capacity <= 0 && (e.capacity - e.flow) <= 0) {
            return false;
        }
        return true;
    }
}