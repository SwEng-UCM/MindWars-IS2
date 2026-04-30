package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import util.AudioSettings;
import util.SoundManager;

public class SettingsPanel extends JPanel {

    private final AudioSettings settings;

    private final SoundManager soundManager;

    private SettingOptionCard soundEffectsCard;

    private SettingOptionCard musicCard;

    private JButton saveButton;

    private JButton backButton;

    public SettingsPanel(AudioSettings settings, SoundManager soundManager) {
        this.settings = settings;
        this.soundManager = soundManager;

        setLayout(new BorderLayout());
        setOpaque(false);

        initializeComponents();

        layoutComponents();

        registerListeners();
    }

    /*
     * This method creates all the UI components
     */
    private void initializeComponents() {

        soundEffectsCard = new SettingOptionCard(
                "Sound Effects",
                "Enable or disable game sound effects",
                settings.isSoundEffectsEnabled());

        musicCard = new SettingOptionCard(
                "Music",
                "Enable or disable background music",
                settings.isMusicEnabled());

        saveButton = null;
        backButton = null;
    }

    private void layoutComponents() {

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(380, 520));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(MindWarsTheme.PINK);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        iconCircle.setOpaque(false);

        iconCircle.setPreferredSize(new Dimension(72, 72));
        iconCircle.setMaximumSize(new Dimension(72, 72));
        iconCircle.setMinimumSize(new Dimension(72, 72));

        iconCircle.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel("⚙");
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        iconLabel.setForeground(Color.WHITE);
        iconCircle.add(iconLabel);

        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(MindWarsTheme.HEADING_FONT);
        titleLabel.setForeground(MindWarsTheme.PINK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your game audio preferences");
        subtitleLabel.setFont(MindWarsTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(MindWarsTheme.GRAY_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        soundEffectsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton save = MindWarsTheme.createGradientButton("Save Settings");
        saveButton = save;
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = MindWarsTheme.createPinkButton("Back to Menu");
        backButton = back;
        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.add(backButton);

        contentPanel.add(iconCircle);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(22));
        contentPanel.add(soundEffectsCard);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(musicCard);
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(saveButton);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(backPanel);

        centerWrapper.add(contentPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void registerListeners() {
        saveButton.addActionListener(e -> {

            settings.setSoundEffectsEnabled(soundEffectsCard.isSelectedOption());
            settings.setMusicEnabled(musicCard.isSelectedOption());

            soundManager.refreshAudioState();

            if (settings.isMusicEnabled()) {
                soundManager.startBackground();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Settings saved successfully.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public JButton getBackButton() {
        return backButton;
    }
}