package bot;

import trivia.Question;
import trivia.QuestionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class BotAnswerUtil {
    private BotAnswerUtil() {
    }

    static String buildOrderingAnswer(Question question, Random random, boolean shouldBeCorrect) {
        if (question == null || question.getType() != QuestionType.ORDERING) {
            return "";
        }

        List<String> choices = question.getChoices();
        if (choices == null || choices.isEmpty()) {
            return "";
        }

        List<Integer> correctOrder = new ArrayList<>();
        if (question.getOrderingAnswer() != null && !question.getOrderingAnswer().isEmpty()) {
            for (String item : question.getOrderingAnswer()) {
                int idx = choices.indexOf(item);
                if (idx >= 0) {
                    correctOrder.add(idx + 1);
                }
            }
        }

        if (correctOrder.size() != choices.size()) {
            correctOrder.clear();
            for (int i = 1; i <= choices.size(); i++) {
                correctOrder.add(i);
            }
        }

        List<Integer> answerOrder = new ArrayList<>(correctOrder);
        if (!shouldBeCorrect) {
            if (choices.size() > 1) {
                int guard = 0;
                do {
                    Collections.shuffle(answerOrder, random);
                    guard++;
                } while (answerOrder.equals(correctOrder) && guard < 10);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < answerOrder.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(answerOrder.get(i));
        }
        return sb.toString();
    }

    static String buildFallbackAnswer(Question question, Random random, boolean preferCorrect) {
        if (question == null) {
            return "Unknown";
        }

        QuestionType type = question.getType();
        if (type == QuestionType.ORDERING) {
            return buildOrderingAnswer(question, random, preferCorrect);
        }

        if (type == QuestionType.NUMERIC) {
            double base = question.getNumericAnswer();
            if (preferCorrect) {
                return String.valueOf((int) Math.round(base));
            }
            int offset = 1 + random.nextInt(5);
            double guess = (random.nextBoolean() ? base + offset : base - offset);
            return String.valueOf((int) Math.round(guess));
        }

        if (type == QuestionType.TRUE_FALSE) {
            if (preferCorrect && question.getAnswer() != null && !question.getAnswer().isBlank()) {
                return question.getAnswer();
            }
            return random.nextBoolean() ? "T" : "F";
        }

        if (type == QuestionType.MULTIPLE_CHOICE) {
            List<String> choices = question.getChoices();
            if (choices != null && !choices.isEmpty()) {
                if (preferCorrect && question.getAnswer() != null && !question.getAnswer().isBlank()) {
                    return question.getAnswer();
                }
                return choices.get(random.nextInt(choices.size()));
            }
        }

        if (preferCorrect && question.getAnswer() != null && !question.getAnswer().isBlank()) {
            return question.getAnswer();
        }
        return "Unknown";
    }
}
