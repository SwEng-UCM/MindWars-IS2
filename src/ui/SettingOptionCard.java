package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingOptionCard extends JPanel {

    private final String title;
    private final String description;
    private boolean selected;
    private final JCheckBox toggle;

    public SettingOptionCard(String title, String description, boolean initialState) {
        this.title = title;
        this.description = description;
        this.selected = initialState;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 22, 18, 22));
        setPreferredSize(new Dimension(670, 110));
        setMaximumSize(new Dimension(670, 110));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        toggle = new JCheckBox();
        toggle.setSelected(initialState);
        toggle.setOpaque(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            selected = toggle.isSelected();
            repaint();
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(20, 20, 20));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(95, 95, 95));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(descLabel);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(toggle);

        add(textPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggle.setSelected(!toggle.isSelected());
                selected = toggle.isSelected();
                repaint();
            }
        });
    }

    public boolean isSelectedOption() {
        return toggle.isSelected();
    }

     public void setSelectedOption(boolean selected) {
        this.selected = selected;
        toggle.setSelected(selected);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 26;

        Color fillColor = new Color(252, 248, 250);
        Color borderColor = selected
                ? new Color(255, 44, 156)
                : new Color(210, 210, 210);

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.setStroke(new BasicStroke(selected ? 2.4f : 1.8f));
        g2.setColor(borderColor);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }



}