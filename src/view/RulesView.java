package view;

import controller.NavigationController;

public class RulesView extends PlaceholderView {

    private static final String RULES_TEXT = """
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
            - Hard Bot: usually answers correctly, but can still make mistakes

            Winner:
            The player with the highest score at the end of the game wins.
            """;

    public RulesView(NavigationController nav) {
        super("Rules", RULES_TEXT, nav);
    }
}