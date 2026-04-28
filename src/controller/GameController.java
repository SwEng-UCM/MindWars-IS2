package controller;

import command.AnswerCommand;
import command.ClaimCellCommand;
import command.CommandHistory;
import command.InvasionCommand;
import model.AnswerResult;
import model.GameMemento;
import model.GameMementoStore;
import model.GameModel;
import model.GamePhase;
import model.GameSettings;
import model.LeaderboardStore;
import player.Player;

import java.io.IOException;
import javax.swing.Timer;

/**
 * The Controller in MVC. Receives view events (button clicks, cell clicks,
 * answer submissions) and translates them into model operations. It holds no
 * game state of its own — the model is the single source of truth.
 *
 * <p>
 * Views should not call the model directly; they should call methods on
 * this controller. Views observe the model via {@code PropertyChangeListener}
 * and re-render themselves when the phase changes.
 */
public class GameController {

    private final GameModel model;
    private final NavigationController nav;
    private final CommandHistory history = new CommandHistory();
    private final LeaderboardStore leaderboard;
    private final GameMementoStore mementoStore = new GameMementoStore();
    private boolean leaderboardRecorded;
    private GameSettings lastSettings;
    private network.GameClient networkClient;

    public void setNetworkClient(network.GameClient client) {
        this.networkClient = client;
    }

    public GameController(GameModel model, NavigationController nav) {
        this(model, nav, new LeaderboardStore());
    }

    public GameController(
            GameModel model,
            NavigationController nav,
            LeaderboardStore leaderboard) {
        this.model = model;
        this.nav = nav;
        this.leaderboard = leaderboard;
    }

    public GameModel getModel() {
        return model;
    }

    public NavigationController getNav() {
        return nav;
    }

    public CommandHistory getHistory() {
        return history;
    }

    public LeaderboardStore getLeaderboard() {
        return leaderboard;
    }

    // ── Entry points from menu/setup ──

    public void startNewGame(GameSettings settings) {
        lastSettings = settings;
        history.clear();
        leaderboardRecorded = false;
        model.startGame(settings);
        nav.showGame();
    }

    /**
     * Records both players' results to the persistent leaderboard. Safe to
     * call multiple times — only the first call per game has an effect.
     */
    public void recordGameOnLeaderboard() {
        if (leaderboardRecorded)
            return;
        leaderboardRecorded = true;
        Player winner = model.computeWinner();
        for (Player p : model.getPlayers()) {
            boolean won = winner != null && winner == p;
            leaderboard.recordResult(p.getName(), p.getScore(), won);
        }
    }

    public void returnToMenu() {
        nav.showMainMenu();
    }

    // ── Gameplay events ──

    /**
     * If the current player is a bot and the current phase is a "press ready"
     * screen, auto-press the ready button after a short delay so the game
     * flows without human intervention on the bot's side.
     */
    public void processBotReadyIfNeeded() {
        Player cur = model.getCurrentPlayer();
        if (cur == null || !cur.isBot()) return;
        GamePhase phase = model.getPhase();
        if (phase != GamePhase.HOT_SEAT_PASS && phase != GamePhase.INVASION_PASS) return;
        Timer t = new Timer(700, e -> onHotSeatReady());
        t.setRepeats(false);
        t.start();
    }

    /** The hot-seat "Press ENTER when ready" button was pressed. */
    public void onHotSeatReady() {
        if (networkClient != null && networkClient.isConnected()) {
            networkClient.sendReady();
        } else {
            GamePhase phase = model.getPhase();
            if (phase == GamePhase.HOT_SEAT_PASS) {
                model.beginQuestion();
            } else if (phase == GamePhase.INVASION_PASS) {
                model.beginInvasionSelect();
            }
        }
    }

    public void onWagerConfirmed(int amount) {
        model.setCurrentWager(amount);

        model.beginQuestion();
        nav.showGame();
    }

    /**
     * The view submits an answer. Wraps the call in an {@link AnswerCommand}
     * so the player can undo it while the feedback is on screen (#82).
     * Returns the result so the view can flash correct/wrong feedback
     * before calling {@link #onAnswerAcknowledged()}.
     */

    public AnswerResult onAnswerSubmitted(String rawAnswer, long elapsedMs) {
        if (networkClient != null && networkClient.isConnected()) {
            networkClient.sendAnswer(rawAnswer, elapsedMs);
            return null;
        } else {
            AnswerCommand cmd = new AnswerCommand(model, rawAnswer, elapsedMs);
            cmd.execute();
            history.push(cmd);
            return cmd.getResult();
        }
    }

    public void onAnswerAcknowledged() {
        history.clear();
        model.advanceAfterAnswer();
    }

    /** The player clicked a cell during the territory claim phase. */
    public boolean onCellClaimed(int playerIndex, int row, int col) {
        ClaimCellCommand cmd = new ClaimCellCommand(
                model,
                playerIndex,
                row,
                col);
        cmd.execute();
        if (cmd.wasAccepted()) {
            history.push(cmd);
            return true;
        }
        return false;
    }

    public void onTerritoryPhaseFinished() {
        model.finishRound();
    }

    // ── Invasion phase ──

    public void onInvasionFromSelected(int r, int c) {
        model.setAttackFrom(r, c);
    }

    public void onInvasionTargetSelected(int r, int c) {
        model.setAttackTarget(r, c);
    }

    public void onInvasionResolved(
            String attackerAnswer,
            String defenderAnswer) {
        InvasionCommand cmd = new InvasionCommand(
                model,
                attackerAnswer,
                defenderAnswer);
        cmd.execute();
        history.push(cmd);
        if (model.getPhase() == GamePhase.GAME_OVER) {
            nav.showGameOver();
        }
    }

    // ── Undo (#82) ──

    /** Whether the top of the history stack is an answer that can be undone. */
    public boolean canUndo() {
        return history.canUndo() && history.peek() instanceof AnswerCommand;
    }

    /** Undoes the most recent action only if it is an AnswerCommand. Returns true if something was undone. */
    public boolean undoLast() {
        if (!canUndo()) return false;
        return history.undo();
    }

    // ── Game over ──

    public void onGameOverAcknowledged() {
        recordGameOnLeaderboard();
        nav.showMainMenu();
    }

    public void restartGame() {
        if (lastSettings == null)
            return;

        history.clear();
        leaderboardRecorded = false;

        model.startGame(lastSettings);
        nav.showGame();
    }

    // ── Save / load (Memento) ──

    public GameMementoStore getMementoStore() {
        return mementoStore;
    }

    /**
     * Captures the current game into the single save slot. Disallowed
     * in network mode (the host owns authoritative state).
     */
    public void saveGame() throws IOException {
        if (networkClient != null && networkClient.isConnected()) {
            throw new IOException("Saving is disabled in network games.");
        }
        mementoStore.save(model.createMemento());
    }

    /** Loads the saved slot into the model and shows the game screen. */
    public void loadGame() throws IOException {
        GameMemento m = mementoStore.load();
        if (m == null) throw new IOException("No save file found.");
        history.clear();
        leaderboardRecorded = false;
        lastSettings = m.settings;
        model.restoreFromMemento(m);
        nav.showGame();
    }
}
