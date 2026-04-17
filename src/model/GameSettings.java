package model;

/**
 * Value object holding the user's choices from the setup wizard. The
 * controller builds one of these and hands it to {@link GameModel#startGame}.
 */
public final class GameSettings {
    public final int mapSize;
    public final boolean vsBot;
    public final String player1Name;
    public final String player2Name;
    public final boolean randomMode;
    public final String category; // null if random mode
    public final String difficulty; // null if random mode
    public final int numPlayers;

    public GameSettings(int mapSize, boolean vsBot,
            String player1Name, String player2Name,
            boolean randomMode, String category, String difficulty, int numPlayers) {
        this.mapSize = mapSize;
        this.vsBot = vsBot;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.randomMode = randomMode;
        this.category = category;
        this.difficulty = difficulty;
        this.numPlayers = numPlayers;
    }
}
