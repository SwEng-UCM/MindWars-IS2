package game;

import bot.EasyBot;
import player.Player;
import trivia.Question;
import trivia.QuestionType;
import trivia.AnswerValidator;

public class UnitTests {

    public static void runAll() {
        // assert false : "Assertions are working!\n";

        // assert 1 + 1 == 3 : "ERROR: 1 + 1 doesn t equal 3";

        System.out.println("[TESTING] Starting unit tests...\n");

        try {
            testPlayerLogic();
            testMapBoundaries();
            testAnswerValidation();
            testBotStrategy();

            System.out.println("[SUCCESS] All core tests passed!\n");
        } catch (AssertionError e) {
            System.err.println("[!!!] TEST FAILED: " + e.getMessage());
        }
    }

    private static void testPlayerLogic() {
        Player p = new Player("TestUser");

        assert p.getScore() == 0 : "Initial score should be 0";

        p.addScore(150);
        assert p.getScore() == 150 : "Score should be 150 after adding points";

        p.subtractScore(200);
        assert p.getScore() >= 0 : "Score should not drop below zero";
    }

    private static void testMapBoundaries() {
        MapGrid map = new MapGrid(3);

        assert map.isInside(0, 0) : "0,0 should be inside";
        assert !map.isInside(3, 3) : "3,3 should be outside for a 3x3 map";
    }

    private static void testAnswerValidation() {
        Question q = new Question();

        q.setCategory("General");
        q.setDifficulty("EASY");
        q.setPrompt("What is 10 + 5?");
        q.setAnswer("15");
        q.setType(QuestionType.NUMERIC);
        q.setNumericAnswer(15.0);

        assert AnswerValidator.isCorrect(q, "15") : "Should accept correct answer";

        assert !AnswerValidator.isCorrect(q, "20") : "Should reject wrong answer";
    }

    private static void testBotStrategy() {
        Player bot = new Player("Optimus Prime");

        bot.setStrategy(new EasyBot());

        Question q = new Question();
        q.setPrompt("1+1?");
        q.setAnswer("2");
        q.setType(QuestionType.NUMERIC);

        assert bot.getStrategy() != null : "Strategy should be assigned to the player";

        String botAnswer = bot.getStrategy().getAnswer(q);
        assert botAnswer != null : "Bot answer should never be null";

        long delay = bot.getStrategy().getResponseTime();
        assert delay >= 0 : "Response time should be a valid non-negative number";
    }
}