package trivia;

import java.util.*;

/**
 * AnswerValidator provides utility methods to validate the format of user input
 * and check if the provided answer matches the correct one based on the QuestionType.
 */
public class AnswerValidator {

    /**
     * validates if the user's input is syntactically correct for the given question type
     * * @param q the question being answered
     * @param rawAnswer the raw string input from the player
     * @return true if the input can be processed, false otherwise
     */

    public static boolean isValidAnswer(Question q, String rawAnswer) {
        // basic null and empty check
        if (q == null || rawAnswer == null || rawAnswer.trim().isEmpty()) {
            return false;
        }

        String input = rawAnswer.trim().toUpperCase();

        // validation for NUMERIC type: must be a parsable number
        if (q.getType() == QuestionType.NUMERIC) {
            try {
                // normalize decimal separator to support both '.' and ','
                Double.parseDouble(input.replace(",", "."));
                return true;
            } catch (NumberFormatException e) {
                return false; // input is not a valid number
            }
        }

        // validation for TRUE_FALSE type: must be T, F, TRUE, or FALSE
        if (q.getType() == QuestionType.TRUE_FALSE) {
            return (
                input.equals("T") ||
                input.equals("F") ||
                input.equals("TRUE") ||
                input.equals("FALSE")
            );
        }

        // validation for MULTIPLE_CHOICE: must match available options (A-D or 1-4)
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            int numChoices = q.getChoices().size();
            // regex to match a single letter within range or a single digit within range
            return (
                input.matches("^[A-" + (char) ('A' + numChoices - 1) + "]$") ||
                input.matches("^[1-" + numChoices + "]$")
            );
        }

        // Validation for OPEN_ENDED: any non-empty string is considered valid
        if (q.getType() == QuestionType.OPEN_ENDED) {
            return !input.isEmpty();
        }
        // Validation for ORDERING: must match the correct order of choices
        if (q.getType() == QuestionType.ORDERING) {
            String[] parts = rawAnswer.trim().split("[^0-9]+");

            if (parts.length != q.getChoices().size()) {
                return false;
            }

            Set<Integer> used = new HashSet<>();

            for (String p : parts) {
                try {
                    int index = Integer.parseInt(p);

                    if (index < 1 || index > parts.length) {
                        return false;
                    }

                    if (!used.add(index)) {
                        return false; // doublon
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            return true;
        }
        return true;
    }

    /**
     * compares the player's answer with the correct answer stored in the Question object
     * supports numeric tolerance and string normalization
     * * @param q the current question
     * @param rawAnswer the raw string input from the player
     * @return true if the answer is correct, false otherwise
     */

    public static boolean isCorrect(Question q, String rawAnswer) {
        // ensure the input is valid for the specific question type
        if (!isValidAnswer(q, rawAnswer)) {
            return false;
        }

        // ordering question logic is suppose to do it first
        if (q.getType() == QuestionType.ORDERING) {
            String[] parts = rawAnswer.trim().split("[^0-9]+");
            List<String> filtered = new ArrayList<>();

            for (String part : parts) {
                if (!part.isEmpty()) {
                    filtered.add(part);
                }
            }

            parts = filtered.toArray(new String[0]);

            List<String> userOrder = new ArrayList<>();

            for (String p : parts) {
                int index = Integer.parseInt(p) - 1;
                userOrder.add(q.getChoices().get(index));
            }

            return userOrder.equals(q.getOrderingAnswer());
        }

        String playerAnswer = rawAnswer.trim().toUpperCase();

        // CASE 1: Numeric Logic (value comparison with tolerance)
        if (q.getType() == QuestionType.NUMERIC) {
            //tray-catch: if the player writes "i don't know" instead of a number it won't crash
            try {
                double userVal = Double.parseDouble(
                    playerAnswer.replace(",", ".")
                ); // transforms 3,14 to 3.14
                double correctVal = q.getNumericAnswer();
                // success if the absolute difference is within the allowed tolerance range
                return Math.abs(userVal - correctVal) <= q.getTolerance();
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // CASE 2: Text-based Logic (MCQ, T/F, OPEN_ENDED)
        // normalize the correct answer from the data source for a fair comparison
        String correctAnswer = q.getAnswer().trim().toUpperCase();

        // normalize TRUE_FALSE input/answer to short format (T/F)
        if (q.getType() == QuestionType.TRUE_FALSE) {
            if (playerAnswer.equals("TRUE")) playerAnswer = "T";
            if (playerAnswer.equals("FALSE")) playerAnswer = "F";
            if (correctAnswer.equals("TRUE")) correctAnswer = "T";
            if (correctAnswer.equals("FALSE")) correctAnswer = "F";
        }

        // normalize MULTIPLE_CHOICE numeric input (e.g., "1" becomes "A")
        if (
            q.getType() == QuestionType.MULTIPLE_CHOICE &&
            playerAnswer.matches("^[1-9]$")
        ) {
            int index = Integer.parseInt(playerAnswer) - 1;
            playerAnswer = String.valueOf((char) ('A' + index));
        }

        // Validation for OPEN_ENDED: any non-empty string is considered valid
        if (q.getType() == QuestionType.OPEN_ENDED) {
            //Removal of punctuation
            playerAnswer = playerAnswer.replaceAll("[^A-Z0-9 ]", "");
            correctAnswer = correctAnswer.replaceAll("[^A-Z0-9 ]", "");
            //Ignore double spaces
            playerAnswer = playerAnswer.replaceAll("\\s+", " ").trim();
            correctAnswer = correctAnswer.replaceAll("\\s+", " ").trim();
        }

        // final string comparison for text-based questions
        return playerAnswer.equals(correctAnswer);
    }
}
