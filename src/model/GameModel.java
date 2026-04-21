package model;

import bot.EasyBot;
import game.GameState;
import game.MapGrid;
import game.WinnerCalculator;
import player.Player;
import trivia.AnswerValidator;
import trivia.Question;
import trivia.QuestionBank;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Model in MVC.
 *
 * <p>
 * Holds all game state (players, grid, questions, current phase) and
 * exposes operations that mutate it. It never imports Swing and never reads
 * or writes to the console. Views observe it through
 * {@link PropertyChangeListener}s and the controller drives it through the
 * public methods below.
 *
 * <p>
 * Note: this is a GUI-focused model. It does not yet cover every feature
 * of the original console {@code game.Game} (wagers, numeric estimation,
 * bonuses, streaks, lightning bonus). Those can be layered in progressively
 * without touching the view/controller contracts.
 */
public class GameModel {

    // ── Property names for PropertyChangeSupport ──
    public static final String PROP_PHASE = "phase";
    public static final String PROP_CURRENT_PLAYER = "currentPlayer";
    public static final String PROP_QUESTION = "question";
    public static final String PROP_ROUND = "round";
    public static final String PROP_MAP = "map";
    public static final String PROP_SCORES = "scores";

    public static final long TIME_LIMIT_MS = 15_000;
    private static final int POINTS_EASY = 10;
    private static final int POINTS_MEDIUM = 20;
    private static final int POINTS_HARD = 30;

    private final QuestionBank questionBank;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Random random = new Random();

    // ── Game state ──
    private GameSettings settings;
    private final List<Player> players = new ArrayList<>();
    private MapGrid map;
    private List<Question> roundQuestions = new ArrayList<>();
    private int roundIndex;
    private int currentPlayerIndex;
    private GamePhase phase = GamePhase.SETUP;
    private long questionStartMs;
    private int currentWager = 0;
    // Tracks the results of the two players for the current round, used to
    // decide who claims how many cells in the territory phase.
    private boolean[] roundCorrect;
    private long[] roundTimes;

    // Invasion phase state
    private int invaderIndex;
    private int attackFromRow = -1, attackFromCol = -1;
    private int attackToRow = -1, attackToCol = -1;

    public GameModel(QuestionBank questionBank) {
        this.questionBank = questionBank;
    }

    // ── Observer wiring ──
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // ── Accessors ──
    public QuestionBank getQuestionBank() {
        return questionBank;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public MapGrid getMap() {
        return map;
    }

    public int getRoundNumber() {
        return roundIndex + 1;
    }

    public int getTotalRounds() {
        return roundQuestions.size();
    }

    public Question getCurrentQuestion() {
        if (roundQuestions.isEmpty() || roundIndex >= roundQuestions.size())
            return null;
        return roundQuestions.get(roundIndex);
    }

    public GameSettings getSettings() {
        return settings;
    }

    public long getQuestionStartMs() {
        return questionStartMs;
    }

    public int getInvaderIndex() {
        return invaderIndex;
    }

    public int getAttackFromRow() {
        return attackFromRow;
    }

    public int getAttackFromCol() {
        return attackFromCol;
    }

    public int getAttackToRow() {
        return attackToRow;
    }

    public int getAttackToCol() {
        return attackToCol;
    }

    // ── Round-tracker access (used by undo Commands) ──
    public boolean getRoundCorrect(int playerIndex) {
        return roundCorrect[playerIndex];
    }

    public long getRoundTime(int playerIndex) {
        return roundTimes[playerIndex];
    }

    public void setRoundCorrect(int playerIndex, boolean v) {
        roundCorrect[playerIndex] = v;
    }

    public void setRoundTime(int playerIndex, long v) {
        roundTimes[playerIndex] = v;
    }

    public void setCurrentPlayerIndex(int i) {
        this.currentPlayerIndex = i;
    }

    public void forcePhase(GamePhase p) {
        setPhase(p);
    }

    public void setInvaderIndex(int i) {
        this.invaderIndex = i;
    }

    // ── Game lifecycle ──

    /** Starts a brand new game using the given settings. */
    public void startGame(GameSettings settings) {
        this.settings = settings;
        this.players.clear();
        this.roundQuestions.clear();
        this.roundIndex = 0;
        this.currentPlayerIndex = 0;

        char[] symbols = { 'X', 'O', 'A', 'B' };
        String[] names = {
                settings.player1Name,
                settings.player2Name,
                (settings.player3Name == null || settings.player3Name.isBlank()) ? "Player 3" : settings.player3Name,
                (settings.player4Name == null || settings.player4Name.isBlank()) ? "Player 4" : settings.player4Name
        };

        for (int i = 0; i < settings.numPlayers; i++) {
            Player p = new Player(names[i]);
            p.setSymbol(symbols[i]);
            players.add(p);
        }

        if (settings.vsBot && players.size() >= 2) {
            players.get(1).setStrategy(new EasyBot());
        }

        this.map = new MapGrid(settings.mapSize);
        for (Player p : players) {
            map.initVisibilityForPlayer(p.getSymbol());
        }

        this.roundCorrect = new boolean[players.size()];
        this.roundTimes = new long[players.size()];

        loadQuestions();
        setPhase(GamePhase.HOT_SEAT_PASS);
    }

    private void loadQuestions() {
        int count = settings.mapSize; // same rule as console version
        if (settings.randomMode) {
            List<String> categories = new ArrayList<>(questionBank.getCategories());
            for (int i = 0; i < count && !categories.isEmpty(); i++) {
                String cat = categories.get(random.nextInt(categories.size()));
                List<String> diffs = new ArrayList<>(questionBank.getDifficulties(cat));
                if (diffs.isEmpty()) {
                    i--;
                    continue;
                }
                String diff = diffs.get(random.nextInt(diffs.size()));
                Question q = questionBank.getQuestion(cat, diff);
                if (q != null)
                    roundQuestions.add(q);
                else
                    i--;
            }
        } else {
            for (int i = 0; i < count; i++) {
                Question q = questionBank.getQuestion(settings.category, settings.difficulty);
                if (q != null)
                    roundQuestions.add(q);
            }
        }
    }

    // ── Question flow ──

    /** Called after the hot-seat pass screen; transitions to the question. */
    public void beginQuestion() {
        this.questionStartMs = System.currentTimeMillis();
        setPhase(GamePhase.QUESTION);
    }

    /**
     * Submits the current player's answer. Updates the player's score and
     * stats, records the round outcome, advances to the next player, and
     * either emits a new hot-seat transition or moves to territory claim.
     */
    public AnswerResult submitAnswer(String rawAnswer, long clientElapsedMs) {
        Question q = getCurrentQuestion();
        long elapsed = clientElapsedMs > 0
                ? clientElapsedMs
                : System.currentTimeMillis() - questionStartMs;

        boolean timedOut = rawAnswer == null;
        boolean correct = !timedOut && trivia.AnswerValidator.isCorrect(q, rawAnswer);

        Player p = getCurrentPlayer();

        int pts = basePoints(q);
        boolean isBettingActive = (currentWager > 0);

        if (isBettingActive) {
            pts = currentWager;
        }

        int delta;
        if (timedOut) {
            p.addWrongAnswer(elapsed);
            p.subtractScore(pts);
            delta = -pts;
        } else if (correct) {
            p.addCorrectAnswer(elapsed);
            p.addScore(pts);
            delta = pts;
        } else {
            p.addWrongAnswer(elapsed);
            if (isBettingActive) {
                p.subtractScore(pts);
                delta = -pts;
            } else {
                delta = 0;
            }
        }

        this.currentWager = 0;

        p.setTimer(p.getTimer() + elapsed);
        roundCorrect[currentPlayerIndex] = correct;
        roundTimes[currentPlayerIndex] = timedOut ? Long.MAX_VALUE : elapsed;

        pcs.firePropertyChange(PROP_SCORES, null, players);

        String correctAnswer = q.getAnswer();
        AnswerResult result = new AnswerResult(correct, timedOut, delta,
                correctAnswer == null ? "" : correctAnswer, elapsed);

        return result;
    }

    /** Called by the controller after showing answer feedback to move on. */
    public void advanceAfterAnswer() {
        if (currentPlayerIndex < players.size() - 1) {
            currentPlayerIndex++;
            pcs.firePropertyChange(PROP_CURRENT_PLAYER, null, getCurrentPlayer());
            setPhase(GamePhase.HOT_SEAT_PASS);
            return;
        }
        setPhase(GamePhase.TERRITORY_CLAIM);
    }

    // ── Territory phase ──

    /**
     * Returns the order in which players should claim cells for the current
     * round: the round winner is first. Each entry is a player index and how
     * many cells they claim.
     */
    public int[] roundClaimCounts() {
        int size = map.getSize();
        int winnerClaims = size / 2 + 1;
        int loserClaims = size / 2;

        int winnerIdx = determineRoundWinnerIndex();
        int[] out = new int[players.size()];

        for (int i = 0; i < players.size(); i++) {
            out[i] = (i == winnerIdx) ? winnerClaims : loserClaims;
        }
        return out;
    }

    public int determineRoundWinnerIndex() {
        int bestIdx = 0;
        for (int i = 1; i < players.size(); i++) {
            boolean bestCorrect = roundCorrect[bestIdx];
            boolean iCorrect = roundCorrect[i];
            if (iCorrect && !bestCorrect) {
                bestIdx = i;
            } else if (iCorrect == bestCorrect) {
                if (roundTimes[i] < roundTimes[bestIdx]) {
                    bestIdx = i;
                }
            }
        }
        return bestIdx;
    }

    /**
     * Tries to claim the given cell for the given player. Returns true on
     * success. The controller/view is responsible for calling this in the
     * correct order (winner first, matching {@link #roundClaimCounts()}).
     */
    public boolean claimCell(int playerIndex, int row, int col) {
        Player p = players.get(playerIndex);
        char sym = p.getSymbol();
        if (!map.claimCell(sym, row, col))
            return false;
        map.revealCellForPlayer(sym, row, col);
        map.revealNeighbourForPlayer(sym, row, col);
        pcs.firePropertyChange(PROP_MAP, null, map);
        return true;
    }

    /**
     * Called after all claim slots for this round have been filled. Advances
     * to next round or to invasion phase or to game over.
     */
    public void finishRound() {
        roundCorrect = new boolean[players.size()];
        roundTimes = new long[players.size()];

        if (map.isMapFull()) {
            invaderIndex = 0;
            setPhase(GamePhase.INVASION_PASS);
            return;
        }
        roundIndex++;
        if (roundIndex >= roundQuestions.size()) {
            setPhase(GamePhase.GAME_OVER);
            return;
        }
        currentPlayerIndex = 0;
        pcs.firePropertyChange(PROP_CURRENT_PLAYER, null, getCurrentPlayer());
        pcs.firePropertyChange(PROP_ROUND, null, roundIndex);
        setPhase(GamePhase.HOT_SEAT_PASS);
    }

    // ── Invasion phase ──

    /** Called once the current attacker has been shown the pass screen. */
    public void beginInvasionSelect() {
        attackFromRow = attackFromCol = attackToRow = attackToCol = -1;
        setPhase(GamePhase.INVASION_SELECT);
    }

    public void setAttackFrom(int r, int c) {
        this.attackFromRow = r;
        this.attackFromCol = c;
    }

    public void setAttackTarget(int r, int c) {
        this.attackToRow = r;
        this.attackToCol = c;
        // Reuse round question pool: pop a fresh one.
        Question q = questionBank.getAllQuestionsAsList().isEmpty()
                ? null
                : questionBank.getAllQuestionsAsList().get(0);
        if (q != null) {
            // Shove the battle question into slot roundIndex so views reading
            // getCurrentQuestion() work uniformly.
            if (roundIndex >= roundQuestions.size()) {
                roundQuestions.add(q);
            } else {
                roundQuestions.set(roundIndex, q);
            }
        }
        questionStartMs = System.currentTimeMillis();
        setPhase(GamePhase.INVASION_BATTLE);
    }

    public Player getInvader() {
        return players.get(invaderIndex);
    }

    public Player getDefender() {
        for (int i = 0; i < players.size(); i++) {
            if (i != invaderIndex) {
                return players.get(i);
            }
        }
        return players.get(0);
    }

    /**
     * Resolves one invasion battle using the two answers provided and
     * advances invasion state.
     */
    public void resolveInvasion(String attackerAnswer, String defenderAnswer) {
        Question q = getCurrentQuestion();
        Player invader = getInvader();
        Player defender = getDefender();

        boolean attCorrect = attackerAnswer != null && AnswerValidator.isCorrect(q, attackerAnswer);
        boolean defCorrect = defenderAnswer != null && AnswerValidator.isCorrect(q, defenderAnswer);

        if (attCorrect && !defCorrect) {
            map.setOwner(attackToRow, attackToCol, invader.getSymbol());
            pcs.firePropertyChange(PROP_MAP, null, map);
        }

        invaderIndex++;
        if (invaderIndex >= players.size()) {
            setPhase(GamePhase.GAME_OVER);
        } else {
            setPhase(GamePhase.INVASION_PASS);
        }
    }

    // ── End-of-game ──

    public Player computeWinner() {
        return WinnerCalculator.getWinnerOrNull(players, map);
    }

    // ── Helpers ──

    private int basePoints(Question q) {
        String d = q.getDifficulty() == null ? "" : q.getDifficulty().toUpperCase();
        return switch (d) {
            case "HARD" -> POINTS_HARD;
            case "MEDIUM" -> POINTS_MEDIUM;
            default -> POINTS_EASY;
        };
    }

    private void setPhase(GamePhase newPhase) {
        GamePhase old = this.phase;
        this.phase = newPhase;
        pcs.firePropertyChange(PROP_PHASE, old, newPhase);
    }

    public void setCurrentWager(int wager) {
        this.currentWager = wager;
    }

    public int getCurrentWager() {
        return currentWager;
    }

}
