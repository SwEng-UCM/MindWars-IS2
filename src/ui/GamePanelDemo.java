package ui;

import game.GameState;
import game.MapGrid;
import player.Player;

import javax.swing.*;

/**
 * Standalone demo for {@link GamePanel}.
 *
 * <p>Creates two dummy players, seeds some fake scores/streaks/coins and
 * territory claims, then shows the panel in a window.
 * Run this class directly from the IDE to preview the UI without starting
 * a full game session.
 */
public class GamePanelDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ── fake game state ──────────────────────────────────────────────────────
            GameState state = new GameState();

            Player p1 = new Player("Alice");
            p1.setSymbol('X');
            p1.addScore(350);
            p1.setStreakRaw(3);
            p1.addCoins(120);
            p1.addBonusToken();
            state.addPlayer(p1);

            Player p2 = new Player("Bob");
            p2.setSymbol('O');
            p2.addScore(290);
            p2.setStreakRaw(1);
            p2.addCoins(75);
            state.addPlayer(p2);

            state.setRoundNumber(3);
            state.setCurrentPlayerIndex(0);

            // ── fake map ─────────────────────────────────────────────────────────────
            MapGrid map = new MapGrid(5);
            map.initVisibilityForPlayer('X');
            map.initVisibilityForPlayer('O');

            // Claim some territory
            map.claimCell('X', 0, 0);
            map.claimCell('X', 0, 1);
            map.claimCell('X', 1, 0);
            map.claimCell('O', 4, 4);
            map.claimCell('O', 4, 3);
            map.claimCell('O', 3, 4);

            // ── show panel ───────────────────────────────────────────────────────────
            JFrame frame = new JFrame("GamePanel Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);

            GamePanel panel = new GamePanel(state, map);
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
