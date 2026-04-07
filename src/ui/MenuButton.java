package ui;

import javax.swing.*;
import java.awt.*;

public class MenuButton extends JButton {
    private final Color buttonColor;

    public MenuButton(String text, Color buttonColor) {
        super(text);
        this.buttonColor = buttonColor;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 22));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setPreferredSize(new Dimension(620, 72));
        setMaximumSize(new Dimension(620, 72));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = getModel().isPressed() ? buttonColor.darker() : buttonColor;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

        g2.dispose();
        super.paintComponent(g);
    }
}
