package ui;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Plays WAV sound effects asynchronously using javax.sound.sampled.
 * If a sound file is missing or playback fails, the game continues silently.
 */
public class SoundManager {

    private static final String ASSETS_DIR = "assets/";

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

    private final GameSettings settings;

    public SoundManager(GameSettings settings) {
        this.settings = settings;
    }

    /**
     * Optional compatibility constructor if other existing code
     * still creates SoundManager with no arguments.
     */
    public SoundManager() {
        this(new GameSettings());
    }

    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Plays a one-shot sound effect asynchronously.
     */
    public void play(String soundFileName) {
        if (!settings.isSoundEffectsEnabled()) {
            return;
        }

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
                // silently ignore
            }
        }).start();
    }

    /**
     * Starts looping background music.
     */
    public void startBackground() {
        if (!settings.isMusicEnabled()) {
            return;
        }

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
            backgroundClip.start();
        } catch (Exception e) {
            // silently ignore
        }
    }

    public void stopBackground() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }
    }

    /**
     * Starts looping timer sound.
     * Treated as sound effect, not music.
     */
    public void startTimer() {
        if (!settings.isSoundEffectsEnabled()) {
            return;
        }

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
            timerClip.start();
        } catch (Exception e) {
            // silently ignore
        }
    }

    public void stopTimer() {
        if (timerClip != null) {
            timerClip.stop();
            timerClip.close();
            timerClip = null;
        }
    }

    /**
     * Call this after settings change so currently playing sounds
     * react immediately.
     */
    public void refreshAudioState() {
        if (!settings.isMusicEnabled()) {
            stopBackground();
        }

        if (!settings.isSoundEffectsEnabled()) {
            stopTimer();
        }
    }

    public void setMuted(boolean muted) {
        settings.setSoundEffectsEnabled(!muted);
        settings.setMusicEnabled(!muted);
        refreshAudioState();
    }

}