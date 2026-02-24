package trivia;

import java.util.List;

public class Question {
    private QuestionType type; // MULTIPLE_CHOICE, TRUE_FALSE, NUMERIC, OPEN_ENDED
    private String category;
    private String difficulty;
    private String prompt;
    private List<String> choices;
    private String answer;  // used for MCQ, TF, Open-Ended
    private double numericAnswer; // used for NUMERIC
    private double tolerance; // margin of error for NUMERIC -> is going to be used for estimation-based challenges 

    public Question() {} // empty constructor for GSON

    public String formatForConsole() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ").append(category.toUpperCase())
          .append(" (").append(difficulty).append(") ---\n");
        sb.append(prompt).append("\n");

        if (type == QuestionType.MULTIPLE_CHOICE) {
            if (choices != null) {
                char label = 'A';
                for (String choice : choices) {
                    sb.append(label).append(") ").append(choice).append("\n");
                    label++;
                }
            }
        } else if (type == QuestionType.TRUE_FALSE) {
            sb.append("(T)rue or (F)alse\n");
        } else if (type == QuestionType.NUMERIC) {
            sb.append("[Answer with a number]\n");
        }
        return sb.toString();
    }

    // getters
    public QuestionType getType() { return type; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public String getAnswer() { return answer; }
    public List<String> getChoices() { return choices; }
    public double getNumericAnswer() { return numericAnswer; }
    public double getTolerance() { return tolerance; }
}