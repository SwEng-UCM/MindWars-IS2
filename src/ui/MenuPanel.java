package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends JPanel {

    private MainWindow parent;
    private BufferedImage logoImage;

    public MenuPanel(MainWindow parent) {
        this.parent = parent;
        setLayout(new GridBagLayout()); // Center components both vertically & horizontally
        setBorder(new EmptyBorder(50, 50, 50, 50)); // External padding around the central box

        // Load the logo from assets folder
        try {
            logoImage = ImageIO.read(new File("assets/logo.png"));
        } catch (Exception e) {
            System.err.println("Could not load logo.png from assets folder. Make sure the path is correct.");
        }

        // --- Main White Container with Rounded Corners ---
        JPanel whiteContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw white background with rounded corners and a subtle drop shadow effect
                g2d.setColor(new Color(255, 255, 255, 240)); // Nearly opaque white
                g2d.fill(new RoundRectangle2D.Double(10, 10, getWidth() - 20, getHeight() - 20, 40, 40));

                // Optional: Draw a subtle inner border
                g2d.setColor(new Color(0, 0, 0, 15)); // Very faint shadow
                g2d.draw(new RoundRectangle2D.Double(10, 10, getWidth() - 20, getHeight() - 20, 40, 40));

                g2d.dispose();
            }
        };
        whiteContainer.setLayout(new GridBagLayout()); // Arrange components vertically inside
        whiteContainer.setOpaque(false); // Make transparent so we only see the drawn rounded rectangle
        whiteContainer.setPreferredSize(new Dimension(500, 400)); // Size of the central panel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // --- 1. Large Logo as Title ---
        gbc.gridy = 0;
        gbc.weighty = 0.5; // Give significant vertical space to the logo title
        JLabel logoLabel = new JLabel();

        // Check if logo loaded and scale it to be large, like a title
        if (logoImage != null) {
            // Get original logo size
            int originalW = logoImage.getWidth();
            int originalH = logoImage.getHeight();

            // Set large target dimensions (e.g., matching old text's visual impact)
            int targetW = 300;
            int targetH = 150;

            // Scaling logic to maintain aspect ratio
            double scaleFactor = Math.min((double) targetW / originalW, (double) targetH / originalH);
            int newW = (int) (originalW * scaleFactor);
            int newH = (int) (originalH * scaleFactor);

            // Create scaled ImageIcon
            Image scaledImage = logoImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // Fallback text if logo fails
            logoLabel.setText("MINDWARS (Image missing)");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
            logoLabel.setForeground(new Color(136, 23, 91));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        whiteContainer.add(logoLabel, gbc);

        // --- 2. Welcome Text ---
        gbc.gridy = 1;
        gbc.weighty = 0.05; // Less vertical space needed here
        JLabel welcomeLabel = new JLabel("Welcome! Start to continue");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeLabel.setForeground(new Color(119, 119, 119)); // Grey text
        whiteContainer.add(welcomeLabel, gbc);

        // --- 3. Custom Stylized Start Button ---
        gbc.gridy = 2;
        gbc.weighty = 0.3; // Give space for the button at the bottom
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make button stretch horizontally
        JButton startButton = createCustomButton("START");
        startButton.addActionListener(e -> parent.showScreen("GAME")); // Change this to your MVC action later
        whiteContainer.add(startButton, gbc);

        // Add the white container to the main MenuPanel
        add(whiteContainer);
    }

    // --- Modern Gradient Background Painting (Magenta to Orange) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(213, 58, 137), w, h, new Color(223, 103, 31));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }

    // --- Helper to create the custom modern button ---
    private JButton createCustomButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Modern Orange to Red gradient for the button itself
                GradientPaint gp = new GradientPaint(0, 0, new Color(223, 103, 31), w, h, new Color(180, 20, 20));
                g2d.setPaint(gp);

                // Rounded rectangle for the button shape
                g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 25, 25));

                // Draw the text/icon over the gradient
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };

        // Button styling
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); // Crucial for custom painting
        btn.setBorderPainted(false); // Hide standard Swing border
        btn.setFocusPainted(false); // Hide focus ring
        btn.setPreferredSize(new Dimension(300, 55)); // Set a good button size
        btn.setIconTextGap(15); // Gap between icon and text

        return btn;
    }
}