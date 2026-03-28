package bot;

import trivia.Question;

public class HardBot implements BotStrategy {
    @Override
    public String getAnswer(Question question) {
        return question.getAnswer();
    }

    @Override
    public long getResponseTime() {
        return 2000; // 2 seconds
    }

    @Override
    public String getDifficultyName() {
        return "Hard";
    }
}