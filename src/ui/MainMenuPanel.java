package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainMenuPanel extends RoundedPanel {

    private MainWindow parent;

    public MainMenuPanel(MainWindow parent) {

        super(30);
        this.parent = parent;
        setBackground(new Color(250, 248, 248));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(720, 920));
        setBorder(new EmptyBorder(28, 36, 28, 36));

        add(createTopBar());
        add(Box.createVerticalStrut(35));
        add(createLogo());
        add(Box.createVerticalStrut(25));
        add(createTitle());
        add(Box.createVerticalStrut(10));
        add(createSubtitle());
        add(Box.createVerticalStrut(55));

        add(createMenuButton("▶ New Game", new Color(255, 85, 170)));
        add(Box.createVerticalStrut(18));
        add(createMenuButton("⤴ Load Game", new Color(255, 70, 110)));
        add(Box.createVerticalStrut(18));
        add(createMenuButton("⚙ Settings", new Color(90, 84, 78)));
        add(Box.createVerticalStrut(18));
        add(createMenuButton("🏆 Leaderboard", new Color(220, 112, 0)));
        add(Box.createVerticalStrut(18));
        add(createMenuButton("📖 Rules", new Color(180, 174, 174)));
        add(Box.createVerticalStrut(18));
        add(createMenuButton("⇥ Logout", new Color(215, 210, 210)));
    }

    private JComponent createTopBar() {
        RoundedPanel topBar = new RoundedPanel(24);
        topBar.setBackground(new Color(245, 235, 240));
        topBar.setLayout(new BorderLayout());
        topBar.setMaximumSize(new Dimension(650, 62));
        topBar.setPreferredSize(new Dimension(650, 62));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 18, 0, 18));
        topBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton logout = new JButton("Logout");
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setFont(new Font("SansSerif", Font.BOLD, 16));
        logout.setForeground(new Color(90, 80, 80));
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logout.addActionListener(e -> System.out.println("Logout clicked"));

        topBar.add(logout, BorderLayout.EAST);

        return topBar;
    }

    private JComponent createLogo() {
        JLabel logo = new JLabel("⚔", SwingConstants.CENTER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setFont(new Font("SansSerif", Font.PLAIN, 64));
        logo.setForeground(Color.WHITE);

        RoundedPanel circle = new RoundedPanel(120) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(220, 60, 150),
                        getWidth(), getHeight(), new Color(110, 60, 80));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };

        circle.setLayout(new GridBagLayout());
        circle.setOpaque(false);
        circle.setMaximumSize(new Dimension(160, 160));
        circle.setPreferredSize(new Dimension(160, 160));
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);
        circle.add(logo);

        return circle;
    }

    private JComponent createTitle() {
        JLabel title = new JLabel("MindWars");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 78));
        title.setForeground(new Color(120, 40, 70));
        return title;
    }

    private JComponent createSubtitle() {
        JLabel subtitle = new JLabel("Conquer territories through knowledge");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 22));
        subtitle.setForeground(new Color(90, 90, 90));
        return subtitle;
    }

    private JButton createMenuButton(String text, Color color) {
        MenuButton button = new MenuButton(text, color);

        button.addActionListener(e -> {
            System.out.println(text + " clicked");
        });

        return button;
    }
}