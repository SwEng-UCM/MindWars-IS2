package trivia;

/**
 * PURPOSE:
 * - Enumerates supported question types.
 *
 * @TODO (MVP):
 *       - Keep MULTIPLE_CHOICE and TRUE_FALSE only.
 *
 * @TODO (later):
 *       - Add SHORT_ANSWER, NUMBER_GUESS, ORDERING, etc.
 */

public enum QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    NUMERIC, // where the answer is a number
    OPEN_ENDED, // where the player writes a text for the answer
    ORDERING,
}
