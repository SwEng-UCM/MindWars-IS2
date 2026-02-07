package trivia;

/**
 * PURPOSE:
 * - Decides if a player's raw input is correct for a given Question.
 * - Pure logic: returns true/false.
 *
 * @TODO (MVP):
 *       - Normalize player input:
 *       - trim whitespace
 *       - uppercase
 *       - MCQ:
 *       - accept A/B/C/D
 *       - compare to question.correctAnswer
 *       - TRUE/FALSE:
 *       - accept T/F
 *       - optionally accept "TRUE"/"FALSE"
 *
 * @TODO (later):
 *       - Accept numeric input for MCQ (1-4)
 *       - Better error messages (currently GameEngine can handle messaging)
 */
public class AnswerValidator {

    public AnswerValidator() {
        // no state needed for MVP
    }

    public boolean isCorrect(Question q, String rawAnswer) {
        // @TODO
        return false;
    }
}