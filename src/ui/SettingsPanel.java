package ui;

// Swing components like JPanel, JLabel, JButton, JOptionPane
import javax.swing.*;
// EmptyBorder is used to create inner padding
import javax.swing.border.EmptyBorder;
// AWT is used for layout, colors, fonts, drawing and alignment
import java.awt.*;

/*
 * This class represents the full Settings screen.
 *
 * It is responsible for:
 * - showing the title and subtitle
 * - displaying the Sound Effects and Music cards
 * - showing the Save and Back buttons
 * - reading/writing values from GameSettings
 * - notifying SoundManager when settings change
 *
 * This panel can later be placed inside a larger application frame,
 * for example using CardLayout.
 */
public class SettingsPanel extends JPanel {

    // Shared settings model used by the rest of the application
    private final GameSettings settings;

    // Shared sound manager that will react to settings changes
    private final SoundManager soundManager;

    // Card for toggling sound effects
    private SettingOptionCard soundEffectsCard;

    // Card for toggling music
    private SettingOptionCard musicCard;

    // Main primary action button
    private GradientButton saveButton;

    // Optional back button for navigation
    private JButton backButton;

    /*
     * Constructor:
     * The panel receives the shared settings object and sound manager,
     * so it can both display and update the app state.
     */
    public SettingsPanel(GameSettings settings, SoundManager soundManager) {
        this.settings = settings;
        this.soundManager = soundManager;

        /*
         * BorderLayout gives me a simple outer layout.
         * I place the main content in the center.
         */
        setLayout(new BorderLayout());

        /*
         * Soft pink background inspired by the Figma screenshot.
         * This acts as the outer page background.
         */
        setBackground(new Color(245, 221, 230));

        // Build the components of the screen
        initializeComponents();

        // Place them visually on the screen
        layoutComponents();

        // Add button actions and save logic
        registerListeners();
    }

    /*
     * This method creates all the UI components
     * but does not place them on the screen yet.
     */
    private void initializeComponents() {
        /*
         * Create the first option card.
         * Its initial state comes from the shared GameSettings object.
         */
        soundEffectsCard = new SettingOptionCard(
                "Sound Effects",
                "Enable or disable game sound effects",
                settings.isSoundEffectsEnabled());

        /*
         * Create the second option card for music.
         */
        musicCard = new SettingOptionCard(
                "Music",
                "Enable or disable background music",
                settings.isMusicEnabled());

        /*
         * Create the main Save button using the custom gradient component.
         */
        saveButton = new GradientButton("Save  →");

        /*
         * Create a simple back button.
         * Later another controller or screen manager can connect it to navigation.
         */
        backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
    }

    /*
     * This method is responsible for the full visual arrangement of the screen.
     */
    private void layoutComponents() {
        /*
         * Center wrapper:
         * I use GridBagLayout here because it makes it easy
         * to center a single large content panel on the page.
         */
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        /*
         * This is the main white/light content panel in the middle,
         * similar to the big central container in the Figma design.
         */
        JPanel contentPanel = new JPanel();

        // This panel should paint its own background normally
        contentPanel.setOpaque(true);

        // Very light background for the main card/screen area
        contentPanel.setBackground(new Color(250, 250, 250));

        // Fixed size for consistent appearance
        contentPanel.setPreferredSize(new Dimension(720, 760));

        /*
         * BoxLayout Y_AXIS lets me stack everything vertically:
         * icon, title, subtitle, cards, button, back button
         */
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Inner spacing around all content
        contentPanel.setBorder(new EmptyBorder(35, 40, 35, 40));

        /*
         * This custom panel draws the pink circle icon area at the top.
         * I override paintComponent to draw a filled circle.
         */
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Pink circle matching the design accent
                g2.setColor(new Color(255, 18, 145));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        // Transparent outer panel so only the circle is visible
        iconCircle.setOpaque(false);

        // Fixed circular area size
        iconCircle.setPreferredSize(new Dimension(92, 92));
        iconCircle.setMaximumSize(new Dimension(92, 92));
        iconCircle.setMinimumSize(new Dimension(92, 92));

        /*
         * GridBagLayout is used here to center the icon symbol
         * inside the circle.
         */
        iconCircle.setLayout(new GridBagLayout());

        /*
         * I use a gear symbol to visually indicate "Settings".
         * Later this could be replaced with an actual icon image.
         */
        JLabel iconLabel = new JLabel("⚙");
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        iconLabel.setForeground(Color.WHITE);
        iconCircle.add(iconLabel);

        // Center the circle inside the vertical BoxLayout
        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        /*
         * Main screen title.
         */
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(20, 20, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /*
         * Small subtitle under the title.
         */
        JLabel subtitleLabel = new JLabel("Choose your game audio preferences");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(110, 110, 110));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /*
         * Keep the cards and button horizontally centered
         * inside the BoxLayout container.
         */
        soundEffectsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        /*
         * Small panel for the Back button.
         * FlowLayout LEFT keeps the button aligned to the left.
         */
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backPanel.setOpaque(false);
        backPanel.add(backButton);

        /*
         * Now I add everything in vertical order to the content panel.
         * Box.createVerticalStrut(...) adds fixed spacing between elements.
         * Box.createVerticalGlue() pushes later elements downward.
         */
        contentPanel.add(iconCircle);
        contentPanel.add(Box.createVerticalStrut(22));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(34));
        contentPanel.add(soundEffectsCard);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(musicCard);
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(saveButton);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(backPanel);

        /*
         * Put the whole content panel in the center wrapper,
         * and then place that wrapper in the center of this screen.
         */
        centerWrapper.add(contentPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }

    /*
     * This method connects the UI actions with the application logic.
     */
    private void registerListeners() {
        saveButton.addActionListener(e -> {

            // Παίρνω τις τιμές από τα cards και τις περνάω στο settings model
            settings.setSoundEffectsEnabled(soundEffectsCard.isSelectedOption());
            settings.setMusicEnabled(musicCard.isSelectedOption());

            // Ενημερώνω τον SoundManager ώστε να σταματήσει ήχους αν χρειάζεται
            soundManager.refreshAudioState();

            // Αν η μουσική είναι ενεργοποιημένη, ξεκινάει (ή ξαναξεκινάει)
            if (settings.isMusicEnabled()) {
                soundManager.startBackground();
            }

            // Μήνυμα επιβεβαίωσης στον χρήστη
            JOptionPane.showMessageDialog(
                    this,
                    "Settings saved successfully.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /*
     * Getter for the Back button.
     * This allows another class, such as a controller or main frame,
     * to attach navigation behavior without hardcoding it here.
     */
    public JButton getBackButton() {
        return backButton;
    }
}