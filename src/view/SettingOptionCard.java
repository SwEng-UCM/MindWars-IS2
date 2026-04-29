package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingOptionCard extends JPanel {

    // Main title shown on the card
    private final String title;

    // Secondary description shown under the title
    private final String description;

    // This is now the only source of truth for whether the card is selected
    private boolean selected;

    public SettingOptionCard(String title, String description, boolean initialState) {
        this.title = title;
        this.description = description;
        this.selected = initialState;

        // I want to draw the card myself, so I make it non-opaque
        setOpaque(false);

        // BorderLayout lets me place text nicely inside the card
        setLayout(new BorderLayout());

        // Internal spacing so the text does not touch the edges
        setBorder(new EmptyBorder(14, 18, 14, 18));

        // Fixed size for consistent card appearance
        setPreferredSize(new Dimension(360, 96));
        setMaximumSize(new Dimension(360, 96));

        // Hand cursor to show that the whole card is clickable
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Main title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);

        // Description label
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(MindWarsTheme.BODY_FONT);
        descLabel.setForeground(MindWarsTheme.GRAY_TEXT);

        // Text container for vertical stacking
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(descLabel);

        // Add text panel to the card center
        add(textPanel, BorderLayout.CENTER);

        // Make the whole card clickable
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selected = !selected;
                repaint();
            }
        });
    }

    // Allows the parent screen to read the current state
    public boolean isSelectedOption() {
        return selected;
    }

    // Allows the parent screen to set the state programmatically
    public void setSelectedOption(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 26;

        Color fillColor = Color.WHITE;
        Color borderColor = selected ? MindWarsTheme.PINK : MindWarsTheme.GRAY_LIGHT;

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.setStroke(new BasicStroke(selected ? 2.2f : 1.4f));
        g2.setColor(borderColor);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}