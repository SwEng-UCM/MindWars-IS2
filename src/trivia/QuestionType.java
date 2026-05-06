/*
 * @author Leopold Popper
 * AI-assisted: yes (Claude by Anthropic, via Claude Code)
 * @author ARNAUD Aloyse
 * AI-assisted: assist (ChatGPT)
 * @author Dimofte Raisa
 */
package trivia;

public enum QuestionType {
    MULTIPLE_CHOICE, // where the player selects one correct answer from a list of options
    TRUE_FALSE, // where the answer is either true or false
    NUMERIC, // where the answer is a number
    OPEN_ENDED, // where the player writes a text for the answer
    ORDERING, // where the player has to arrange a list of items in the correct order
}
