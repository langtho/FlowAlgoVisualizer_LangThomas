package main.GUI;

import javax.swing.*;
import java.awt.*;

public class ExplanationPanel extends JPanel {
    private JLabel titleLabel;
    private JTextArea descArea;

    public ExplanationPanel() {
        this.setLayout(new BorderLayout(10, 5));
        this.setBackground(new Color(250, 250, 250));
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        titleLabel = new JLabel("Algorithm Explanation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(45, 45, 48));

        descArea = new JTextArea("Select an algorithm to begin.");
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descArea.setForeground(new Color(60, 60, 60));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setFocusable(false);

        this.add(titleLabel, BorderLayout.NORTH);
        this.add(descArea, BorderLayout.CENTER);
    }

    public void updateText(String title, String desc) {
        SwingUtilities.invokeLater(() -> {
            if (title != null) titleLabel.setText(title);
            if (desc != null) descArea.setText(desc);
        });
    }
}