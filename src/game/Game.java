package game;

import player.Player;
import trivia.AnswerValidator;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;
import ui.ConsoleIO;

import java.util.ArrayList;
import java.util.List;

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

        // Pre-pick questions so both players get the same ones
        List<Question> roundQuestions = questionBank.getAllQuestionsAsList();
        if (roundQuestions.size() > questionsPerPlayer) {
            roundQuestions = roundQuestions.subList(0, questionsPerPlayer);
        }

        // Hot seat: alternate between players each round
        for (int round = 0; round < roundQuestions.size(); round++) {
            Question currentQuestion = roundQuestions.get(round);

            io.println("");
            io.println("  =========== ROUND " + (round + 1) + " of " + roundQuestions.size() + " ===========");

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
                    io.println("  >> CORRECT! +1 point");
                    currentPlayer.addScore(1);
                } else {
                    String correctAnswer = (currentQuestion.getType() == QuestionType.NUMERIC)
                            ? String.valueOf(currentQuestion.getNumericAnswer())
                            : currentQuestion.getAnswer();
                    io.println("  >> WRONG! The answer was: " + correctAnswer);
                }
                io.println("  Score: " + currentPlayer.getScore() + "/" + (round + 1));
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

                    "Enter name for Player " + i + ":");

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
}
