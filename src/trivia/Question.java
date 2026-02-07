package trivia;

//import java.util.List;

/**
 * PURPOSE:
 * - Represents a single question (data model).
 * - Holds prompt, type, choices, correct answer token.
 * - Knows how to format itself for console display (string).
 *
 * @TODO (MVP fields):
 *       - QuestionType type
 *       - String prompt
 *       - List<String> choices (for MCQ; can be empty for TF)
 *       - String correctAnswer (e.g., "A" or "T")
 *
 * @TODO (MVP methods):
 *       - Constructors with all fields
 *       - Getters
 *       - formatForConsole():
 *       - returns prompt + choices (A-D) or shows T/F
 *
 * @TODO (quality):
 *       - Keep this class free of input reading / scoring logic.
 */
public class Question {
    // @TODO fields

    public Question() {
        // @TODO add constructor(s)
    }

    public String formatForConsole() {
        // @TODO
        return "";
    }

    // @TODO getters
}