package view;

import controller.RegisterController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RegisterPanel extends JPanel {

    private static final String PASSWORD_PLACEHOLDER = "Enter your password";
    private static final String CONFIRM_PASSWORD_PLACEHOLDER = "Confirm your password";
    private static final Color BRAND_PINK = new Color(213, 58, 137);
    private static final Color BRAND_ORANGE = new Color(223, 103, 31);
    private static final Color INPUT_BORDER = new Color(164, 164, 164);
    private static final Color INPUT_FOCUS = new Color(213, 58, 137);
    private static final int CONTROL_WIDTH = 370;
    private static final int CONTROL_HEIGHT = 54;

    private MainWindow parent;
    private BufferedImage logoImage;

    // variables accesed in ActionListener
    private JTextField userField;
    private JTextField emailField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;
    private JScrollPane cardScroll;
    private RegisterController controller;

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
                g2d.setColor(new Color(0, 0, 0, 28));
                g2d.fill(new RoundRectangle2D.Double(4, 6, getWidth() - 8, getHeight() - 8, 42, 42));
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
                g2d.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        gbc.insets = new Insets(4, 22, 4, 22);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // logo
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 22, 8, 22);
        JLabel logoLabel = new JLabel();
        if (logoImage != null) {

            int targetHeight = 150;
            int targetWidth = (int) (logoImage.getWidth() * ((double) targetHeight / logoImage.getHeight()));
            Image scaledLogo = logoImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(logoLabel, gbc);

        // subtitle
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 22, 12, 22);
        JLabel sub = new JLabel("Join the adventure! Create an account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        content.add(sub, gbc);

        // tab panel
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 22, 12, 22);
        content.add(createTabPanel(), gbc);

        gbc.insets = new Insets(6, 22, 2, 22);

        gbc.gridy = 3;
        content.add(createLabel("Username"), gbc);
        gbc.gridy = 4;
        gbc.ipady = 4;
        userField = createStyledTextField("Choose a username");
        content.add(userField, gbc);
        gbc.ipady = 0;

        gbc.gridy = 5;
        content.add(createLabel("Email Address"), gbc);
        gbc.gridy = 6;
        gbc.ipady = 4;
        emailField = createStyledTextField("email@example.com");
        content.add(emailField, gbc);
        gbc.ipady = 0;

        gbc.gridy = 7;
        content.add(createLabel("Password"), gbc);
        gbc.gridy = 8;
        gbc.ipady = 4;
        passField = createStyledPasswordField(PASSWORD_PLACEHOLDER);
        content.add(passField, gbc);
        gbc.ipady = 0;

        gbc.gridy = 9;
        content.add(createLabel("Confirm Password"), gbc);
        gbc.gridy = 10;
        gbc.ipady = 4;
        confirmPassField = createStyledPasswordField(CONFIRM_PASSWORD_PLACEHOLDER);
        content.add(confirmPassField, gbc);
        gbc.ipady = 0;

        // register button
        gbc.gridy = 11;
        gbc.insets = new Insets(16, 22, 10, 22);
        JButton regBtn = createGradientButton("Create Account");

        regBtn.addActionListener(e -> {
            System.out.println("Create Account pressed");
            if (controller != null){
                controller.register();

            }        });
        content.add(regBtn, gbc);

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

    public void setController(RegisterController controller) {
        this.controller = controller;
        System.out.println("RegisterController connected");

    }

    public MainWindow getParentWindow() {
        return parent;
    }

    public String getUsernameInput(){
        return userField.getText().trim();
    }

    public String getEmailInput(){
        return emailField.getText().trim();
    }

    public String getPasswordInput(){
        String value = new String(passField.getPassword());
        return value.equals(PASSWORD_PLACEHOLDER) ? "" : value;
    }
    public String getConfirmPasswordInput(){
        String value = new String(confirmPassField.getPassword());
        return value.equals(CONFIRM_PASSWORD_PLACEHOLDER) ? "" : value;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void clearFields() {
        userField.setText("");
        emailField.setText("");
        resetPasswordPlaceholder(passField, PASSWORD_PLACEHOLDER);
        resetPasswordPlaceholder(confirmPassField, CONFIRM_PASSWORD_PLACEHOLDER);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, BRAND_PINK, getWidth(), getHeight(), BRAND_ORANGE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // helpers
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150, 48));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(BRAND_PINK);
            btn.setBorder(BorderFactory.createLineBorder(
                    new Color(BRAND_PINK.getRed(), BRAND_PINK.getGreen(), BRAND_PINK.getBlue(), 190), 2, true));
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
        tf.setForeground(Color.LIGHT_GRAY);
        tf.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        tf.setText(placeholder);

        // auto-clear on click
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.LIGHT_GRAY);
                }
            }
        });

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

    private void setupPasswordPlaceholder(JPasswordField field, String placeholder) {
        resetPasswordPlaceholder(field, placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('*');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(field.getPassword()).trim();
                if (current.isEmpty()) {
                    resetPasswordPlaceholder(field, placeholder);
                }
            }
        });
    }

    private void resetPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.LIGHT_GRAY);
        field.setEchoChar((char) 0);
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BRAND_PINK, getWidth(), 0,
                        new Color(180, 80, 0));
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(CONTROL_WIDTH, 56));
        return btn;
    }

    public void scrollToTop() {
        if (cardScroll == null) {
            return;
        }
        cardScroll.getVerticalScrollBar().setValue(0);
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