package player;

public class Player {
    private String name;
    private int score;
    private long time;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.time = 0;
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

    public long getTime() {return time;}

    public void addTime(long ms) {this.time += ms;}

    public String formatTime() {
        double seconds = time / 1000.0;
        return String.format("%.3f s", seconds);
    }
}