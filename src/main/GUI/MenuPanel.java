package main.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MenuPanel extends JPanel {
    private JButton fordBtn;
    private JButton bellmanBtn;
    private JButton dijkstraBtn;
    private JButton floydBtn;

    public MenuPanel() {
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        this.setBackground(new Color(45, 45, 48)); // Slightly darker, professional grey
        this.setBorder(new EmptyBorder(5, 0, 5, 0));

        fordBtn = createStyledButton("Ford-Fulkerson");
        bellmanBtn = createStyledButton("MinCost: Bellman-Ford");
        dijkstraBtn = createStyledButton("MinCost: Dijkstra");
        floydBtn = createStyledButton("Cycle: Floyd-Warshall");

        this.add(fordBtn);
        this.add(bellmanBtn);
        this.add(floydBtn);
        this.add(dijkstraBtn);

    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 122, 204)); // "Visual Studio Blue" on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(70, 70, 70));
            }
        });

        return btn;
    }

    public JButton getFordBtn() { return fordBtn; }
    public JButton getBellmanBtn() { return bellmanBtn; }
    public JButton getDijkstraBtn() { return dijkstraBtn; }
    public JButton getFloydBtn() { return floydBtn; }
}