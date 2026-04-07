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

        return "I'm not sure";
    }

    @Override
    public long getResponseTime() {
        return 0;
    }

    @Override
    public String getDifficultyName() {
        return "Medium";
    }
}