package bot;

import trivia.Question;
import java.util.Random;

public class HardBot implements BotStrategy {
    private final Random random = new Random();

    @Override
    public String getAnswer(Question question) {
        int chance = random.nextInt(100);

        // 80% chances to give the right answer
        if (chance < 80) {
            return question.getAnswer();
        } else {
            // 20% chances to give a wrong answer (not finished bcs there are multiple types
            // of questions)
            return "I am not sure, but I will guess A";
        }
    }

    @Override
    public long getResponseTime() {
        return 1000 + random.nextInt(2000);
    }

    @Override
    public String getDifficultyName() {
        return "Hard";
    }
}