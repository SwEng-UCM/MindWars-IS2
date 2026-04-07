package command;

import model.AnswerResult;
import model.GameModel;
import player.Player;

/**
 * Reversible wrapper around {@link GameModel#submitAnswer(String, long)}.
 * On {@link #execute()} it takes a snapshot of the current player's
 * answer-related state and then delegates to the model; on {@link #undo()}
 * it restores that snapshot so the player can re-answer the same question.
 */
public class AnswerCommand implements Command {

    private final GameModel model;
    private final String rawAnswer;
    private final long elapsedMs;

    // Snapshot
    private int playerIndex;
    private int oldScore;
    private int oldCorrect;
    private int oldWrong;
    private long oldTimer;
    private boolean oldRoundCorrect;
    private long oldRoundTime;

    // Side-effect of execute, exposed to the caller
    private AnswerResult result;

    public AnswerCommand(GameModel model, String rawAnswer, long elapsedMs) {
        this.model = model;
        this.rawAnswer = rawAnswer;
        this.elapsedMs = elapsedMs;
    }

    @Override
    public void execute() {
        playerIndex = model.getCurrentPlayerIndex();
        Player p = model.getCurrentPlayer();
        oldScore = p.getScore();
        oldCorrect = p.getCorrectAnswers();
        oldWrong = p.getWrongAnswers();
        oldTimer = p.getTimer();
        oldRoundCorrect = model.getRoundCorrect(playerIndex);
        oldRoundTime = model.getRoundTime(playerIndex);

        result = model.submitAnswer(rawAnswer, elapsedMs);
    }

    @Override
    public void undo() {
        Player p = model.getPlayers().get(playerIndex);
        p.setScore(oldScore);
        p.setCorrectAnswers(oldCorrect);
        p.setWrongAnswers(oldWrong);
        p.setTimer(oldTimer);
        p.popLastResponseTime();
        model.setRoundCorrect(playerIndex, oldRoundCorrect);
        model.setRoundTime(playerIndex, oldRoundTime);
    }

    /** Returns the result produced by {@link #execute()}. */
    public AnswerResult getResult() {
        return result;
    }

    @Override
    public String describe() {
        return "Answer by player " + playerIndex;
    }
}
