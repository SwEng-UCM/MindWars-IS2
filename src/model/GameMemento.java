package model;

import java.util.ArrayList;
import java.util.List;
import trivia.Question;

/**
 * Memento (GoF) for {@link GameModel}. A flat, immutable-by-convention
 * snapshot of the parts of the game state that need to survive a save/load
 * cycle. Built with public fields and a no-arg constructor so Gson can
 * serialise it without custom adapters.
 *
 * <p>The memento captures stable round-level state. Restoration always
 * resumes at {@link GamePhase#HOT_SEAT_PASS} for the saved current
 * player, so timer / question-in-flight state does not need to be
 * persisted.
 */
public final class GameMemento {

    /** Schema version. Bump on incompatible structural changes. */
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public String savedAt;

    public GameSettings settings;

    public int roundIndex;
    public int currentPlayerIndex;
    public int currentWager;

    public int mapSize;
    public String mapOwners;
    public List<Boolean> mapBonus;

    public List<PlayerSnap> players = new ArrayList<>();
    public List<Question> roundQuestions = new ArrayList<>();

    /** Per-player flat snapshot. */
    public static final class PlayerSnap {
        public String name;
        public char symbol;
        public int score;
        public int streak;
        public int coins;
        public int bonusTokens;
        public int correctAnswers;
        public int wrongAnswers;
        public boolean isBot;
        public String botDifficulty;
        public List<String> weapons = new ArrayList<>();
    }
}
