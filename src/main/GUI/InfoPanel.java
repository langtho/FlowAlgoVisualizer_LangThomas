package main.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class InfoPanel extends JPanel {
    private JLabel flowVal, bottleneckVal, costVal;
    private JTextArea dataArea;
    private TitledBorder dataBorder;
    private JTextArea bottleEdgesArea;
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color ACCENT_COLOR = new Color(70, 130, 180);

    public InfoPanel() {
        this.setPreferredSize(new Dimension(300, 0));
        this.setLayout(new BorderLayout());
        this.setBackground(BACKGROUND_COLOR);

        JPanel varBox = new JPanel(new GridBagLayout());
        varBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Stats", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)));
        varBox.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addVarRow(varBox, gbc, 0, "Total Flow :", flowVal = new JLabel("0", SwingConstants.RIGHT));
        flowVal.setForeground(ACCENT_COLOR);
        flowVal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        addVarRow(varBox, gbc, 1, "Total Cost :", costVal = new JLabel("0", SwingConstants.RIGHT));
        costVal.setForeground(ACCENT_COLOR);
        costVal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        addVarRow(varBox, gbc, 2, "Bottleneck (min) :", bottleneckVal = new JLabel("-", SwingConstants.RIGHT));
        bottleneckVal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 2, 10);
        varBox.add(new JLabel("MinCut Edge/s:"), gbc);

        bottleEdgesArea = createStyledTextArea(3);
        JScrollPane bottleScroll = new JScrollPane(bottleEdgesArea);
        gbc.gridy = 4;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.BOTH;
        varBox.add(bottleScroll, gbc);

        dataArea = createStyledTextArea(10);
        dataArea.setFont(new Font("Consolas",Font.PLAIN,12));
        JScrollPane dataScroll = new JScrollPane(dataArea);
        dataBorder= BorderFactory.createTitledBorder("Algo Data");
        dataScroll.setBorder(dataBorder);
        gbc.gridy = 5;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        varBox.add(dataScroll, gbc);



        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(varBox, BorderLayout.NORTH);

        this.add(contentPanel, BorderLayout.CENTER);
    }

    public void updateInfo(int flow, int cost, String bottle, String bottleEdge, String path) {
        SwingUtilities.invokeLater(() -> {
            flowVal.setText(String.valueOf(flow));
            costVal.setText(String.valueOf(cost));
            bottleneckVal.setText(bottle);
            bottleEdgesArea.setText(bottleEdge);
            dataArea.setText(path);

        });
    }

    private void addVarRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valLabel) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.5;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        panel.add(valLabel, gbc);
    }

    private JTextArea createStyledTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 10);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(5, 5, 5, 5));
        return area;
    }

    public void updateDataView(String data, String title) {
        SwingUtilities.invokeLater(() -> {
            dataArea.setText(data);
            if (title != null) {
                dataBorder.setTitle(title);
                this.repaint();
            }
        });
    }
}