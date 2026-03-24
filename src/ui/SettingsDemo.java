package ui;

// SwingUtilities is used to start Swing code on the Event Dispatch Thread
import javax.swing.*;

/*
 * This class is only for testing the SettingsPanel in isolation.
 * It allows me to run and preview the settings screen
 * without needing the whole application to be finished.
 */
public class SettingsDemo {

    public static void main(String[] args) {
        /*
         * Swing UI should be created on the Event Dispatch Thread.
         * SwingUtilities.invokeLater ensures that.
         */
        SwingUtilities.invokeLater(() -> {
            // Create the shared settings object used by the panel
            GameSettings settings = new GameSettings();

            // Create the sound manager that reacts to settings changes
            SoundManager soundManager = new SoundManager(settings);

            // Create a frame just for demo/testing purposes
            JFrame frame = new JFrame("MindWars Settings Demo");

            // Close the app when the window is closed
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Set the demo window size
            frame.setSize(1000, 850);

            // Center the window on screen
            frame.setLocationRelativeTo(null);

            // Put the SettingsPanel inside the frame
            frame.setContentPane(new SettingsPanel(settings, soundManager));

            // Show the window
            frame.setVisible(true);
        });
    }
}