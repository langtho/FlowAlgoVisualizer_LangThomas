package main.GUI;

import javax.swing.*;
import java.awt.*;

public class HeadPanel extends JPanel {
    private JLabel algoNameLabel;
    private JButton loadGraphBtn, exportDotBtn;

    public HeadPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(35, 35, 35)); // Fond sombre
        this.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        algoNameLabel = new JLabel("Ford-Fulkerson");
        algoNameLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        algoNameLabel.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(algoNameLabel);

        loadGraphBtn = new JButton("Load Graph");
        loadGraphBtn.setFocusPainted(false);
        loadGraphBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loadGraphBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));

        exportDotBtn = new JButton("Export .dot");
        exportDotBtn.setFocusPainted(false);
        exportDotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportDotBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(exportDotBtn,BorderLayout.WEST);
        buttonPanel.add(loadGraphBtn, BorderLayout.EAST);

        this.add(titlePanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.EAST);
    }

    public void setAlgorithmName(String name) {
        algoNameLabel.setText(name);
    }

    public JButton getLoadGraphBtn() {
        return loadGraphBtn;
    }

    public JButton getExportDotBtn() {
        return exportDotBtn;
    }
}