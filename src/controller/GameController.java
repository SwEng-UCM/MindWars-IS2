package controller;

import command.AnswerCommand;
import command.ClaimCellCommand;
import command.CommandHistory;
import command.InvasionCommand;
import model.AnswerResult;
import model.GameModel;
import model.GamePhase;
import model.GameSettings;

/**
 * The Controller in MVC. Receives view events (button clicks, cell clicks,
 * answer submissions) and translates them into model operations. It holds no
 * game state of its own — the model is the single source of truth.
 *
 * <p>Views should not call the model directly; they should call methods on
 * this controller. Views observe the model via {@code PropertyChangeListener}
 * and re-render themselves when the phase changes.
 */
public class GameController {

    private final GameModel model;
    private final NavigationController nav;
    private final CommandHistory history = new CommandHistory();

    public GameController(GameModel model, NavigationController nav) {
        this.model = model;
        this.nav = nav;
    }

    public GameModel getModel() { return model; }
    public NavigationController getNav() { return nav; }
    public CommandHistory getHistory() { return history; }

    // ── Entry points from menu/setup ──

    public void startNewGame(GameSettings settings) {
        history.clear();
        model.startGame(settings);
        nav.showGame();
    }

    public void returnToMenu() {
        nav.showMainMenu();
    }

    // ── Gameplay events ──

    /** The hot-seat "Press ENTER when ready" button was pressed. */
    public void onHotSeatReady() {
        GamePhase phase = model.getPhase();
        if (phase == GamePhase.HOT_SEAT_PASS) {
            model.beginQuestion();
        } else if (phase == GamePhase.INVASION_PASS) {
            model.beginInvasionSelect();
        }
    }

    /**
     * The view submits an answer. Wraps the call in an {@link AnswerCommand}
     * so the player can undo it while the feedback is on screen (#82).
     * Returns the result so the view can flash correct/wrong feedback
     * before calling {@link #onAnswerAcknowledged()}.
     */
    public AnswerResult onAnswerSubmitted(String rawAnswer, long elapsedMs) {
        AnswerCommand cmd = new AnswerCommand(model, rawAnswer, elapsedMs);
        cmd.execute();
        history.push(cmd);
        return cmd.getResult();
    }

    public void onAnswerAcknowledged() {
        model.advanceAfterAnswer();
    }

    /** The player clicked a cell during the territory claim phase. */
    public boolean onCellClaimed(int playerIndex, int row, int col) {
        ClaimCellCommand cmd = new ClaimCellCommand(model, playerIndex, row, col);
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

    public void onInvasionResolved(String attackerAnswer, String defenderAnswer) {
        InvasionCommand cmd = new InvasionCommand(model, attackerAnswer, defenderAnswer);
        cmd.execute();
        history.push(cmd);
        if (model.getPhase() == GamePhase.GAME_OVER) {
            nav.showGameOver();
        }
    }

    // ── Undo (#82) ──

    /** Whether there is an action on the history stack that can be undone. */
    public boolean canUndo() {
        return history.canUndo();
    }

    /** Undoes the most recent action. Returns true if something was undone. */
    public boolean undoLast() {
        return history.undo();
    }

    // ── Game over ──

    public void onGameOverAcknowledged() {
        nav.showMainMenu();
    }
}
