package bot;

import trivia.Question;

import java.util.List;
import java.util.Random;

public class MediumBot implements BotStrategy {

    // Random generator used for answer selection and response time
    private final Random random = new Random();

    @Override
    public String getAnswer(Question question) {
        int chance = random.nextInt(100);

        if (question.getType() == trivia.QuestionType.ORDERING) {
            return BotAnswerUtil.buildOrderingAnswer(question, random, chance < 50);
        }

        if (question.getType() == trivia.QuestionType.NUMERIC
                || question.getType() == trivia.QuestionType.OPEN_ENDED) {
            return BotAnswerUtil.buildFallbackAnswer(question, random, chance < 50);
        }

        // 50% probability to return the correct answer
        if (chance < 50) {
            return question.getAnswer();
        } else {
            return getSmartWrongAnswer(question);
        }
    }

    // Returns a random wrong choice from the available question choices
    private String getSmartWrongAnswer(Question question) {
        List<String> choices = question.getChoices();
        String correctAnswer = question.getAnswer();

        // If the question has choices, keep only the wrong ones
        if (choices != null && !choices.isEmpty()) {
            List<String> wrongChoices = choices.stream()
                    .filter(choice -> !choice.equalsIgnoreCase(correctAnswer))
                    .toList();

            // Return one random wrong choice
            if (!wrongChoices.isEmpty()) {
                return wrongChoices.get(random.nextInt(wrongChoices.size()));
            }
        }

        // Fallback for open-ended questions or missing choices
        return BotAnswerUtil.buildFallbackAnswer(question, random, false);
    }

    @Override
    public long getResponseTime() {
        // Medium bot responds in 1.5 to 3.5 seconds
        return 1500 + random.nextInt(2000);
    }

    @Override
    public String getDifficultyName() {
        return "Medium";
    }
}