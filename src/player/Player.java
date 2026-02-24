package player;

public class Player {
    private String name;
    private int score;
    private long timer;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.timer = 0;
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
}