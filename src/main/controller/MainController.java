package main.controller;

import main.GUI.*;
import main.Algorithms.*;
import main.graphStruct.Graph;
import main.HelperClass;

import javax.swing.*;
import java.io.File;

public class MainController {
    private AppWindow window;
    private Graph currentGraph;
    private AlgorithmController algoController;

    public MainController() {
        try {
            currentGraph = HelperClass.loadGraph("Graphs/g1.txt");
        } catch (Exception e) {
            System.out.println("Default graph not found. Starting with empty workspace.");
            currentGraph = null;
        }
        try {
            window = new AppWindow(currentGraph);

            Runnable initialAlgo = this::runFordFulkerson;
            algoController = new AlgorithmController(window.getPainter(), window.getInfoPanel(), window.getHeadPanel(), initialAlgo);

            ControlPanel controlPanel = new ControlPanel(algoController, window.getPainter());
            window.attachControlPanel(controlPanel);

            algoController.setControlPanel(controlPanel);
            algoController.setExplanationPanel(window.getExplanationPanel());

            setupActionListeners();

            window.setVisible(true);
            algoController.reset();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupActionListeners() {
        window.getHeadPanel().getLoadGraphBtn().addActionListener(e -> handleLoadGraph());
        window.getHeadPanel().getExportDotBtn().addActionListener(e -> handleExportGraph());

        window.getMenuPanel().getFordBtn().addActionListener(e ->
                switchAlgorithm("Ford-Fulkerson", false, this::runFordFulkerson));

        window.getMenuPanel().getBellmanBtn().addActionListener(e ->
                switchAlgorithm("MinCost: Bellman-Ford", false, this::runBellmanFord));

        window.getMenuPanel().getDijkstraBtn().addActionListener(e ->
                switchAlgorithm("MinCost: Dijkstra", true, this::runDijkstra));

        window.getMenuPanel().getFloydBtn().addActionListener(e ->
                switchAlgorithm("MinCost: Floyd-Warshall", false, this::runFloydWarshall));
    }

    private void switchAlgorithm(String algoName, boolean isDijkstraMode, Runnable solverLogic) {
        if (currentGraph == null) {
            JOptionPane.showMessageDialog(window, "Please load a graph first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        window.getHeadPanel().setAlgorithmName(algoName);
        algoController.setDijkstraMode(isDijkstraMode);

        Runnable newLauncher = () -> {
            currentGraph.resetAllFlows();
            currentGraph.clearVisuals();
            solverLogic.run();
        };

        algoController.setAlgoLauncher(newLauncher);
        algoController.reset();
    }

    private void runFordFulkerson() {
        if (currentGraph == null) return;
        Ford_Fulkerson solver = new Ford_Fulkerson(currentGraph);
        if (algoController != null) solver.setListener(algoController);
        solver.run();
    }

    private void runBellmanFord() {
        if (currentGraph == null) return;
        Ford_Fulkerson initialSolver = new Ford_Fulkerson(currentGraph);
        initialSolver.run();

        currentGraph.clearVisuals();

        BellmanFord solver = new BellmanFord(currentGraph);
        if (algoController != null) solver.setListener(algoController);
        solver.solve();
    }

    private void runDijkstra() {
        if (currentGraph == null) return;
        Dijkstra solver = new Dijkstra(currentGraph);
        if (algoController != null) solver.setListener(algoController);
        solver.solve();
    }

    private void runFloydWarshall() {
        if (currentGraph == null) return;
        Ford_Fulkerson initialSolver = new Ford_Fulkerson(currentGraph);
        initialSolver.run();

        currentGraph.clearVisuals();

        FloydWarshall solver = new FloydWarshall(currentGraph);
        if (algoController != null) solver.setListener(algoController);
        solver.solve();
    }

    private void handleLoadGraph() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setDialogTitle("Select a Graph file");

        if (fileChooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                currentGraph = HelperClass.loadGraph(selectedFile.getAbsolutePath());

                Layout newLayout = new Layout(currentGraph, 750, 600);
                window.getPainter().setGraphAndLayout(currentGraph, newLayout);

                algoController.reset();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(window, "Load error : " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportGraph() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setDialogTitle("Save as .dot file");

        if (fileChooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.endsWith(".dot")) {
                filePath += ".dot";
            }

            try {
                HelperClass.exportGraphToDot(currentGraph, filePath);
                System.out.println("Exported DOT file to: " + filePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(window, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}