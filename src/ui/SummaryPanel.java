package ui;

import game.MapGrid;
import player.Player;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

public class SummaryPanel extends RoundedPanel {

    public SummaryPanel(List<Player> players, MapGrid map) {
        super(30);
        setBackground(new Color(22, 18, 18));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(30, 50, 30, 50));
        setPreferredSize(new Dimension(760, 1250));

        add(createTitle("Thanks for playing MindWars!", 34));
        add(Box.createVerticalStrut(35));
        add(createTitle("=== PLAYER STATISTICS ===", 28));
        add(Box.createVerticalStrut(30));

        for (Player player : players) {
            add(createPlayerStats(player));
            add(Box.createVerticalStrut(25));
        }

        add(Box.createVerticalStrut(15));
        add(createTitle("=== FINAL MAP ===", 28));
        add(Box.createVerticalStrut(25));

        add(createLegend(players, map));
        add(Box.createVerticalStrut(20));
        add(createMapCard(players, map));
        add(Box.createVerticalStrut(30));

        JButton playAgainButton = new GradientButton("↻  Play Again");
        JButton menuButton = new GradientButton("⌂  Back to Menu");

        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        playAgainButton.addActionListener(e -> System.out.println("Play again"));
        menuButton.addActionListener(e -> System.out.println("Back to menu"));

        add(playAgainButton);
        add(Box.createVerticalStrut(18));
        add(menuButton);
    }

    private JLabel createTitle(String text, int size) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JComponent createPlayerStats(Player player) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(620, 180));

        JLabel name = new JLabel(player.getName() + ":");
        name.setFont(new Font("SansSerif", Font.BOLD, 22));
        name.setForeground(Color.WHITE);

        panel.add(name);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createStatRow("Correct Answers", String.valueOf(player.getCorrectAnswers())));
        panel.add(createStatRow("Wrong Answers", String.valueOf(player.getWrongAnswers())));
        panel.add(createStatRow("Average Response", String.format("%.1fs", player.getAverageResponseTime())));
        panel.add(createStatRow("Fastest Response", String.format("%.1fs", player.getFastestResponse() / 1000.0)));

        return panel;
    }

    private JComponent createStatRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(620, 35));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));
        label.setForeground(new Color(220, 220, 220));

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("SansSerif", Font.BOLD, 18));
        value.setForeground(Color.WHITE);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private JComponent createLegend(List<Player> players, MapGrid map) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Player player : players) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            row.setOpaque(false);

            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(18, 18));
            colorBox.setBackground(getPlayerColor(player));

            JLabel label = new JLabel(player.getName() + ": " + map.countTerritory(player.getSymbol()));
            label.setFont(new Font("SansSerif", Font.BOLD, 16));
            label.setForeground(Color.WHITE);

            row.add(colorBox);
            row.add(label);
            panel.add(row);
        }

        return panel;
    }

    private JComponent createMapCard(List<Player> players, MapGrid map) {
        RoundedPanel card = new RoundedPanel(24);
        card.setBackground(new Color(40, 34, 34));
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(620, 420));
        card.setPreferredSize(new Dimension(620, 420));
        card.setMinimumSize(new Dimension(620, 420));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        FinalMap finalMap = new FinalMap(map, players);
        finalMap.setPreferredSize(new Dimension(560, 360));
        finalMap.setMinimumSize(new Dimension(560, 360));
        finalMap.setMaximumSize(new Dimension(560, 360));

        card.add(finalMap, BorderLayout.CENTER);

        return card;
    }

    private Color getPlayerColor(Player player) {
        if (player.getSymbol() == 'X') {
            return new Color(255, 45, 170);
        } else {
            return new Color(110, 104, 98);
        }
    }

}
