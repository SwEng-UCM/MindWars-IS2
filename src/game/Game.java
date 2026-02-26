package game;

import player.Player;
import trivia.AnswerValidator;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;
import ui.ConsoleIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static player.Player.STREAK_BONUS;

public class Game {

    private final ConsoleIO io;
    private final QuestionBank questionBank;
    private final GameState gameState;

    public Game(ConsoleIO io, QuestionBank questionBank) {
        this.io = io;
        this.questionBank = questionBank;
        this.gameState = new GameState();
    }

    public void run() {
        printWelcomeMessage();
        setupPlayers();
        setStartingPlayer();

        io.println("");
        io.println("  All players registered. Let the battle begin!");
        io.println("");

        int questionsPerPlayer = 3;

        // Randomly decide which player chooses category and which chooses difficulty
        Random random = new Random();
        boolean player1ChoosesCategory = random.nextBoolean();

        Player categoryChooser = player1ChoosesCategory ? gameState.getPlayers().get(0) : gameState.getPlayers().get(1);
        Player difficultyChooser = player1ChoosesCategory ? gameState.getPlayers().get(1)
                : gameState.getPlayers().get(0);

        // Category selection
        io.println("  " + categoryChooser.getName() + ", you will choose the CATEGORY for all questions!");
        io.println("");
        List<String> categories = new ArrayList<>(questionBank.getCategories());
        String selectedCategory = io.selectFromList("  Choose a CATEGORY:", categories);
        io.println("  Category selected: " + selectedCategory);
        io.println("");

        // Difficulty selection
        io.println("  " + difficultyChooser.getName() + ", you will choose the DIFFICULTY for all questions!");
        io.println("");
        List<String> difficulties = new ArrayList<>(questionBank.getDifficulties(selectedCategory));
        String selectedDifficulty = io.selectFromList("  Choose a DIFFICULTY:", difficulties);
        io.println("  Difficulty selected: " + selectedDifficulty);
        io.println("");

        // Get questions for the game based on selections
        List<Question> roundQuestions = new ArrayList<>();
        for (int i = 0; i < questionsPerPlayer; i++) {
            Question q = questionBank.getQuestion(selectedCategory, selectedDifficulty);
            if (q != null) {
                roundQuestions.add(q);
            }
        }

        if (roundQuestions.isEmpty()) {
            io.println("  ERROR: No questions available for this category/difficulty combination!");
            io.println("  Game cannot start.");
            return;
        }

        if (roundQuestions.size() < questionsPerPlayer) {
            io.println("  WARNING: Only " + roundQuestions.size() + " question(s) available instead of "
                    + questionsPerPlayer + ".");
            io.println("  Continuing with " + roundQuestions.size() + " question(s)...");
        }

        io.println("  Starting game with " + roundQuestions.size() + " questions!");
        io.println("");

        // Play multiple rounds with pre-selected questions
        for (int round = 0; round < roundQuestions.size(); round++) {
            io.println("");
            io.println("  =========== ROUND " + (round + 1) + " of " + roundQuestions.size() + " ===========");

            Question currentQuestion = roundQuestions.get(round);

            // Hot seat: alternate between players each round
            for (int p = 0; p < gameState.getPlayers().size(); p++) {
                gameState.setCurrentPlayerIndex(p);
                Player currentPlayer = gameState.getPlayers().get(p);

                // Hot seat handoff between players
                io.println("");
                io.println("  +----------------------------------------+");
                io.println("  |                                        |");
                io.println("  |     PASS THE DEVICE TO " + padRight(currentPlayer.getName().toUpperCase(), 14) + " |");
                io.println("  |     Other player, look away!           |");
                io.println("  |                                        |");
                io.println("  +----------------------------------------+");
                io.readLine("  Press ENTER when ready...");

                io.println("");
                io.println(
                        "  " + currentPlayer.getName() + " - Question " + (round + 1) + " of " + roundQuestions.size());
                io.println("  ----------------------------------------");
                io.println("  " + currentQuestion.formatForConsole().replace("\n", "\n  "));

                // Start timer
                long startTime = System.currentTimeMillis();

                // Read answer and validate input
                String response = readValidAnswer(currentQuestion);

                // Stop timer and add elapsed time to player's total
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                currentPlayer.setTimer(currentPlayer.getTimer() + elapsedTime);

                boolean isCorrect = AnswerValidator.isCorrect(currentQuestion, response);

                if (isCorrect) {
                    int points = calculatePoints(currentQuestion);
                    currentPlayer.setStreak(points);
                    io.println("  >> CORRECT! +" + points + " points ");
                    if (currentPlayer.getStreak() >= 3) {
                        io.println("Streak bonus! +" + STREAK_BONUS);
                    }
                } else {
                    currentPlayer.resetStreak();
                    String correctAnswer = (currentQuestion.getType() == QuestionType.NUMERIC)
                            ? String.valueOf(currentQuestion.getNumericAnswer())
                            : currentQuestion.getAnswer();
                    io.println("  >> WRONG! The answer was: " + correctAnswer);
                }
                io.println("  Score: " + currentPlayer.getScore());
            }
        }

        // Final results
        determineWinner();
    }

    private String readValidAnswer(Question question) {
        while (true) {
            String response = io.readNonEmptyString("  Your answer:");
            if (AnswerValidator.isValidAnswer(question, response)) {
                return response;
            }
            io.println("  Invalid answer. Please enter a valid option.");
        }
    }

    private void printWelcomeMessage() {
        io.println("");
        io.println("  +========================================+");
        io.println("  |                                        |");
        io.println("  |      M I N D W A R S  T R I V I A      |");
        io.println("  |       -  Where Brains Conquer  -       |");
        io.println("  |                                        |");
        io.println("  +========================================+");
        io.println("");
    }

    private void setupPlayers() {
        io.println("  The game requires 2 players.");
        io.println("");
        for (int i = 1; i <= 2; i++) {
            String name = io.readNonEmptyString(
                    "  Enter name for Player " + i + ":");
            Player newPlayer = new Player(name);
            gameState.addPlayer(newPlayer);
            io.println("");
        }
    }

    private void setStartingPlayer() {
        gameState.setCurrentPlayerIndex(0);

        String startName = gameState.getPlayers().get(0).getName();
        io.println("  " + startName + " will go first.");
    }

    private void determineWinner() {

        io.println("\n=== GAME OVER ===");
        io.println("\nFinal Scores:");

        io.println("");
        io.println("  +========================================+");
        io.println("  |                                        |");
        io.println("  |           FINAL SCOREBOARD             |");
        io.println("  |                                        |");
        io.println("  +========================================+");
        io.println("");

        // show all player scores and response times
        for (Player player : gameState.getPlayers()) {
            double timeInSeconds = player.getTimer() / 1000.0;

            io.println(player.getName() + ": " + player.getScore() + " points (Response time: " +
                    String.format("%.2f", timeInSeconds) + "s)");

            io.println("    " + padRight(player.getName(), 15)
                    + padRight(player.getScore() + " pts", 10)
                    + String.format("%.2fs", timeInSeconds));
        }

        io.println("");
        io.println("  ------------------------------------------");

        // calculate the winner (considers score first, then response time for ties)
        Player winner = WinnerCalculator.getWinnerOrNull(gameState.getPlayers());

        if (winner == null) {
            io.println("  It's a TIE! Same score and response time.");
        } else {
            io.println("  WINNER: " + winner.getName() + "!");
        }

        io.println("  ------------------------------------------");
        io.println("");
        io.println("  Thanks for playing MindWars!");
        io.println("");
    }

    private String padRight(String text, int length) {
        if (text.length() >= length)
            return text;
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(' ');
        }

        return sb.toString();
    }

    private int calculatePoints(Question question) {
        String diff = question.getDifficulty().toUpperCase();
        // logic: easy=10 medium=20 hard=30
        return switch (diff) {
            case "HARD" -> 30;
            case "MEDIUM" -> 20;
            default -> 10; // covers "easy" or any unexpected strings
        };
    }
}
