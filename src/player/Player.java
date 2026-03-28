package player;

import game.Weapon;
import game.WeaponType;
import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private int score;
    private long timer;
    private long answerTimeMs;
    private int streak;
    private boolean hasUsedBet = false;
    private boolean hasUsedBonus = false;
    private char symbol;
    private int bonusTokens = 0;
    private static final int STREAK_TARGET = 3;
    public static final int STREAK_BONUS = 3;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private List<Long> responseTimes = new ArrayList<>();
    private List<WeaponType> inventory = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.timer = 0;
        this.answerTimeMs = 0;
        this.streak = 0;
        this.symbol = ' ';
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

    public void setScore(int score) {
        this.score = score;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public long getAnswerTimeMs() {
        return answerTimeMs;
    }

    public void setAnswerTimeMs(long answerTimeMs) {
        this.answerTimeMs = answerTimeMs;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int score) {
        streak++;
        addScore(score);

        if (streak >= STREAK_TARGET) {
            addScore(STREAK_BONUS);
        }
    }

    public void subtractScore(int points) {
        this.score -= points;
        if (this.score < 0)
            this.score = 0;
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

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public void addBonusToken() {
        this.bonusTokens++;
    }

    public int getBonusTokens() {
        return bonusTokens;
    }

    public void useBonusToken() {
        if (bonusTokens > 0) {
            bonusTokens--;
        }
    }

    public boolean hasUsedBonus() {
        return hasUsedBonus;
    }

    public void setHasUsedBonus(boolean useBonus) {
        this.hasUsedBonus = useBonus;
    }

    public void addCorrectAnswer(long responseTimeMs) {
        correctAnswers++;
        responseTimes.add(responseTimeMs);
    }

    public void addWrongAnswer(long responseTimeMs) {
        wrongAnswers++;
        responseTimes.add(responseTimeMs);
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public double getAverageResponseTime() {
        if (responseTimes.isEmpty())
            return 0;
        long sum = 0;
        for (long t : responseTimes)
            sum += t;
        return sum / (double) responseTimes.size() / 1000.0; // secondes
    }

    public double getFastestResponse() {
        if (responseTimes.isEmpty())
            return 0;
        long min = Long.MAX_VALUE;
        for (long t : responseTimes)
            if (t < min)
                min = t;
        return min / 1000.0; // secondes
    }

    public void addWeapon(WeaponType weapon) {
        inventory.add(weapon);
    }

    public boolean hasWeapon(WeaponType weapon) {
        return inventory.contains(weapon);
    }

    public void useWeapon(WeaponType weapon) {
        inventory.remove(weapon);
    }

    public List<WeaponType> getInventory() {
        return new ArrayList<>(inventory);
    }

    public List<WeaponType> getAttackWeapon() {
        List<WeaponType> attackWeapons = new ArrayList<>();
        for (WeaponType w : getInventory()) {
            switch (w) {
                case CANNON, CROSSBOW, BURST -> attackWeapons.add(w);
            }
        }
        return attackWeapons;
    }

    public List<WeaponType> getDefendWeapon() {
        List<WeaponType> defenseWeapons = new ArrayList<>();
        for (WeaponType w : getInventory()) {
            switch (w) {
                case SHIELD, LASER_SIGHT, HELMET -> defenseWeapons.add(w);
            }
        }
        return defenseWeapons;
    }

    public boolean hasAttackWeapon() {
        for (WeaponType weapon : inventory) {
            switch (weapon) {
                case CANNON:
                case CROSSBOW:
                case BURST:
                    return true;
            }
        }
        return false;
    }

    public boolean hasDefendWeapon() {
        for (WeaponType weapon : inventory) {
            switch (weapon) {
                case SHIELD:
                case LASER_SIGHT:
                case HELMET:
                    return true;
            }
        }
        return false;
    }
}
