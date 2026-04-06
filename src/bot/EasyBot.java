package bot;

import trivia.Question;

public class EasyBot implements BotStrategy {

    @Override
    public String getAnswer(Question question) {
        return "I'm not sure... maybe B?";
    }

    @Override
    public long getResponseTime() {
        return 10000; // 10 seconds
    }

    @Override
    public String getDifficultyName() {
        return "Easy";
    }
}