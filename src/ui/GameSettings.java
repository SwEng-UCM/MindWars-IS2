package ui;

/**
 * Stores user-configurable settings for the game UI.
 * This class can later be extended with more options
 * such as difficulty, theme, volume, etc.
 */
public class GameSettings {

    private boolean soundEffectsEnabled;
    private boolean musicEnabled;

    public GameSettings() {
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