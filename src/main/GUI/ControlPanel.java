package main.GUI;

import main.controller.AlgorithmController;
import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    JButton minCutBtn;
    private GraphPainter painter;
    private boolean minCutModeActive = false;

    public ControlPanel(AlgorithmController controller, GraphPainter gp) {
        this.setLayout(new BorderLayout());
        painter=gp;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton playPause = new JButton(controller.isPaused() ? "Start / Resume" : "Pause");
        JButton next = new JButton("Next Step");
        JButton nextMajor = new JButton("Next Major Step");
        JButton backBtn = new JButton("Step Back");
        JButton resetBtn = new JButton("Reset");
         minCutBtn = new JButton("Show Min-Cut");
        minCutBtn.setEnabled(false);

        JSlider speed = new JSlider(0, 2000, 500);
        speed.setToolTipText("Adjust speed (ms)");


        resetBtn.addActionListener(e -> {
            controller.reset();
            playPause.setText("Start / Resume");
        });

        minCutBtn.addActionListener(e -> {
            minCutModeActive = !minCutModeActive;

            painter.setHideMinCutEdges(minCutModeActive);

            minCutBtn.setText(minCutModeActive ? "Restore Graph" : "Show Min-Cut");
        });
        buttonPanel.add(minCutBtn);

        backBtn.addActionListener(e -> {
            controller.stepBack();
            playPause.setText("Resume");
        });

        playPause.addActionListener(e -> {
            if (controller.isPaused()) {
                controller.play();
                playPause.setText("Pause");
            } else {
                controller.pause();
                playPause.setText("Resume");
            }
        });

        next.addActionListener(e -> {
            controller.requestNextStep();
            if (controller.isPaused()) {
                playPause.setText("Resume");
            }
        });

        nextMajor.addActionListener(e -> {
            controller.requestNextMajor();
            playPause.setText("Resume");
        });

        speed.addChangeListener(e -> {
            controller.setDelay(speed.getValue());
        });

        buttonPanel.add(new JLabel("Controls:"));
        buttonPanel.add(playPause);
        buttonPanel.add(next);
        buttonPanel.add(nextMajor);
        buttonPanel.add(backBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(new JSeparator(JSeparator.VERTICAL));
        buttonPanel.add(new JLabel("Delay (ms):"));
        buttonPanel.add(speed);

        this.add(buttonPanel, BorderLayout.CENTER);
    }

    public void setMinCutEnabled(boolean enabled){
        minCutBtn.setEnabled(enabled);
    }
}