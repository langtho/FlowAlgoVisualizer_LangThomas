package main.controller;

import main.GUI.*;

import javax.swing.*;

public class AlgorithmController implements StepListener {
    private final HistoryManager history = new HistoryManager();
    private final ExecEngine engine = new ExecEngine();

    private final GraphPainter painter;
    private final InfoPanel infoPanel;
    private final HeadPanel headPanel;
    private  ControlPanel controlPanel;
    private ExplanationPanel explanationPanel;

    private Runnable algoLauncher;
    private Thread algoThread;
    private javax.swing.Timer playbackTimer;

    private String lastTitle = "Ready";
    private String lastSub = "...";
    private boolean lastIsMajor = false;
    private boolean skipToMajor = false;

    public AlgorithmController(GraphPainter painter, InfoPanel infoPanel, HeadPanel headPanel, Runnable algoLauncher ) {
        this.painter = painter;
        this.infoPanel = infoPanel;
        this.headPanel = headPanel;
        this.algoLauncher = algoLauncher;
    }

    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }
    public void setExplanationPanel(ExplanationPanel ep) { this.explanationPanel = ep; }

    @Override
    public synchronized void onStep(String message, boolean isMajor) {
        this.lastIsMajor = isMajor;
        if (isMajor) {
            this.lastTitle = message;
            this.lastSub = "";
        } else {
            this.lastSub = message;
        }
    }

    public void updateVisuals(String title, String sub, int flow, String bottle, String bottleEdge, String path,boolean minCutReady) {
        if (Thread.currentThread().isInterrupted()) return;
        StepSaveStruct snap;
        boolean wasAtFront;

        synchronized(this) {
            wasAtFront = (history.getIndex() == history.size() - 1) || history.size() == 0;
            snap = new StepSaveStruct(
                    painter.getGraph().captureState(),
                    title != null ? title : lastTitle,
                    sub != null ? sub : lastSub,
                    flow, bottle, bottleEdge, path, lastIsMajor, minCutReady
            );
            history.add(snap);

            if (skipToMajor && lastIsMajor) {
                skipToMajor = false;
                engine.pause();
            }
        }

        if (!skipToMajor) {
            updateUI(snap);
        }

        try {
            engine.waitIfNeeded();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateUI(StepSaveStruct s) {
        SwingUtilities.invokeLater(() -> {
            painter.getGraph().applyState(s.graphFlows());
            int realTimeFlow = painter.getGraph().calculateCurrentTotalFlow();
            int realTimeCost = painter.getGraph().calculateCurrentTotalCost();
            if (explanationPanel != null) {
                explanationPanel.updateText(s.title(), s.subtitle());
            }
            infoPanel.updateInfo(realTimeFlow, realTimeCost, s.bottleneckValue(), s.bottleneckEdge(), s.pathStr());            if (controlPanel != null) {
                controlPanel.setMinCutEnabled(s.isMinCutAvailable());
                painter.setStepAllowsMinCut(s.isMinCutAvailable());
                if (!s.isMinCutAvailable()) {
                    painter.setHideMinCutEdges(false);
                }
            }
            painter.repaint();
        });
    }

    public synchronized void stepBack() {
        if (playbackTimer != null) playbackTimer.stop();
        if (history.canStepBack()) {
            engine.pause();
            history.moveBack();
            updateUI(history.getCurrent());
        }
    }

    public synchronized void requestNextStep() {
        if (playbackTimer != null) playbackTimer.stop();
        if (history.canStepForward()) {
            history.moveForward();
            updateUI(history.getCurrent());
        } else {
            if (algoThread != null && algoThread.isAlive()) {
                engine.requestStep();
            }
        }
    }

    public synchronized void play() {
        if (history.canStepForward()) {
            if (playbackTimer == null) {
                playbackTimer = new Timer(engine.getDelay(), e -> {
                    if (history.canStepForward()) {
                        history.moveForward();
                        updateUI(history.getCurrent());
                    } else {
                        playbackTimer.stop();
                        if (algoThread != null && algoThread.isAlive()) {
                            engine.resume();
                        }
                    }
                });
            }
            playbackTimer.start();
        } else {
            if (algoThread != null && algoThread.isAlive()) {
                engine.resume();
            }
        }
    }

    public synchronized void pause() {
        if (playbackTimer != null && playbackTimer.isRunning()) {
            playbackTimer.stop();
        }
        engine.pause();
    }

    public synchronized void reset() {
        if (algoThread != null){
            try {
                algoThread.join(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            algoThread.interrupt();}

        history.clear();
        engine.reset();
        painter.getGraph().resetAllFlows();

        this.lastTitle = "Initialisation";
        this.lastSub = "Ready...";
        this.skipToMajor = false;
        SwingUtilities.invokeLater(() -> {
            infoPanel.updateInfo(0, 0,"-", "-", "-");
            painter.setStepAllowsMinCut(false);
            painter.setHideMinCutEdges(false);
            if (controlPanel != null) {
                controlPanel.setMinCutEnabled(false);
            }
            painter.repaint();
        });
        algoThread = new Thread(algoLauncher);
        algoThread.start();
    }

    public void setDelay(int d) {
        engine.setDelay(d,playbackTimer);
    }

    public boolean isPaused(){
        return engine.isPaused();
    }

    public synchronized void requestNextMajor() {
        if (playbackTimer != null) playbackTimer.stop();
        boolean foundInHistory = false;
        for (int i = history.getIndex() + 1; i < history.size(); i++) {
            if (history.get(i).isMajor()) {
                history.setIndex(i);
                updateUI(history.getCurrent());
                foundInHistory = true;
                break;
            }
        }

        if (!foundInHistory) {
            if (algoThread != null && algoThread.isAlive()) {
                if (history.canStepForward()) {
                    history.setIndex(history.size() - 1);
                    updateUI(history.getCurrent());
                }
                this.skipToMajor = true;
                engine.resume();
            }
        }
    }

    public void setAlgoLauncher(Runnable algoLauncher) {
        this.algoLauncher = algoLauncher;
    }

    public void updateExplanation(String title, String desc) {
        if (explanationPanel != null) {
            explanationPanel.updateText(title, desc);
        }
    }

    public void updateInfo(int flow, int cost, String bottle, String bottleEdge, String path) {
        if (infoPanel != null) {
            infoPanel.updateInfo(flow, cost, bottle, bottleEdge, path);
        }
    }

    public void setDijkstraMode(boolean enabled) {
        painter.setDijkstraMode(enabled);
        painter.repaint();
    }
}