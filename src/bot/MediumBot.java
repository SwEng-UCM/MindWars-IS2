package bot;

import trivia.Question;

public class MediumBot implements BotStrategy {
    @Override
    public String getAnswer(Question question) {
        return "The answer should be " + question.getAnswer();
    }

    @Override
    public long getResponseTime() {
        return 7000; // 7 seconds
    }

    @Override
    public String getDifficultyName() {
        return "Medium";
    }
}