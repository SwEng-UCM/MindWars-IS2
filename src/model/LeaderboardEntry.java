package model;

/**
 * One row in the persistent leaderboard (#89). Tracks a player's wins,
 * total score across all games, and games played. Compared by wins, then
 * total score, then games played (fewer first — more efficient winner).
 */
public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String name;
    private int wins;
    private int totalScore;
    private int gamesPlayed;

    public LeaderboardEntry() {
    } // GSON

    public LeaderboardEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void recordGame(int scoreGained, boolean won) {
        this.gamesPlayed++;
        this.totalScore += scoreGained;
        if (won)
            this.wins++;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        int w = Integer.compare(o.wins, this.wins);
        if (w != 0)
            return w;
        int s = Integer.compare(o.totalScore, this.totalScore);
        if (s != 0)
            return s;
        return Integer.compare(this.gamesPlayed, o.gamesPlayed);
    }
}
