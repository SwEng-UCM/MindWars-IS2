package model;

/**
 * Immutable result of a player submitting an answer. Returned by
 * {@link GameModel#submitAnswer(String, long)} so the controller / view can
 * react without digging into model internals.
 */
public final class AnswerResult {
    public final boolean correct;
    public final boolean timedOut;
    public final int pointsDelta;
    public final String correctAnswer;
    public final long elapsedMs;

    public AnswerResult(boolean correct, boolean timedOut, int pointsDelta,
                        String correctAnswer, long elapsedMs) {
        this.correct = correct;
        this.timedOut = timedOut;
        this.pointsDelta = pointsDelta;
        this.correctAnswer = correctAnswer;
        this.elapsedMs = elapsedMs;
    }
}
