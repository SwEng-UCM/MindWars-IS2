package bot;

import trivia.Question;
import java.util.Random;

public class MediumBot implements BotStrategy {

    private final Random random = new Random();

    @Override
    public String getAnswer(Question question) {
        return "";
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