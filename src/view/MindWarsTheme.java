package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Centralized theme constants and utility methods for the MindWars GUI.
 * Two themes: LIGHT (menus/setup - pink gradient) and DARK (gameplay - dark bg).
 */
public final class MindWarsTheme {

    private MindWarsTheme() {}

    // ── Brand colors ──
    public static final Color PINK        = new Color(0xE91E8C);
    public static final Color PINK_LIGHT  = new Color(0xF8BBD0);
    public static final Color PINK_BG     = new Color(0xFCE4EC);
    public static final Color ORANGE      = new Color(0xC87137);
    public static final Color ORANGE_DARK = new Color(0xA0522D);
    public static final Color WHITE       = Color.WHITE;
    public static final Color DARK_BG     = new Color(0x1E1E1E);
    public static final Color DARK_CARD   = new Color(0x2D2D2D);
    public static final Color DARK_BORDER = new Color(0x444444);
    public static final Color GRAY_TEXT   = new Color(0x888888);
    public static final Color GRAY_LIGHT  = new Color(0xCCCCCC);
    public static final Color PLAYER_X    = PINK;
    public static final Color PLAYER_O    = ORANGE;
    public static final Color FOG_COLOR   = new Color(0xBDBDBD);
    public static final Color EMPTY_CELL  = new Color(0xF5F5F5);
    public static final Color CORRECT_GREEN = new Color(0x4CAF50);
    public static final Color WRONG_RED     = new Color(0xF44336);

    // ── Fonts ──
    public static final Font TITLE_FONT    = new Font("SansSerif", Font.BOLD, 32);
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font HEADING_FONT  = new Font("SansSerif", Font.BOLD, 22);
    public static final Font BODY_FONT     = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD     = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BUTTON_FONT   = new Font("SansSerif", Font.BOLD, 16);
    public static final Font TIMER_FONT    = new Font("SansSerif", Font.BOLD, 28);
    public static final Font SMALL_FONT    = new Font("SansSerif", Font.PLAIN, 12);

    // ── Dimensions ──
    public static final int FRAME_WIDTH  = 500;
    public static final int FRAME_HEIGHT = 750;
    public static final int CARD_RADIUS  = 20;
    public static final int BUTTON_RADIUS = 12;

    // ── Gradient background panel (pink top-left to orange bottom-right) ──
    public static JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0xF8BBD0),
                        getWidth(), getHeight(), new Color(0xCC7832));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    // ── White rounded card panel ──
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        return card;
    }

    // ── Dark card panel (for gameplay screens) ──
    public static JPanel createDarkCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DARK_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2.setColor(DARK_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        return card;
    }

    // ── Gradient button (pink to orange) ──
    public static JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, PINK,
                        getWidth(), 0, ORANGE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                g2.dispose();

                // Draw text
                FontMetrics fm = g.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.setFont(getFont());
                g.setColor(WHITE);
                g.drawString(getText(), x, y);
            }
        };
        styleButton(btn);
        return btn;
    }

    // ── Pink solid button ──
    public static JButton createPinkButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PINK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                g2.dispose();

                FontMetrics fm = g.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.setFont(getFont());
                g.setColor(WHITE);
                g.drawString(getText(), x, y);
            }
        };
        styleButton(btn);
        return btn;
    }

    // ── Outlined option card (selectable, with pink border when selected) ──
    public static JPanel createOptionCard(String title, String subtitle, boolean selected) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected) {
                    g2.setColor(PINK_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(PINK);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                } else {
                    g2.setColor(WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(GRAY_LIGHT);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_BOLD);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        if (subtitle != null && !subtitle.isEmpty()) {
            panel.add(Box.createVerticalStrut(4));
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(SMALL_FONT);
            subLabel.setForeground(GRAY_TEXT);
            subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(subLabel);
        }

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return panel;
    }

    // ── Styled text field ──
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(GRAY_LIGHT);
                    g2.setFont(BODY_FONT);
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left + 2, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(300, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRAY_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        return field;
    }

    // ── Back button (plain text with arrow) ──
    public static JButton createBackButton() {
        JButton btn = new JButton("<html>&larr; Back</html>");
        btn.setFont(BODY_FONT);
        btn.setForeground(Color.BLACK);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Timer progress bar (gradient fill) ──
    public static JProgressBar createTimerBar() {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(GRAY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Fill
                int fillWidth = (int) (getWidth() * (getValue() / 100.0));
                if (fillWidth > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, PINK, fillWidth, 0, ORANGE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, fillWidth, getHeight(), 8, 8);
                }
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(400, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setBorderPainted(false);
        bar.setOpaque(false);
        bar.setValue(100);
        return bar;
    }

    // ── Helper: style a button ──
    private static void styleButton(JButton btn) {
        btn.setFont(BUTTON_FONT);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(360, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ── Helper: centered label ──
    public static JLabel centeredLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
