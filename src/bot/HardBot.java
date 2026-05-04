package bot;

import trivia.Question;

import java.util.List;
import java.util.Random;

public class HardBot implements BotStrategy {
    private final Random random = new Random();

    @Override
    public String getAnswer(Question question) {
        int chance = random.nextInt(100);

        if (question.getType() == trivia.QuestionType.ORDERING) {
            return BotAnswerUtil.buildOrderingAnswer(question, random, chance < 80);
        }

        if (question.getType() == trivia.QuestionType.NUMERIC
                || question.getType() == trivia.QuestionType.OPEN_ENDED) {
            return BotAnswerUtil.buildFallbackAnswer(question, random, chance < 80);
        }

        if (chance < 80) {
            return question.getAnswer();
        } else {
            return getSmartWrongAnswer(question);
        }
    }

    private String getSmartWrongAnswer(Question question) {
        List<String> options = question.getChoices();
        String correctAnswer = question.getAnswer();

        // if we have options (multiple choice or true/false)
        if (options != null && !options.isEmpty()) {
            List<String> wrongOptions = options.stream()
                    .filter(opt -> !opt.equalsIgnoreCase(correctAnswer))
                    .toList();

            if (!wrongOptions.isEmpty()) {
                return wrongOptions.get(random.nextInt(wrongOptions.size()));
            }
        }

        // if it s an open-ended question or we do not have an option list (to be
        // implemented)
        return BotAnswerUtil.buildFallbackAnswer(question, random, false);
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