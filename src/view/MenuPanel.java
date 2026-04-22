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

    private MainWindow parent;
    private BufferedImage logoImage;

    private JTextField emailField;
    private JPasswordField passField;
    private LoginController controller;

    public MenuPanel(MainWindow parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        try {
            logoImage = ImageIO.read(new File("assets/logo.png"));
        } catch (Exception e) {
            System.err.println(
                "Could not load logo.png. Ensure it is in the assets/ folder."
            );
        }

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2d.setColor(Color.WHITE);
                g2d.fill(
                    new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        40,
                        40
                    )
                );
                g2d.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 30, 10, 30);
        JLabel logoLabel = new JLabel();
        if (logoImage != null) {
            int targetHeight = 150;
            int targetWidth = (int) (logoImage.getWidth() *
                ((double) targetHeight / logoImage.getHeight()));
            Image scaledLogo = logoImage.getScaledInstance(
                targetWidth,
                targetHeight,
                Image.SCALE_SMOOTH
            );
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 30, 20, 30);
        JLabel sub = new JLabel(
            "Welcome back! Login to continue",
            SwingConstants.CENTER
        );
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        card.add(sub, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 30, 20, 30);
        card.add(createTabPanel(), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 30, 2, 30);
        JLabel emailLabel = new JLabel("✉ Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(emailLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 30, 15, 30);
        emailField = createStyledTextField(EMAIL_PLACEHOLDER);
        card.add(emailField, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 30, 2, 30);
        JLabel passLabel = new JLabel("🔒 Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(passLabel, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 30, 15, 30);
        passField = createStyledPasswordField(PASSWORD_PLACEHOLDER);
        card.add(passField, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(30, 30, 10, 30);
        JButton loginBtn = createGradientButton("→ Login to Game");

        loginBtn.addActionListener(e -> {
            if (controller != null) {
                controller.login();
            }
        });
        card.add(loginBtn, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 30, 20, 30);
        JButton skipBtn = new JButton("Continue without login");
        skipBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        skipBtn.setForeground(Color.GRAY);
        skipBtn.setContentAreaFilled(false);
        skipBtn.setBorderPainted(false);
        skipBtn.setFocusPainted(false);
        skipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> parent.startGameSession());
        card.add(skipBtn, gbc);

        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
            0,
            0,
            new Color(213, 58, 137),
            getWidth(),
            getHeight(),
            new Color(223, 103, 31)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        JButton loginTab = new JButton("→ Login");
        JButton regTab = new JButton("👤 Register");

        regTab.addActionListener(e -> parent.showScreen("REGISTER"));

        styleTabButton(loginTab, true);
        styleTabButton(regTab, false);

        panel.add(loginTab);
        panel.add(regTab);
        return panel;
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(100, 45));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(213, 58, 137));
            btn.setBorder(
                BorderFactory.createLineBorder(
                    new Color(213, 58, 137, 80),
                    1,
                    true
                )
            );
        } else {
            btn.setBackground(new Color(248, 248, 248));
            btn.setForeground(Color.GRAY);
            btn.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(300, 45));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220, 220, 220),
                    1,
                    true
                ),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            )
        );

        setupTextPlaceholder(tf, placeholder);
        return tf;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(300, 45));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220, 220, 220),
                    1,
                    true
                ),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            )
        );

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
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                GradientPaint gp = new GradientPaint(
                    0,
                    0,
                    new Color(213, 58, 137),
                    getWidth(),
                    0,
                    new Color(180, 80, 0)
                );
                g2d.setPaint(gp);
                g2d.fill(
                    new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        25,
                        25
                    )
                );
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(300, 55));
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

    public void showError(String msg) {
        JOptionPane.showMessageDialog(
            this,
            msg,
            "Login Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(
            this,
            msg,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}