package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A simple Swing settings screen for toggling sound effects and music.
 * This screen updates a shared GameSettings object so it can be integrated
 * with the rest of the UI later.
 */
public class SettingsScreen extends JFrame {

    private final GameSettings settings;
    private final SoundManager soundManager;

    private JCheckBox soundEffectsCheckBox;
    private JCheckBox musicCheckBox;
    private JButton saveButton;
    private JButton backButton;

    public SettingsScreen(GameSettings settings, SoundManager soundManager) {
        this.settings = settings;
        this.soundManager = soundManager;

        initializeFrame();
        initializeComponents();
        layoutComponents();
        registerListeners();
    }

    private void initializeFrame() {
        setTitle("MindWars - Settings");
        setSize(420, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void initializeComponents() {
        soundEffectsCheckBox = new JCheckBox("Enable Sound Effects");
        soundEffectsCheckBox.setSelected(settings.isSoundEffectsEnabled());
        soundEffectsCheckBox.setFont(new Font("Arial", Font.PLAIN, 16));
        soundEffectsCheckBox.setFocusPainted(false);

        musicCheckBox = new JCheckBox("Enable Music");
        musicCheckBox.setSelected(settings.isMusicEnabled());
        musicCheckBox.setFont(new Font("Arial", Font.PLAIN, 16));
        musicCheckBox.setFocusPainted(false);

        saveButton = new JButton("Save");
        saveButton.setFocusPainted(false);

        backButton = new JButton("Back");
        backButton.setFocusPainted(false);
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1, 10, 10));
        centerPanel.add(soundEffectsCheckBox);
        centerPanel.add(musicCheckBox);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.add(saveButton);
        bottomPanel.add(backButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void registerListeners() {
        saveButton.addActionListener(e -> saveSettings());

        backButton.addActionListener(e -> dispose());
    }

    private void saveSettings() {
        settings.setSoundEffectsEnabled(soundEffectsCheckBox.isSelected());
        settings.setMusicEnabled(musicCheckBox.isSelected());

        soundManager.refreshAudioState();

        JOptionPane.showMessageDialog(
                this,
                "Settings saved successfully.",
                "Settings",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}