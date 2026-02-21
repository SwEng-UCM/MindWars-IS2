package trivia;

import java.util.List;

public class Question {
    private final QuestionType type;
    private final String prompt;
    private final List<String> choices;
    private final String correctAnswer;

    public Question(QuestionType type, String prompt, List<String> choices, String correctAnswer) {
        this.type = type;
        this.prompt = prompt;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
    }

    public String formatForConsole() {
        StringBuilder sb = new StringBuilder();
        sb.append(prompt).append("\n");

        if (type == QuestionType.MULTIPLE_CHOICE) {
            char label = 'A';
            for (String choice : choices) {
                sb.append(label).append(") ").append(choice).append("\n");
                label++;
            }
        } else if (type == QuestionType.TRUE_FALSE) {
            sb.append("(T)rue or (F)alse\n");
        }
        return sb.toString();
    }

    // getters
    public QuestionType getType() { return type; }
    public List<String> getChoices() { return choices; }
    public String getCorrectAnswer() { return correctAnswer; }
}