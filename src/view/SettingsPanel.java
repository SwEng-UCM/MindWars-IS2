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

    private GradientButton saveButton;

    private JButton backButton;

    public SettingsPanel(AudioSettings settings, SoundManager soundManager) {
        this.settings = settings;
        this.soundManager = soundManager;

        setLayout(new BorderLayout());

   
        setBackground(new Color(245, 221, 230));

        
        initializeComponents();


        layoutComponents();

       
        registerListeners();
    }

    /*
      This method creates all the UI components
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

        saveButton = new GradientButton("Save  →");

        backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
    }

  
    private void layoutComponents() {
     
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

       
        JPanel contentPanel = new JPanel();

        // This panel should paint its own background normally
        contentPanel.setOpaque(true);

        // Very light background for the main card/screen area
        contentPanel.setBackground(new Color(250, 250, 250));

        // Fixed size for consistent appearance
        contentPanel.setPreferredSize(new Dimension(720, 760));

       
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setBorder(new EmptyBorder(35, 40, 35, 40));

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

        iconCircle.setOpaque(false);

        iconCircle.setPreferredSize(new Dimension(92, 92));
        iconCircle.setMaximumSize(new Dimension(92, 92));
        iconCircle.setMinimumSize(new Dimension(92, 92));

        
        iconCircle.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel("⚙");
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        iconLabel.setForeground(Color.WHITE);
        iconCircle.add(iconLabel);

        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

   
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(20, 20, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

   
        JLabel subtitleLabel = new JLabel("Choose your game audio preferences");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(110, 110, 110));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        soundEffectsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backPanel.setOpaque(false);
        backPanel.add(backButton);

    
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