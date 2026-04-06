package controller;

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

    public GameController(GameModel model, NavigationController nav) {
        this.model = model;
        this.nav = nav;
    }

    public GameModel getModel() { return model; }
    public NavigationController getNav() { return nav; }

    // ── Entry points from menu/setup ──

    public void startNewGame(GameSettings settings) {
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
     * The view submits an answer. Returns the result so the view can flash
     * correct/wrong feedback before calling {@link #onAnswerAcknowledged()}.
     */
    public AnswerResult onAnswerSubmitted(String rawAnswer, long elapsedMs) {
        return model.submitAnswer(rawAnswer, elapsedMs);
    }

    public void onAnswerAcknowledged() {
        model.advanceAfterAnswer();
    }

    /** The player clicked a cell during the territory claim phase. */
    public boolean onCellClaimed(int playerIndex, int row, int col) {
        return model.claimCell(playerIndex, row, col);
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
        model.resolveInvasion(attackerAnswer, defenderAnswer);
        if (model.getPhase() == GamePhase.GAME_OVER) {
            nav.showGameOver();
        }
    }

    // ── Game over ──

    public void onGameOverAcknowledged() {
        nav.showMainMenu();
    }
}
