package bot;

import trivia.Question;

public interface BotStrategy {
    String getAnswer(Question question);

    long getResponseTime();

    String getDifficultyName();
}