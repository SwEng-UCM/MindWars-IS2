package trivia;

import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class Question {

    private QuestionType type; // MULTIPLE_CHOICE, TRUE_FALSE, NUMERIC, OPEN_ENDED
    private String category;
    private String difficulty;
    private String prompt;
    private List<String> choices;
    private String answer; // used for MCQ, TF, Open-Ended
    private double numericAnswer; // used for NUMERIC
    private double tolerance; // margin of error for NUMERIC -> is going to be used for estimation-based challenges
    private List<String> orderingAnswer; // used for ORDERING
    private String clue;

    public Question() {} // empty constructor for GSON

    public String formatForConsole() {
        StringBuilder sb = new StringBuilder();
        sb
            .append("\n--- ")
            .append(category.toUpperCase())
            .append(" (")
            .append(difficulty)
            .append(") ---\n");
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
            if (choices != null) {
                char label = 'A';
                for (String choice : choices) {
                    sb.append("-").append(choice).append("\n");
                }
                sb.append("[Answer with a number]\n");
            } else {
                sb.append("[Answer with a number]\n");
            }
        } else if (type == QuestionType.OPEN_ENDED) {
            sb.append("Type an answer\n");
        } else if (type == QuestionType.ORDERING) {
            if (choices != null) {
                int index = 1;
                for (String choice : choices) {
                    sb.append(index).append(") ").append(choice).append("\n");
                    index++;
                }
                sb.append(
                    "Enter the correct order (example: 2 1 3 or 1;2;3)\n"
                );
            }
        }
        return sb.toString();
    }

    public Question cloneQuestion() {
        Question q = new Question();
        q.setPrompt(this.getPrompt());
        q.setType(this.getType());
        q.setAnswer(this.getAnswer());
        q.setNumericAnswer(this.getNumericAnswer());
        q.setCategory(this.getCategory());
        q.setDifficulty(this.getDifficulty());

        // Copie sécurisée des listes
        q.setChoices(
            this.getChoices() != null
                ? new ArrayList<>(this.getChoices())
                : new ArrayList<>()
        );
        q.setOrderingAnswer(
            this.getOrderingAnswer() != null
                ? new ArrayList<>(this.getOrderingAnswer())
                : new ArrayList<>()
        );

        q.setClue(this.getClue());
        return q;
    }

    public void copyFrom(Question other) {
        if (other == null) return;

        this.prompt = other.prompt;
        this.type = other.type;
        this.category = other.category;
        this.difficulty = other.difficulty;
        this.answer = other.answer;
        this.numericAnswer = other.numericAnswer;

        if (other.choices != null) {
            this.choices = new ArrayList<>(other.choices);
        } else {
            this.choices = null;
        }

        if (other.orderingAnswer != null) {
            this.orderingAnswer = new ArrayList<>(other.orderingAnswer);
        } else {
            this.orderingAnswer = null;
        }

        this.clue = other.clue;
    }

    // getters
    public QuestionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getChoices() {
        return choices;
    }

    public double getNumericAnswer() {
        return numericAnswer;
    }

    public double getTolerance() {
        return tolerance;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<String> getOrderingAnswer() {
        return orderingAnswer;
    }

    public String getClue() {
        return clue;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setNumericAnswer(double numericAnswer) {
        this.numericAnswer = numericAnswer;
    }

    public void setOrderingAnswer(List<String> orderingAnswer) {
        this.orderingAnswer = orderingAnswer;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setType(QuestionType Type) {
        this.type = Type;
    }

    public void setClue(String new_clue) {
        this.clue = new_clue;
    }

    public void setCategory(String new_cat) {
        this.category = new_cat;
    }

    public void setDifficulty(String new_dif) {
        this.difficulty = new_dif;
    }
}
