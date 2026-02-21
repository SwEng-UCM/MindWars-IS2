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

    public static boolean isValidAnswer(Question q, String rawAnswer) {
        if (q == null || rawAnswer == null) return false;
        String input = rawAnswer.trim().toUpperCase();

        if (q.getType() == QuestionType.TRUE_FALSE) {
            return input.equals("T") || input.equals("F")
                || input.equals("TRUE") || input.equals("FALSE");
        }

        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            // accept A-D letters or 1-4 numbers based on number of choices
            int numChoices = q.getChoices().size();
            for (int i = 0; i < numChoices; i++) {
                if (input.equals(String.valueOf((char) ('A' + i)))
                    || input.equals(String.valueOf(i + 1))) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    public static boolean isCorrect(Question q, String rawAnswer) {
        if (q == null || rawAnswer == null) {
            return false;
        }

        // normalize the input of the players
        String playerAnswer = rawAnswer.trim().toUpperCase();
        String correctAnswer = q.getCorrectAnswer().trim().toUpperCase();

        //TRUE or FALSE question
        if (q.getType() == QuestionType.TRUE_FALSE) {
            if (playerAnswer.equals("TRUE")) playerAnswer = "T";
            if (playerAnswer.equals("FALSE")) playerAnswer = "F";
        }

        // Multiple Choice question
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            if (playerAnswer.equals("1")) playerAnswer = "A";
            if (playerAnswer.equals("2")) playerAnswer = "B";
            if (playerAnswer.equals("3")) playerAnswer = "C";
            if (playerAnswer.equals("4")) playerAnswer = "D";
        }

        return playerAnswer.equals(correctAnswer);
    }
}
