package util;

/**
 * Stores user-configurable audio settings.
 */
public class AudioSettings {

    private boolean soundEffectsEnabled;
    private boolean musicEnabled;

    public AudioSettings() {
        this.soundEffectsEnabled = true;
        this.musicEnabled = true;
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        this.soundEffectsEnabled = soundEffectsEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }
}
