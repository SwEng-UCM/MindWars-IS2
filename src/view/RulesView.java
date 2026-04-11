package view;

import controller.NavigationController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RulesView extends JPanel {

    private JScrollPane scrollPane;

    public RulesView(NavigationController nav) {
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BorderLayout(0, 18));
        card.setPreferredSize(new Dimension(440, 600));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = MindWarsTheme.centeredLabel(
                "Rules",
                MindWarsTheme.HEADING_FONT,
                MindWarsTheme.PINK
        );

        JLabel subtitle = MindWarsTheme.centeredLabel(
                "How to play MindWars",
                MindWarsTheme.SUBTITLE_FONT,
                MindWarsTheme.GRAY_TEXT
        );

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(4, 2, 4, 2));

        content.add(createSection("Objective",
                "Answer questions correctly and faster than your opponent to earn more points."));
        content.add(Box.createVerticalStrut(12));

        content.add(createSection("Game Modes",
                " Solo Mode: play against a bot\n" +
                " Multiplayer Mode: play against another player"));
        content.add(Box.createVerticalStrut(12));

        content.add(createSection("Question Types",
                " Multiple Choice\n" +
                " True / False\n" +
                " Estimation"));
        content.add(Box.createVerticalStrut(12));

        content.add(createSection("Scoring",
                " Correct answer: base points\n" +
                " Faster answer: +1 bonus point\n" +
                " Answer within 3 seconds: double points\n" +
                " Wrong answer: 0 points"));
        content.add(Box.createVerticalStrut(12));

        content.add(createSection("Bot Difficulty",
                " Easy Bot: answers randomly after a short delay\n" +
                " Hard Bot: usually answers correctly but can still make mistakes"));
        content.add(Box.createVerticalStrut(12));

        content.add(createSection("Winner",
                "The player with the highest score at the end of the game wins."));

        scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
       
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JButton back = MindWarsTheme.createPinkButton("Back to Menu");
        back.addActionListener(e -> nav.showMainMenu());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);

        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    private JPanel createSection(String heading, String body) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new EmptyBorder(10, 10, 10, 10));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(MindWarsTheme.SUBTITLE_FONT);
        headingLabel.setForeground(MindWarsTheme.PINK);
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea bodyArea = new JTextArea(body);
        bodyArea.setFont(MindWarsTheme.BODY_FONT);
        bodyArea.setForeground(new Color(90, 90, 90));
        bodyArea.setEditable(false);
        bodyArea.setOpaque(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBorder(null);
        bodyArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        section.add(headingLabel);
        section.add(Box.createVerticalStrut(4));
        section.add(bodyArea);

        return section;
    }
}