package bot;

import trivia.Question;

import java.util.List;
import java.util.Random;

public class MediumBot implements BotStrategy {

    private final Random random = new Random();

    @Override
    public String getAnswer(Question question) {
        int chance = random.nextInt(100);

        if (chance < 50) {
            return question.getAnswer();
        } else {
            return getSmartWrongAnswer(question);
        }
    }

    private String getSmartWrongAnswer(Question question) {
        List<String> choices = question.getChoices();
        String correctAnswer = question.getAnswer();

        if (choices != null && !choices.isEmpty()) {
            List<String> wrongChoices = choices.stream()
                    .filter(choice -> !choice.equalsIgnoreCase(correctAnswer))
                    .toList();

            if (!wrongChoices.isEmpty()) {
                return wrongChoices.get(random.nextInt(wrongChoices.size()));
            }
        }

        return "I'm not sure";
    }

    @Override
    public long getResponseTime() {
        return 1500 + random.nextInt(2000);
    }

    @Override
    public String getDifficultyName() {
        return "Medium";
    }
}