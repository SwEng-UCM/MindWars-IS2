/*
 * @author Leopold Popper
 * AI-assisted: yes (Claude by Anthropic, via Claude Code)
 * @author Ioannis Stogiannaris
 * AI-assisted: yes (ChatGPT)
 */
package util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


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

    /**
     * Open an audio stream for {@code soundFileName}, looking first inside
     * the JAR (classpath resource at {@code /<name>}), then on disk under
     * {@code assets/}. Returns {@code null} if neither is available.
     */
    private AudioInputStream openAudio(String soundFileName) throws UnsupportedAudioFileException, IOException {
        InputStream in = SoundManager.class.getResourceAsStream("/" + soundFileName);
        if (in != null) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        }
        File file = new File(ASSETS_DIR + soundFileName);
        if (file.exists()) {
            return AudioSystem.getAudioInputStream(file);
        }
        return null;
    }

    public void play(String soundFileName) {
        if (!settings.isSoundEffectsEnabled()) {
            return;
        }

        new Thread(() -> {
            try {
                AudioInputStream stream = openAudio(soundFileName);
                if (stream == null) {
                    return;
                }
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

        try {
            AudioInputStream stream = openAudio(BACKGROUND);
            if (stream == null) {
                return;
            }
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

        try {
            AudioInputStream stream = openAudio(TIMER);
            if (stream == null) {
                return;
            }
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
