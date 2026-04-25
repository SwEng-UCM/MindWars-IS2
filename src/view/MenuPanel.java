package view;

import controller.LoginController;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * MenuPanel handles the Login UI, including the gradient background,
 * custom styled inputs, and the transition to the main game.
 */
public class MenuPanel extends JPanel {

    private static final String EMAIL_PLACEHOLDER = "your.email@example.com";
    private static final String PASSWORD_PLACEHOLDER = "Enter your password";
    private static final Color BRAND_PINK = new Color(213, 58, 137);
    private static final Color BRAND_ORANGE = new Color(223, 103, 31);
    private static final Color INPUT_BORDER = new Color(164, 164, 164);
    private static final Color INPUT_FOCUS = new Color(213, 58, 137);
    private static final int CONTROL_WIDTH = 370;
    private static final int CONTROL_HEIGHT = 54;

    private MainWindow parent;
    private BufferedImage logoImage;

    private JTextField emailField;
    private JPasswordField passField;
    private JScrollPane cardScroll;
    private LoginController controller;

    public MenuPanel(MainWindow parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        try {
            logoImage = ImageIO.read(new File("assets/logo.png"));
        } catch (Exception e) {
            System.err.println(
                    "Could not load logo.png. Ensure it is in the assets/ folder.");
        }

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 28));
                g2d.fill(
                        new RoundRectangle2D.Double(
                                4,
                                6,
                                getWidth() - 8,
                                getHeight() - 8,
                                42,
                                42));
                g2d.setColor(Color.WHITE);
                g2d.fill(
                        new RoundRectangle2D.Double(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                40,
                                40));
                g2d.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
                super.paintChildren(g2d);
                g2d.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        Dimension cardSize = new Dimension(420, 650);
        card.setPreferredSize(cardSize);
        card.setMinimumSize(cardSize);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 22, 10, 22);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 22, 8, 22);
        JLabel logoLabel = new JLabel();
        if (logoImage != null) {
            int targetHeight = 150;
            int targetWidth = (int) (logoImage.getWidth() *
                    ((double) targetHeight / logoImage.getHeight()));
            Image scaledLogo = logoImage.getScaledInstance(
                    targetWidth,
                    targetHeight,
                    Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(logoLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 22, 12, 22);
        JLabel sub = new JLabel(
                "Welcome back! Login to continue",
                SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        content.add(sub, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 22, 12, 22);
        content.add(createTabPanel(), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(8, 22, 4, 22);
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        content.add(emailLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(6, 22, 12, 22);
        gbc.ipady = 4;
        emailField = createStyledTextField(EMAIL_PLACEHOLDER);
        content.add(emailField, gbc);
        gbc.ipady = 0;

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 22, 4, 22);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        content.add(passLabel, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(6, 22, 12, 22);
        gbc.ipady = 4;
        passField = createStyledPasswordField(PASSWORD_PLACEHOLDER);
        content.add(passField, gbc);
        gbc.ipady = 0;

        gbc.gridy = 7;
        gbc.insets = new Insets(14, 22, 10, 22);
        JButton loginBtn = createGradientButton("Login to Game");

        loginBtn.addActionListener(e -> {
            if (controller != null) {
                controller.login();
            }
        });
        content.add(loginBtn, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 22, 10, 22);
        JButton skipBtn = new JButton("Continue without login");
        skipBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        skipBtn.setForeground(Color.GRAY);
        skipBtn.setContentAreaFilled(false);
        skipBtn.setBorderPainted(false);
        skipBtn.setFocusPainted(false);
        skipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> parent.startGameSession());
        content.add(skipBtn, gbc);

        cardScroll = new JScrollPane(
                content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cardScroll.setBorder(BorderFactory.createEmptyBorder());
        cardScroll.setOpaque(false);
        cardScroll.getViewport().setOpaque(false);
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);
        cardScroll.getVerticalScrollBar().setBlockIncrement(64);
        cardScroll.setMinimumSize(new Dimension(420, 650));
        JPanel scrollHost = new JPanel(new BorderLayout());
        scrollHost.setOpaque(false);
        scrollHost.setBorder(new EmptyBorder(0, 6, 4, 6));
        scrollHost.add(cardScroll, BorderLayout.CENTER);
        card.add(scrollHost, BorderLayout.CENTER);
        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.gridx = 0;
        rootGbc.gridy = 0;
        rootGbc.weightx = 1;
        rootGbc.weighty = 1;
        rootGbc.anchor = GridBagConstraints.CENTER;
        rootGbc.fill = GridBagConstraints.NONE;
        rootGbc.insets = new Insets(10, 10, 10, 10);
        add(card, rootGbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
                0,
                0,
                BRAND_PINK,
                getWidth(),
                getHeight(),
                BRAND_ORANGE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        JButton loginTab = new JButton("Login");
        JButton regTab = new JButton("Register");

        regTab.addActionListener(e -> parent.showScreen("REGISTER"));

        styleTabButton(loginTab, true);
        styleTabButton(regTab, false);

        panel.add(loginTab);
        panel.add(regTab);
        return panel;
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150, 48));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(BRAND_PINK);
            btn.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(BRAND_PINK.getRed(), BRAND_PINK.getGreen(), BRAND_PINK.getBlue(), 190),
                            2,
                            true));
        } else {
            btn.setBackground(new Color(248, 248, 248));
            btn.setForeground(new Color(120, 120, 120));
            btn.setBorder(BorderFactory.createLineBorder(new Color(186, 186, 186), 2, true));
        }
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new RoundedTextField(16);
        Dimension size = new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT);
        tf.setPreferredSize(size);
        tf.setMinimumSize(size);
        tf.setMaximumSize(size);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        setupTextPlaceholder(tf, placeholder);
        return tf;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField pf = new RoundedPasswordField(16);
        Dimension size = new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT);
        pf.setPreferredSize(size);
        pf.setMinimumSize(size);
        pf.setMaximumSize(size);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pf.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        setupPasswordPlaceholder(pf, placeholder);
        return pf;
    }

    private void setupTextPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void setupPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setEchoChar((char) 0);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(field.getPassword()).trim();
                if (current.isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char) 0);
                }
            }
        });
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0,
                        0,
                        BRAND_PINK,
                        getWidth(),
                        0,
                        new Color(180, 80, 0));
                g2d.setPaint(gp);
                g2d.fill(
                        new RoundRectangle2D.Double(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                25,
                                25));
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(CONTROL_WIDTH, 56));
        return btn;
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    public String getEmailInput() {
        String value = emailField.getText().trim();
        return value.equals(EMAIL_PLACEHOLDER) ? "" : value;
    }

    public String getPasswordInput() {
        String value = new String(passField.getPassword());
        return value.equals(PASSWORD_PLACEHOLDER) ? "" : value;
    }

    public MainWindow getParentWindow() {
        return parent;
    }

    public void scrollToTop() {
        if (cardScroll == null) {
            return;
        }
        cardScroll.getVerticalScrollBar().setValue(0);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static class RoundedTextField extends JTextField {
        private final int arc;

        RoundedTextField(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus() ? INPUT_FOCUS : INPUT_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final int arc;

        RoundedPasswordField(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus() ? INPUT_FOCUS : INPUT_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
        }
    }
}