package main;

import javax.swing.SwingUtilities;
import main.controller.MainController;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainController());
    }
}