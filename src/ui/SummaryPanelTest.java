package ui;

import game.MapGrid;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SummaryPanelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Player p1 = new Player("ashley");
            p1.setSymbol('X');
            p1.addScore(50);
            p1.setTimer(14000);
            p1.addCorrectAnswer(1);
            p1.addCorrectAnswer(1);
            p1.addCorrectAnswer(1);

            Player p2 = new Player("aloyse");
            p2.setSymbol('O');
            p2.addScore(40);
            p2.setTimer(3000);
            p2.addCorrectAnswer(1);
            p2.addWrongAnswer(1);
            p2.addWrongAnswer(1);

            MapGrid map = new MapGrid(3);

            map.claimCell('X', 1, 0);
            map.claimCell('X', 1, 2);
            map.claimCell('X', 2, 0);
            map.claimCell('X', 2, 2);
            map.claimCell('X', 0, 2);

            map.claimCell('O', 0, 0);
            map.claimCell('O', 0, 1);
            map.claimCell('O', 1, 1);
            map.claimCell('O', 2, 1);

            JFrame frame = new JFrame("Summary Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 1100);
            frame.setLocationRelativeTo(null);

            GradientBackgroundPanel bg = new GradientBackgroundPanel();
            SummaryPanel summary = new SummaryPanel(List.of(p1, p2), map);

            JScrollPane scrollPane = new JScrollPane(summary);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16); // smooth scroll
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            bg.setLayout(new BorderLayout());
            bg.add(scrollPane, BorderLayout.CENTER);
            frame.setContentPane(bg);
            frame.setVisible(true);
        });
    }
}