package player;

public class Player {
    private String name;
    private int score;
    private long timer;
    private int streak;
    private boolean hasUsedBet = false;
    private static final int STREAK_TARGET = 3;
    public static final int STREAK_BONUS = 3;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.timer = 0;
        this.streak = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int score) {
        streak++;
        addScore(score);

        if (streak == STREAK_TARGET) {
            addScore(STREAK_BONUS);
        }

    }

    public void resetStreak() {
        streak = 0;
    }

    public boolean hasUsedBet() {
        return hasUsedBet;
    }

    public void setHasUsedBet(boolean hasUsedBet) {
        this.hasUsedBet = hasUsedBet;
    }
}