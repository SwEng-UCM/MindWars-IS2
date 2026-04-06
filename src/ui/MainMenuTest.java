package ui;

import javax.swing.*;

public class MainMenuTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MindWars");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 900);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MainMenuPanel());
            frame.setVisible(true);
        });
    }
}