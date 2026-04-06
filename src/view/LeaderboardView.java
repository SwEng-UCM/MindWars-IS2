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

    private static final String[] COLUMNS = {"#", "Player", "Wins", "Total Score", "Games"};

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
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(MindWarsTheme.BODY_FONT);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setSelectionBackground(MindWarsTheme.PINK_BG);
        table.getTableHeader().setFont(MindWarsTheme.BODY_BOLD);
        table.getTableHeader().setBackground(MindWarsTheme.PINK);
        table.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i : new int[]{0, 2, 3, 4}) {
            table.getColumnModel().getColumn(i).setCellRenderer(right);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(MindWarsTheme.GRAY_LIGHT));
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
            tableModel.addRow(new Object[]{
                    rank++,
                    e.getName(),
                    e.getWins(),
                    e.getTotalScore(),
                    e.getGamesPlayed()
            });
        }
        if (entries.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "(no games played yet)", "", "", ""});
        }
    }
}
