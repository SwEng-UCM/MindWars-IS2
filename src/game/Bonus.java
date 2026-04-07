package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import player.Player;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;
import ui.ConsoleIO;

public class Bonus {

    private final ConsoleIO io;
    private final QuestionBank questionBank;
    private final Random random = new Random();

    public Bonus(ConsoleIO io, QuestionBank questionBank) {
        this.io = io;
        this.questionBank = questionBank;
    }

    public enum BonusType {
        FIFTY_FIFTY,
        NEW_QUESTION,
        CLUE,
    }

    public void offerBonusIfAvailable(Player player, Question q) {
        // if (player.hasUsedBonus()) return;

        player.setHasUsedBonus(false);
        boolean menuActive = true;
        // boolean bonusApplied = false;
        // String choice = "";

        while (menuActive) {
            io.println("\n  Bonus available! Choose one or skip:");

            // requirement: 50/50 unavailable for T/F and typed-answer (Open, Numeric,
            // Ordering)
            boolean canUse5050 = (q.getType() == QuestionType.MULTIPLE_CHOICE);
            // Requirement: New Question unavailable for Numeric/Ordering
            boolean canUseNewQ = (q.getType() != QuestionType.NUMERIC && q.getType() != QuestionType.ORDERING);

            if (canUse5050) {
                io.println("  1) 50/50");
            } else {
                io.println("  1) [50/50 Not Available for this question type]");
            }

            if (canUseNewQ) {
                io.println("  2) New Question (same category & difficulty)");
            } else {
                io.println("  2) [New Question Not Available for this question type]");
            }

            io.println("   3) Clue (get a hint)");
            io.println("   4) Skip");

            String choice = io.readNonEmptyString("  Your choice (1-4):");

            switch (choice) {
                case "1":
                    if (!canUse5050) {
                        io.println("  Selection unavailable for True/False or Open-Ended. Try another.");
                        continue;
                    }
                    apply5050(q);
                    player.setHasUsedBonus(true);
                    menuActive = false;
                    break;
                case "2":
                    if (!canUseNewQ) {
                        io.println("  Selection unavailable for this question type.");
                        continue;
                    }
                    applyNewQuestion(player, q);
                    player.setHasUsedBonus(true);
                    menuActive = false;
                    break;
                case "3":
                    showClue(q);
                    player.setHasUsedBonus(true);
                    menuActive = false;
                    break;
                case "4":
                    io.println("  Skipped bonus.");
                    menuActive = false;
                    break;
                default:
                    io.println("  Invalid choice. Try again.");
            }
        }

    }

    private void apply5050(Question q) {
        QuestionType type = q.getType();

        if (type == QuestionType.MULTIPLE_CHOICE) {
            List<String> choices = new ArrayList<>(q.getChoices());
            // convert the answer in index
            int correctIndex = q.getAnswer().toUpperCase().charAt(0) - 'A';

            String correctChoice = choices.get(correctIndex);
            choices.remove(correctChoice);

            String randomChoice = choices.get(random.nextInt(choices.size()));

            List<String> newChoices = new ArrayList<>();
            newChoices.add(correctChoice);
            newChoices.add(randomChoice);

            // mix
            java.util.Collections.shuffle(newChoices, random);

            // update the lettre of the ansews
            int newIndex = newChoices.indexOf(correctChoice);
            char newAnswer = (char) ('A' + newIndex);
            q.setAnswer(String.valueOf(newAnswer));

            q.setChoices(newChoices);
            io.println("  50/50 applied: only 2 choices remain!");
            io.println("  " + q.formatForConsole().replace("\n", "\n  "));
        }

        if (type == QuestionType.NUMERIC) {
            double correct = q.getNumericAnswer();
            // Generate a gap between 1 and 10% of the correct ansews
            double percent = 1 + random.nextInt(10); // 1 to 10%
            percent = percent / 100.0;
            double fake;

            if (correct == 0) {
                fake = 1 + random.nextInt(10);
            } else {
                boolean add = random.nextBoolean();
                fake = add ? correct * (1 + percent) : correct * (1 - percent);
            }

            List<String> options = new ArrayList<>();
            options.add(String.format(Locale.US, "%.2f", correct));
            options.add(String.format(Locale.US, "%.2f", fake));
            q.setChoices(options);
            io.println("  50/50 applied: 2 numeric options shown!");
            io.println("  " + q.formatForConsole().replace("\n", "\n  "));
        }

        if (type == QuestionType.ORDERING) {
            List<String> ordering = q.getOrderingAnswer();
            int hintCount = Math.max(1, ordering.size() / 2);
            List<String> hintElements = ordering.subList(0, hintCount);

            io.println(
                    "  50/50 applied: here is the start of the correct order!");
            io.println(
                    "  Hint (first " + hintCount + " elements): " + hintElements);
            io.println("  Full question remains to be answered by player:");
            io.println("  " + q.formatForConsole().replace("\n", "\n  "));
        }
    }

    private void applyNewQuestion(Player player, Question currentQuestion) {
        Question newQ = null;
        int tries = 0;

        while ((newQ == null || newQ.getAnswer() == null) && tries < 10) {
            newQ = questionBank.getQuestion(
                    currentQuestion.getCategory(),
                    currentQuestion.getDifficulty());
            tries++;
        }

        if (newQ == null || newQ.getAnswer() == null) {
            io.println("  No valid new question available!");
            return;
        }

        Question cloned = newQ.cloneQuestion();
        currentQuestion.copyFrom(cloned);

        io.println("\n  New question loaded!");
        io.println(
                "  " + currentQuestion.formatForConsole().replace("\n", "\n  "));
    }

    private void showClue(Question question) {
        String clue = question.getClue();
        if (clue == null || clue.isEmpty()) {
            io.println("  No clue available for this question.");
        } else {
            io.println(
                    "  " + question.formatForConsole().replace("\n", "\n  "));
            io.println("  Clue: " + clue);
        }
    }
}
