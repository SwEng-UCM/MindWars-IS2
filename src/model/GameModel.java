package model;

import bot.EasyBot;
import bot.HardBot;
import bot.MediumBot;
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
import java.util.Arrays;

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
    private double[] numericGuesses;

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
        if (settings == null) {
            return roundQuestions.size();
        }
        return calculateRoundQuestionCount();
    }

    public Question getCurrentQuestion() {
        ensureQuestionForRound(roundIndex);
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
            if (settings.vsBot && i == 1) {
                p.setStrategy(new EasyBot());
            }
            players.add(p);
        }

        if (settings.vsBot && players.size() >= 2) {
            String diff = settings.botDifficulty == null ? "Easy" : settings.botDifficulty;
            bot.BotStrategy strategy = switch (diff) {
                case "Medium" -> new MediumBot();
                case "Hard" -> new HardBot();
                default -> new EasyBot();
            };
            players.get(1).setStrategy(strategy);
        }

        this.map = new MapGrid(settings.mapSize);
        for (Player p : players) {
            map.initVisibilityForPlayer(p.getSymbol());
        }

        this.roundCorrect = new boolean[players.size()];
        this.roundTimes = new long[players.size()];
        this.numericGuesses = new double[players.size()];
        Arrays.fill(this.numericGuesses, Double.NaN);

        loadQuestions();
        setPhase(GamePhase.HOT_SEAT_PASS);
    }

    private void loadQuestions() {
        int count = calculateRoundQuestionCount();
        for (int i = 0; i < count; i++) {
            Question q = pickQuestion();
            if (q != null)
                roundQuestions.add(q);
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

        // for NUMERIC questions also record the raw guess so that
        // determineRoundWinnerIndex() can apply "closest wins" logic
        if (q.getType() == trivia.QuestionType.NUMERIC && !timedOut) {
            try {
                numericGuesses[currentPlayerIndex] = Double.parseDouble(rawAnswer.trim());
            } catch (NumberFormatException ignored) {
                numericGuesses[currentPlayerIndex] = Double.NaN;
            }
        }

        pcs.firePropertyChange(PROP_SCORES, null, players);

        String correctAnswer;
        if (q.getType() == trivia.QuestionType.ORDERING && q.getOrderingAnswer() != null) {
            correctAnswer = String.join(" → ", q.getOrderingAnswer());
        } else if (q.getType() == trivia.QuestionType.NUMERIC) {
            double num = q.getNumericAnswer();
            correctAnswer = (num == Math.floor(num))
                    ? String.valueOf((long) num)
                    : String.valueOf(num);
        } else {
            correctAnswer = q.getAnswer();
        }
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
        currentPlayerIndex = 0;
        setPhase(GamePhase.TERRITORY_CLAIM);
    }

    // ── Territory phase ──

    /**
     * Calculates how many cells each player can claim this round.
     * The best player gets the most claims, and the rest are shared
     * among the other players based on their ranking.
     */
    public int[] roundClaimCounts() {
        int size = map.getSize();
        int playerCount = players.size();

        int [] claims = new int[playerCount];

        List<Integer> ranking= new ArrayList<>();


        for (int i = 0; i < playerCount; i++) {
            ranking.add(i);
        }

        ranking.sort((a,b) ->{
            if (roundCorrect[a] != roundCorrect[b]) {
                return roundCorrect[a] ? - 1 : 1;
            }
            return Long.compare(roundTimes[a], roundTimes[b]);

        });

        int remainingClaims = size;
        int winnerClaims = size/ 2+1;
        claims[ranking.getFirst()] = winnerClaims;
        remainingClaims -= winnerClaims;


        int rank = 1;
        while (remainingClaims > 0 ) {
            claims[ranking.get(rank)]++;
            remainingClaims--;
            rank++;

            if (rank >= ranking.size()) {
                rank = 1; //keeps distributing among winners
            }
        }
        return claims;

    }

    public int determineRoundWinnerIndex() {
        // for NUMERIC questions use "closest to target wins
        Question q = getCurrentQuestion();
        if (q != null && q.getType() == trivia.QuestionType.NUMERIC) {
            return determineNumericWinnerIndex(q.getNumericAnswer());
        }

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

    private int determineNumericWinnerIndex(double target) {
        java.util.List<game.NumericWinnerCalculator.EstimationResponse> responses = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            double guess = numericGuesses[i];
            if (!Double.isNaN(guess)) {
                long time = roundTimes[i] == Long.MAX_VALUE ? Long.MAX_VALUE : roundTimes[i];
                responses.add(new game.NumericWinnerCalculator.EstimationResponse(
                        players.get(i), guess, time));
            }
        }
        if (responses.isEmpty()) {
            return 0; // everyone timed out — default to first player
        }
        player.Player winner = game.NumericWinnerCalculator.calculateWinner(target, responses);
        if (winner == null)
            return 0;
        int idx = players.indexOf(winner);
        return idx < 0 ? 0 : idx;
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
        numericGuesses = new double[players.size()];
        Arrays.fill(numericGuesses, Double.NaN);

        if (map.isMapFull()) {
            setPhase(GamePhase.GAME_OVER);
            return;
        }

        roundIndex++;
        currentPlayerIndex = 0;
        pcs.firePropertyChange(PROP_CURRENT_PLAYER, null, getCurrentPlayer());
        pcs.firePropertyChange(PROP_ROUND, null, roundIndex);
        setPhase(GamePhase.HOT_SEAT_PASS);
    }

    private int calculateRoundQuestionCount() {
        if (settings == null) {
            return roundQuestions.size();
        }
        return estimateTotalRounds(settings.mapSize);
    }

    public static int estimateTotalRounds(int mapSize) {
        int totalCells = Math.max(1, mapSize * mapSize);
        int claimsPerRound = totalClaimsPerRound(mapSize);
        if (claimsPerRound <= 0) {
            return totalCells;
        }
        return Math.max(1, (int) Math.ceil((double) totalCells / claimsPerRound));
    }

    private static int totalClaimsPerRound(int mapSize) {
        return mapSize;
    }

    private Question pickQuestion() {
        if (settings == null) {
            return null;
        }
        if (settings.randomMode) {
            List<String> categories = new ArrayList<>(questionBank.getCategories());
            if (categories.isEmpty()) {
                return null;
            }
            for (int attempt = 0; attempt < 8; attempt++) {
                String cat = categories.get(random.nextInt(categories.size()));
                List<String> diffs = new ArrayList<>(questionBank.getDifficulties(cat));
                if (diffs.isEmpty()) {
                    continue;
                }
                String diff = diffs.get(random.nextInt(diffs.size()));
                Question q = questionBank.getQuestion(cat, diff);
                if (q != null) {
                    return q;
                }
            }
            return null;
        }
        return questionBank.getQuestion(settings.category, settings.difficulty);
    }

    private void ensureQuestionForRound(int index) {
        if (index < 0) {
            return;
        }
        while (roundQuestions.size() <= index) {
            Question q = pickQuestion();
            if (q == null) {
                return;
            }
            roundQuestions.add(q);
        }
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

    // ── Memento (save/load) ──

    /**
     * Captures the current game state into a {@link GameMemento}. The
     * caller (caretaker) is responsible for persisting it.
     */
    public GameMemento createMemento() {
        GameMemento m = new GameMemento();
        m.savedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        m.settings = settings;
        m.roundIndex = roundIndex;
        m.currentPlayerIndex = currentPlayerIndex;
        m.currentWager = currentWager;

        m.mapSize = map.getSize();
        StringBuilder owners = new StringBuilder(m.mapSize * m.mapSize);
        m.mapBonus = new ArrayList<>(m.mapSize * m.mapSize);
        for (int r = 0; r < m.mapSize; r++) {
            for (int c = 0; c < m.mapSize; c++) {
                owners.append(map.getOwner(r, c));
                m.mapBonus.add(map.hasBonus(r, c));
            }
        }
        m.mapOwners = owners.toString();

        for (Player p : players) {
            GameMemento.PlayerSnap s = new GameMemento.PlayerSnap();
            s.name = p.getName();
            s.symbol = p.getSymbol();
            s.score = p.getScore();
            s.streak = p.getStreak();
            s.coins = p.getCoins();
            s.bonusTokens = p.getBonusTokens();
            s.correctAnswers = p.getCorrectAnswers();
            s.wrongAnswers = p.getWrongAnswers();
            s.isBot = p.isBot();
            if (p.isBot() && p.getStrategy() != null) {
                s.botDifficulty = p.getStrategy().getDifficultyName();
            }
            for (game.WeaponType w : p.getInventory()) {
                s.weapons.add(w.name());
            }
            m.players.add(s);
        }

        m.roundQuestions = new ArrayList<>(roundQuestions);
        return m;
    }

    /**
     * Replaces the current game state with the contents of {@code m}.
     * Resumes at {@link GamePhase#HOT_SEAT_PASS} for the saved current
     * player, so the user re-presses Ready to continue.
     */
    public void restoreFromMemento(GameMemento m) {
        this.settings = m.settings;
        this.players.clear();
        this.roundIndex = m.roundIndex;
        this.currentPlayerIndex = m.currentPlayerIndex;
        this.currentWager = m.currentWager;

        char[] symbols = { 'X', 'O', 'A', 'B' };
        for (int i = 0; i < m.players.size(); i++) {
            GameMemento.PlayerSnap s = m.players.get(i);
            Player p = new Player(s.name);
            p.setSymbol(s.symbol == 0 ? symbols[i] : s.symbol);
            p.setScore(s.score);
            p.setStreakRaw(s.streak);
            p.setCoins(s.coins);
            for (int b = 0; b < s.bonusTokens; b++)
                p.addBonusToken();
            p.setCorrectAnswers(s.correctAnswers);
            p.setWrongAnswers(s.wrongAnswers);
            if (s.isBot && s.botDifficulty != null) {
                bot.BotStrategy strat = switch (s.botDifficulty) {
                    case "Medium" -> new MediumBot();
                    case "Hard" -> new HardBot();
                    default -> new EasyBot();
                };
                p.setStrategy(strat);
            }
            if (s.weapons != null) {
                for (String wName : s.weapons) {
                    try {
                        p.addWeapon(game.WeaponType.valueOf(wName));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            players.add(p);
        }

        this.map = new MapGrid(m.mapSize);
        for (Player p : players) {
            map.initVisibilityForPlayer(p.getSymbol());
        }
        for (int r = 0; r < m.mapSize; r++) {
            for (int c = 0; c < m.mapSize; c++) {
                int idx = r * m.mapSize + c;
                if (idx < m.mapOwners.length()) {
                    char owner = m.mapOwners.charAt(idx);
                    if (owner != '.') {
                        map.setOwner(r, c, owner);
                        map.revealNeighbourForPlayer(owner, r, c);
                    }
                }
            }
        }

        this.roundQuestions = new ArrayList<>(m.roundQuestions);
        this.roundCorrect = new boolean[players.size()];
        this.roundTimes = new long[players.size()];

        pcs.firePropertyChange(PROP_ROUND, null, roundIndex);
        pcs.firePropertyChange(PROP_SCORES, null, players);
        pcs.firePropertyChange(PROP_MAP, null, map);
        setPhase(GamePhase.HOT_SEAT_PASS);
    }

    public void updateBotStrategy(String difficulty) {
        if (players.size() > 1 && players.get(1).isBot()) {
            bot.BotStrategy strategy = switch (difficulty) {
                case "Medium" -> new bot.MediumBot();
                case "Hard" -> new bot.HardBot();
                default -> new bot.EasyBot();
            };
            players.get(1).setStrategy(strategy);
        }
    }
}
