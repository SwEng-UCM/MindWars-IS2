package ui;

// JButton is the base Swing button class
import javax.swing.*;
// AWT is used for colors, painting, fonts, cursor, dimensions
import java.awt.*;

/*
 * This is a custom button component.
 * I created it because the default Swing button looks too plain
 * compared to the Figma-inspired design.
 *
 * Its main purpose is to provide:
 * - rounded corners
 * - gradient background
 * - modern typography
 */
public class GradientButton extends JButton {

    /*
     * Constructor:
     * I pass the button text when creating the button,
     * for example "Save →"
     */
    public GradientButton(String text) {
        super(text);

        /*
         * Remove the default Swing focus border
         * so the button looks cleaner.
         */
        setFocusPainted(false);

        /*
         * I do not want the default border,
         * because I will draw my own rounded shape.
         */
        setBorderPainted(false);

        /*
         * I disable the default filled background
         * because I will paint a gradient manually.
         */
        setContentAreaFilled(false);

        // White text matches the strong colored gradient background
        setForeground(Color.WHITE);

        // Larger bold font to look like a primary action button
        setFont(new Font("SansSerif", Font.BOLD, 22));

        // Hand cursor makes it feel clickable and modern
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Fixed size to match the Figma-like layout
        setPreferredSize(new Dimension(670, 62));
    }

    /*
     * Here I override the default painting
     * to draw a rounded gradient button.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Create a safe copy of the Graphics object
        Graphics2D g2 = (Graphics2D) g.create();

        // Smooth edges for rounded corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * Create a left-to-right gradient:
         * pink -> orange
         * which matches the style in the screenshot.
         */
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(255, 85, 170),
                getWidth(), 0, new Color(214, 104, 0));

        // Use the gradient as the paint source
        g2.setPaint(gp);

        // Draw the rounded button background
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

        /*
         * Let Swing paint the button text on top.
         */
        super.paintComponent(g2);

        // Clean up the copied graphics object
        g2.dispose();
    }
}