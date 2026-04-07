package ui;

import javax.swing.*;
import java.awt.*;

public class MainMenuFrame extends JFrame {
    public MainMenuFrame() {
        setTitle("MindWars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientBackgroundPanel background = new GradientBackgroundPanel();
        background.setLayout(new GridBagLayout());

        MainMenuPanel menuPanel = new MainMenuPanel(null);

        background.add(menuPanel);
        setContentPane(background);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenuFrame frame = new MainMenuFrame();
            frame.setVisible(true);
        });
    }
}
