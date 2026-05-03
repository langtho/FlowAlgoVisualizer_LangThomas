package main.GUI;

import main.graphStruct.Graph;
import javax.swing.*;
import java.awt.*;

public class AppWindow extends JFrame {
    private HeadPanel headPanel;
    private MenuPanel menuPanel;
    private ExplanationPanel explanationPanel;
    private InfoPanel infoPanel;
    private GraphPainter painter;

    public AppWindow(Graph initialGraph) {
        super("Min-Cost Flow Visualizer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        headPanel = new HeadPanel();
        menuPanel = new MenuPanel();
        explanationPanel = new ExplanationPanel();
        infoPanel = new InfoPanel();

        Layout layout = new Layout(initialGraph, 900, 800);
        painter = new GraphPainter(initialGraph, layout);

        JPanel northContainer = new JPanel(new BorderLayout());
        JPanel topGroup = new JPanel(new BorderLayout());
        topGroup.add(headPanel, BorderLayout.NORTH);
        topGroup.add(menuPanel, BorderLayout.SOUTH);
        northContainer.add(topGroup, BorderLayout.NORTH);
        northContainer.add(explanationPanel, BorderLayout.SOUTH);

        this.add(northContainer, BorderLayout.NORTH);
        this.add(painter, BorderLayout.CENTER);
        this.add(infoPanel, BorderLayout.EAST);

        this.setSize(1200, 850);
        this.setLocationRelativeTo(null);
    }

    public void attachControlPanel(ControlPanel cp) {
        this.add(cp, BorderLayout.SOUTH);
        this.revalidate();
        this.repaint();
    }

    public HeadPanel getHeadPanel() { return headPanel; }
    public MenuPanel getMenuPanel() { return menuPanel; }
    public ExplanationPanel getExplanationPanel() { return explanationPanel; }
    public InfoPanel getInfoPanel() { return infoPanel; }
    public GraphPainter getPainter() { return painter; }
}