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
    public final String player3Name;
    public final String player4Name;
    public final boolean randomMode;
    public final String category; // null if random mode
    public final String difficulty; // null if random mode
    public final int numPlayers;
    public final String botDifficulty; // easy medium hard (relevant when vsBot = true)

    public GameSettings(int mapSize, boolean vsBot,
            String player1Name, String player2Name,
            String player3Name, String player4Name,
            boolean randomMode, String category, String difficulty,
            int numPlayers, String botDifficulty) {
        this.mapSize = mapSize;
        this.vsBot = vsBot;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player3Name = player3Name;
        this.player4Name = player4Name;
        this.randomMode = randomMode;
        this.category = category;
        this.difficulty = difficulty;
        this.numPlayers = numPlayers;
        this.botDifficulty = (botDifficulty == null || botDifficulty.isBlank()) ? "Easy" : botDifficulty;
    }

    // constructor
    public GameSettings(int mapSize, boolean vsBot,
            String player1Name, String player2Name,
            String player3Name, String player4Name,
            boolean randomMode, String category, String difficulty, int numPlayers) {
        this(mapSize, vsBot, player1Name, player2Name, player3Name, player4Name,
                randomMode, category, difficulty, numPlayers, "Easy");
    }

}
