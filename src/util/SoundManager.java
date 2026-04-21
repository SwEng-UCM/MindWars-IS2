package util;

import javax.sound.sampled.*;
import java.io.File;


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

    private final AudioSettings settings;

    public SoundManager(AudioSettings settings) {
        this.settings = settings;
    }

    public SoundManager() {
        this(new AudioSettings());
    }

    public AudioSettings getSettings() {
        return settings;
    }

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