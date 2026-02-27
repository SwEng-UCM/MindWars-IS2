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
    private final MapGrid map;

    long TIME_LIMIT_MS = 15000;

    public Game(ConsoleIO io, QuestionBank questionBank) {
        this.io = io;
        this.questionBank = questionBank;
        this.gameState = new GameState();
        this.map = new MapGrid(3);
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
            io.println("   =========== ROUND " + (round + 1) + " of " + roundQuestions.size() + " ===========");

            Question currentQuestion = roundQuestions.get(round);
            boolean[] roundResults = new boolean[gameState.getPlayers().size()];
            long[] roundTimes = new long[gameState.getPlayers().size()];

            // Hot seat: alternate between players each round
            for (int p = 0; p < gameState.getPlayers().size(); p++) {
                gameState.setCurrentPlayerIndex(p);
                Player currentPlayer = gameState.getPlayers().get(p);

                io.println("");
                io.println("   +----------------------------------------+");
                io.println("   |                                        |");
                io.println("   |     PASS THE DEVICE TO " + padRight(currentPlayer.getName().toUpperCase(), 14) + " |");
                io.println("   |     Other player, look away!           |");
                io.println("   |                                        |");
                io.println("   +----------------------------------------+");
                io.readLine("   Press ENTER when ready...");

                io.println("");
                io.println("   " + currentPlayer.getName() + " - Question " + (round + 1) + " of "
                        + roundQuestions.size());
                io.println("   ----------------------------------------");
                io.println("   " + currentQuestion.formatForConsole().replace("\n", "\n   "));

                long startTime = System.currentTimeMillis();
                String response = readValidAnswer(currentQuestion);
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                roundTimes[p] = elapsedTime;
                currentPlayer.setTimer(currentPlayer.getTimer() + elapsedTime);

                boolean isCorrect = AnswerValidator.isCorrect(currentQuestion, response);

                if (isCorrect && elapsedTime > 15000) {
                    io.println("   >> [TIMEOUT] Correct answer, but took too long (> 15s)!");
                    isCorrect = false;
                }

                roundResults[p] = isCorrect;

                if (isCorrect) {
                    int points = calculatePoints(currentQuestion);
                    currentPlayer.addScore(points);
                    currentPlayer.setStreak(currentPlayer.getStreak() + 1);

                    io.println("   >> CORRECT! +" + points + " points ");
                    if (currentPlayer.getStreak() >= 3) {
                        io.println("   Streak bonus active!");
                    }
                } else {
                    currentPlayer.resetStreak();
                    String correctAnswer = (currentQuestion.getType() == QuestionType.NUMERIC)
                            ? String.valueOf(currentQuestion.getNumericAnswer())
                            : currentQuestion.getAnswer();
                    io.println("   >> WRONG! The answer was: " + correctAnswer);
                }
                io.println("   Score: " + currentPlayer.getScore());
            }

            applySpeedBonus(roundResults, roundTimes);

            handleTerritoryPhase(roundResults[0], roundTimes[0], roundResults[1], roundTimes[1]);
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

        io.println("  RULES:");
        io.println("  1. BATTLE: Answer correctly and BE FAST! Speed is the tie-breaker.");
        io.println("  2. REWARD: Round Winner claims 2 cells from the map. The runner-up claims 1 cell.");
        io.println("             - Once claimed, the cell will show your NAME'S INITIAL.");
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

    private void askToClaim(int playerNum) {
        boolean done = false;

        Player currentPlayer = gameState.getPlayers().get(playerNum - 1);

        char initial = currentPlayer.getName().toUpperCase().charAt(0);

        while (!done) {
            String input = io.readNonEmptyString(
                    "  " + currentPlayer.getName() + " (" + initial + "), enter coordinates row,col:");

            if (!input.contains(",")) {
                io.println("  Invalid format! Please use: row,col");
                continue;
            }

            try {
                String[] parts = input.split(",");
                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());

                if (map.claimCell(initial, r, c)) {
                    done = true;
                    io.println("  Success! Cell [" + r + "," + c + "] is now marked with '" + initial + "'.");
                } else {
                    io.println("  That cell is either outside the map or already taken! Try again.");
                }
            } catch (NumberFormatException e) {
                io.println("  Error: Please enter valid numbers for row and column.");
            }
        }
    }

    private void handleTerritoryPhase(boolean p1Correct, long p1Time, boolean p2Correct, long p2Time) {
        int winner;
        String reason;

        if (p1Correct && !p2Correct) {
            winner = 1;
            reason = "being the only one with the correct answer!";
        } else if (!p1Correct && p2Correct) {
            winner = 2;
            reason = "being the only one with the correct answer!";
        } else if (p1Correct && p2Correct) {
            if (p1Time <= p2Time) {
                winner = 1;
            } else {
                winner = 2;
            }
            reason = "answering correctly AND being faster!";
        } else {
            if (p1Time <= p2Time) {
                winner = 1;
            } else {
                winner = 2;
            }
            reason = "being faster (even though both missed the mark)!";
        }

        int loser;
        if (winner == 1) {
            loser = 2;
        } else {
            loser = 1;
        }

        String winnerName = gameState.getPlayers().get(winner - 1).getName();
        String loserName = gameState.getPlayers().get(loser - 1).getName();

        io.println("\n  >> ROUND CONQUEROR: " + winnerName);
        io.println("  >> Reason: " + reason);

        for (int i = 1; i <= 2; i++) {
            io.println("  [" + winnerName + " Selection " + i + "/2]");
            askToClaim(winner);
        }

        io.println("  [" + loserName + " Selection 1/1]");
        askToClaim(loser);

        map.display(io);
    }

    private void applySpeedBonus(boolean[] results, long[] times) {
        if (results.length >= 2 && results[0] && results[1]) {

            int winnerIndex;

            if (times[0] <= times[1]) {
                winnerIndex = 0;
            } else {
                winnerIndex = 1;
            }

            Player speedWinner = gameState.getPlayers().get(winnerIndex);

            int speedBonus = 1;
            speedWinner.addScore(speedBonus);

            io.println("\n   SPEED BONUS: " + speedWinner.getName() + " was faster! +" + speedBonus + " pts");
        }
    }
}
