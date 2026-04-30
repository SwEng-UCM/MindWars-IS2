package view;

import controller.NavigationController;
import model.LeaderboardEntry;
import model.LeaderboardStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Persistent leaderboard screen (#89). Shows a sorted table of all
 * players who have ever played, with their wins, total score, and
 * games played. Data is backed by {@link LeaderboardStore}.
 */
public class LeaderboardView extends JPanel {

    private static final String[] COLUMNS = { "#", "Player", "Wins", "Total Score", "Games" };

    private final LeaderboardStore store;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public LeaderboardView(NavigationController nav) {
        this(nav, new LeaderboardStore());
    }

    public LeaderboardView(NavigationController nav, LeaderboardStore store) {
        this.store = store;
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setPreferredSize(new Dimension(440, 560));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = MindWarsTheme.centeredLabel("Leaderboard",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK);
        card.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(MindWarsTheme.BODY_FONT);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(MindWarsTheme.PINK_BG);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.getTableHeader().setFont(MindWarsTheme.BODY_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());

        table.getColumnModel().getColumn(0).setCellRenderer(new CellRenderer(SwingConstants.CENTER));
        for (int i : new int[] { 2, 3, 4 }) {
            table.getColumnModel().getColumn(i).setCellRenderer(new CellRenderer(SwingConstants.RIGHT));
        }
        table.getColumnModel().getColumn(1).setCellRenderer(new CellRenderer(SwingConstants.LEFT));

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(MindWarsTheme.GRAY_LIGHT));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JButton back = MindWarsTheme.createPinkButton("Back to Menu");
        back.addActionListener(e -> nav.showMainMenu());
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(back);
        card.add(south, BorderLayout.SOUTH);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Reloads the table from disk. Called by MainFrame before the card shows. */
    public void reload() {
        store.reload();
        tableModel.setRowCount(0);
        List<LeaderboardEntry> entries = store.getEntries();
        int rank = 1;
        for (LeaderboardEntry e : entries) {
            tableModel.addRow(new Object[] {
                    rank++,
                    e.getName(),
                    e.getWins(),
                    e.getTotalScore(),
                    e.getGamesPlayed()
            });
        }
        if (entries.isEmpty()) {
            tableModel.addRow(new Object[] { "—", "(no games played yet)", "", "", "" });
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(MindWarsTheme.BODY_BOLD);
            setBackground(MindWarsTheme.PINK);
            setForeground(Color.WHITE);
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MindWarsTheme.PINK));
            return this;
        }
    }

    private static final class CellRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        private CellRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(alignment);
            setFont(MindWarsTheme.BODY_FONT);
            setForeground(Color.BLACK);
            if (row == 0) {
                setBackground(new Color(255, 236, 179)); // gold
            } else if (row == 1) {
                setBackground(new Color(232, 235, 239)); // silver
            } else if (row == 2) {
                setBackground(new Color(223, 194, 164)); // bronze
            } else {
                setBackground(Color.WHITE);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }
}
