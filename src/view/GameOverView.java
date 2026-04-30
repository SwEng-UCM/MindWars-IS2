package view;

import controller.GameController;
import game.MapGrid;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import player.Player;

/**
 * End-of-game screen. Shows the winner, per-player stats, a final map
 * snapshot, and buttons to play again or return to the main menu.
 */
public class GameOverView extends JPanel {

    private final GameController controller;
    private final JLabel winnerLabel;
    private final JPanel statsPanel;
    private final FinalMapCanvas mapCanvas;
    private final JPanel legendPanel;

    public GameOverView(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(560, 680));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        card.add(MindWarsTheme.centeredLabel(
                "Game Over", MindWarsTheme.TITLE_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(12));

        winnerLabel = MindWarsTheme.centeredLabel(
                "", MindWarsTheme.HEADING_FONT, Color.BLACK);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(winnerLabel);
        card.add(Box.createVerticalStrut(18));

        card.add(sectionLabel("Player Statistics"));
        card.add(Box.createVerticalStrut(8));
        statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statsPanel);
        card.add(Box.createVerticalStrut(16));

        card.add(sectionLabel("Final Map"));
        card.add(Box.createVerticalStrut(6));

        legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        legendPanel.setOpaque(false);
        legendPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(legendPanel);
        card.add(Box.createVerticalStrut(6));

        mapCanvas = new FinalMapCanvas();
        mapCanvas.setAlignmentX(Component.CENTER_ALIGNMENT);
        mapCanvas.setPreferredSize(new Dimension(320, 220));
        mapCanvas.setMaximumSize(new Dimension(320, 220));
        card.add(mapCanvas);
        card.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgain = MindWarsTheme.createGradientButton("Play Again");
        playAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgain.addActionListener(e -> controller.restartGame());

        JButton back = MindWarsTheme.createGradientButton("Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> controller.onGameOverAcknowledged());

        buttons.add(playAgain);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(back);
        card.add(buttons);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    public void refresh() {
        controller.recordGameOnLeaderboard();
        Player winner = controller.getModel().computeWinner();
        winnerLabel.setText(
                winner == null ? "It's a draw!" : winner.getName() + " wins!");

        List<Player> players = controller.getModel().getPlayers();
        MapGrid map = controller.getModel().getMap();

        rebuildStats(players);
        rebuildLegend(players, map);
        mapCanvas.update(map, players);
    }

    private void rebuildStats(List<Player> players) {
        statsPanel.removeAll();
        if (players == null)
            return;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Color color = playerColor(i);

            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
            row.setBorder(new EmptyBorder(4, 0, 4, 0));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel name = new JLabel(p.getName() + "  —  " + p.getScore() + " pts");
            name.setFont(MindWarsTheme.BODY_BOLD);
            name.setForeground(color);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(name);

            row.add(statLine("Correct / Wrong",
                    p.getCorrectAnswers() + " / " + p.getWrongAnswers()));
            row.add(statLine("Avg response",
                    String.format("%.1fs", p.getAverageResponseTime())));
                double fastestSeconds = p.getFastestResponse();
                row.add(statLine("Fastest response",
                    fastestSeconds > 0 ? String.format("%.1fs", fastestSeconds) : "—"));

            statsPanel.add(row);
            if (i < players.size() - 1)
                statsPanel.add(Box.createVerticalStrut(6));
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JComponent statLine(String label, String value) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(MindWarsTheme.BODY_FONT);
        l.setForeground(new Color(0x555555));

        JLabel v = new JLabel(value);
        v.setFont(MindWarsTheme.BODY_BOLD);
        v.setForeground(Color.BLACK);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        line.add(l, BorderLayout.WEST);
        line.add(v, BorderLayout.EAST);
        return line;
    }

    private void rebuildLegend(List<Player> players, MapGrid map) {
        legendPanel.removeAll();
        if (players == null || map == null)
            return;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int territory = map.countTerritory(p.getSymbol());

            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            item.setOpaque(false);

            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new Dimension(14, 14));
            swatch.setBackground(playerColor(i));

            JLabel text = new JLabel(p.getSymbol() + ": " + territory);
            text.setFont(MindWarsTheme.SMALL_FONT);
            text.setForeground(Color.BLACK);

            item.add(swatch);
            item.add(text);
            legendPanel.add(item);
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(MindWarsTheme.BODY_BOLD);
        l.setForeground(MindWarsTheme.PINK);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static Color playerColor(int index) {
        return switch (index) {
            case 0 -> MindWarsTheme.PLAYER_X;
            case 1 -> MindWarsTheme.PLAYER_O;
            case 2 -> MindWarsTheme.PLAYER_A;
            case 3 -> MindWarsTheme.PLAYER_B;
            default -> MindWarsTheme.GRAY_LIGHT;
        };
    }

    private static class FinalMapCanvas extends JPanel {
        private MapGrid map;
        private List<Player> players;

        FinalMapCanvas() {
            setOpaque(false);
        }

        void update(MapGrid map, List<Player> players) {
            this.map = map;
            this.players = players;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (map == null)
                return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = map.getSize();
            int gap = 4;
            int cellSize = Math.min(
                    (getWidth() - gap * (size + 1)) / size,
                    (getHeight() - gap * (size + 1)) / size);

            int totalW = gap + size * (cellSize + gap);
            int totalH = gap + size * (cellSize + gap);
            int offsetX = (getWidth() - totalW) / 2;
            int offsetY = (getHeight() - totalH) / 2;

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int x = offsetX + gap + col * (cellSize + gap);
                    int y = offsetY + gap + row * (cellSize + gap);
                    char owner = map.getOwner(row, col);

                    Color fill = colorFor(owner);
                    g2.setColor(fill);
                    g2.fillRoundRect(x, y, cellSize, cellSize, 8, 8);
                    g2.setColor(MindWarsTheme.DARK_BORDER);
                    g2.drawRoundRect(x, y, cellSize, cellSize, 8, 8);
                }
            }
            g2.dispose();
        }

        private Color colorFor(char owner) {
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).getSymbol() == owner)
                        return playerColor(i);
                }
            }
            return MindWarsTheme.EMPTY_CELL;
        }
    }
}
