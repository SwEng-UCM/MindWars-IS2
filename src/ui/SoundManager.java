package ui;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Plays WAV sound effects asynchronously using javax.sound.sampled.
 * If a sound file is missing or playback fails, the game continues silently.
 */
public class SoundManager {

    private static final String ASSETS_DIR = "assets/";

    // Sound file constants — add matching .wav files to assets/
    public static final String CORRECT = "CORRECT.wav";
    public static final String INCORRECT = "INCORRECT.wav";
    public static final String LIGHTNING = "LIGHTNING.wav";
    public static final String VICTORY = "VICTORY.wav";
    public static final String GAME_START = "GAME_START.wav";
    public static final String TERRITORY = "TERRITORY.wav";
    public static final String BACKGROUND = "BACKGROUND.wav";
    public static final String TIMER = "TIMER.wav";

    private Clip backgroundClip;
    private Clip timerClip;
    private boolean isMuted = false; // Default is not muted

    /**
     * Plays a WAV sound file asynchronously (non-blocking).
     * If the file doesn't exist or playback fails, it is silently ignored.
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        // If we mute while music is playing, stop the music immediately
        if (isMuted) {
            stopBackground();
            stopTimer();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void play(String soundFileName) {
        if (isMuted)
            return; // Skip if muted
        File file = new File(ASSETS_DIR + soundFileName);
        if (!file.exists()) {
            return;
        }

        new Thread(() -> {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception e) {
                // silently ignore — game should never break due to sound
            }
        }).start();
    }

    /**
     * Starts background music on a continuous loop.
     * Call stopBackground() to stop it.
     */
    public void startBackground() {
        if (isMuted)
            return; // Skip if muted
        stopBackground();
        File file = new File(ASSETS_DIR + BACKGROUND);
        if (!file.exists()) {
            return;
        }

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(stream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // silently ignore
        }
    }

    /**
     * Stops background music if currently playing.
     */
    public void stopBackground() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }
    }

    /**
     * Starts the timer ticking sound on loop.
     * The tick plays continuously until stopTimer() is called
     * (when the player answers or time runs out).
     */
    public void startTimer() {
        if (isMuted)
            return;
        stopTimer();
        File file = new File(ASSETS_DIR + TIMER);
        if (!file.exists()) {
            return;
        }

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            timerClip = AudioSystem.getClip();
            timerClip.open(stream);
            timerClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // silently ignore
        }
    }

    /**
     * Stops the timer ticking sound.
     */
    public void stopTimer() {
        if (timerClip != null && timerClip.isRunning()) {
            timerClip.stop();
            timerClip.close();
            timerClip = null;
        }
    }
}
