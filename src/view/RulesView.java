package view;

import controller.NavigationController;

import javax.swing.*;
import java.awt.*;

public class RulesView extends JPanel {

    public RulesView(NavigationController nav) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Game Rules", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea rulesText = new JTextArea(getRules());
        rulesText.setEditable(false);
        rulesText.setLineWrap(true);
        rulesText.setWrapStyleWord(true);
        rulesText.setFont(new Font("Arial", Font.PLAIN, 14));
        rulesText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(rulesText);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> nav.showMainMenu());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String getRules() {
        return """
                Objective:
                Answer questions correctly and faster than your opponent to score more points.

                Game Modes:
                - Solo Mode: play against a bot
                - Multiplayer Mode: play against another player

                Question Types:
                - Multiple Choice
                - True / False
                - Estimation

                Scoring:
                - Correct answer: base points
                - Faster answer: +1 bonus point
                - Answer within 3 seconds: double points
                - Wrong answer: 0 points

                Bot Difficulty:
                - Easy Bot: answers randomly after a short delay
                - Hard Bot: usually answers correctly but can make mistakes

                Winner:
                The player with the highest score at the end wins.
                """;
    }
}