package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class RegisterPanel extends JPanel {

    private MainWindow parent;
    private BufferedImage logoImage;

    public RegisterPanel(MainWindow parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // logo
        try {
            logoImage = ImageIO.read(new File("assets/logo.png"));
        } catch (Exception e) {
            System.err.println("Could not load logo.png.");
        }

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
                g2d.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 30, 2, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // logo
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 30, 10, 30);
        JLabel logoLabel = new JLabel();
        if (logoImage != null) {

            int targetHeight = 150;
            int targetWidth = (int) (logoImage.getWidth() * ((double) targetHeight / logoImage.getHeight()));
            Image scaledLogo = logoImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, gbc);

        // subtitle
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 30, 20, 30);
        JLabel sub = new JLabel("Join the adventure! Create an account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        card.add(sub, gbc);

        // tab panel
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 30, 10, 30);
        card.add(createTabPanel(), gbc);
        gbc.insets = new Insets(2, 30, 0, 30);

        gbc.gridy = 3;
        card.add(createLabel("Username"), gbc);
        gbc.gridy = 4;
        card.add(createStyledTextField("Choose a username"), gbc);

        gbc.gridy = 5;
        card.add(createLabel("Email Address"), gbc);
        gbc.gridy = 6;
        card.add(createStyledTextField("email@example.com"), gbc);

        gbc.gridy = 7;
        card.add(createLabel("Password"), gbc);
        gbc.gridy = 8;
        card.add(createStyledPasswordField(), gbc);

        gbc.gridy = 9;
        card.add(createLabel("Confirm Password"), gbc);
        gbc.gridy = 10;
        card.add(createStyledPasswordField(), gbc);

        // register button
        gbc.gridy = 11;
        gbc.insets = new Insets(15, 30, 20, 30);
        JButton regBtn = createGradientButton("Create Account");
        regBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Account Created!");
            parent.showScreen("MENU");
        });
        card.add(regBtn, gbc);

        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(213, 58, 137), getWidth(), getHeight(),
                new Color(223, 103, 31));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // helpers
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        JButton btnL = new JButton("Login");
        JButton btnR = new JButton("Register");
        styleTabButton(btnL, false);
        styleTabButton(btnR, true);
        btnL.addActionListener(e -> parent.showScreen("MENU"));
        panel.add(btnL);
        panel.add(btnR);
        return panel;
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(213, 58, 137));
            btn.setBorder(BorderFactory.createLineBorder(new Color(213, 58, 137, 80), 1, true));
        } else {
            btn.setBackground(new Color(248, 248, 248));
            btn.setForeground(Color.GRAY);
            btn.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setPreferredSize(new Dimension(300, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField("******");
        pf.setPreferredSize(new Dimension(300, 35));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        return pf;
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(213, 58, 137), getWidth(), 0,
                        new Color(180, 80, 0));
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(300, 45));
        return btn;
    }
}