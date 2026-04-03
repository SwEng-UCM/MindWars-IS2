package ui;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(MainWindow parent) {

        setBackground(new Color(45, 45, 45));
        setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("MIND WARS");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(Color.RED);

        JButton startButton = new JButton("START GAME");
        startButton.setPreferredSize(new Dimension(200, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(startButton, gbc);
    }
}