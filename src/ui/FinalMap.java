package ui;

import game.MapGrid;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class FinalMap extends JPanel {
        private final MapGrid map;
        private final List<Player> players;

        public FinalMap(MapGrid map, List<Player> players) {
            this.map = map;
            this.players = players;
            setOpaque(false);
            setPreferredSize(new Dimension(560, 360));
            setMinimumSize(new Dimension(560, 360));
            setMaximumSize(new Dimension(560, 360));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = map.getSize();
            int gap = 10;
            int cellSize = Math.min((getWidth() - (gap * (size + 1))) / size,
                    (getHeight() - (gap * (size + 1))) / size);

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int x = gap + col * (cellSize + gap);
                    int y = gap + row * (cellSize + gap);

                    char owner = map.getCell(row, col);

                    if (owner == 'X') {
                        g2.setColor(new Color(255, 45, 170));
                    } else if (owner == 'O') {
                        g2.setColor(new Color(110, 104, 98));
                    } else {
                        g2.setColor(new Color(90, 85, 80));
                    }

                    g2.fillRoundRect(x, y, cellSize, cellSize, 12, 12);

                    g2.setColor(new Color(150, 140, 140));
                    g2.drawRoundRect(x, y, cellSize, cellSize, 12, 12);
                }
            }

            g2.dispose();
        }
}

