package game;

import player.Player;
import trivia.AnswerValidator;
import trivia.Question;
import trivia.QuestionBank;
import ui.ConsoleIO;

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

        io.println("\nSetup Complete!");
        io.println("Registered players: " + gameState.getPlayers().size());

        int questionsToAsk = 5;

        // Loop over each player (Player 1 then Player 2)
         for (int p = 0; p < gameState.getPlayers().size(); p++) {

            // Set whose turn it is
            gameState.setCurrentPlayerIndex(p);
            Player currentPlayer = gameState.getPlayers().get(p);

            io.println("\n============================");
            io.println("TURN: " + currentPlayer.getName());
            io.println("============================");
            io.println(currentPlayer.getName() + ", you will answer " + questionsToAsk + " questions!");

            for (int i = 1; i <= questionsToAsk; i++) {

                Question currentQuestion = questionBank.getRandomQuestion();

                // Safety: If the bank runs out of questions, stop early.
                if (currentQuestion == null) {
                    io.println("\nNo more questions available in the bank. Ending early.");
                    break;
                }

                io.println("\nQuestion " + i + "/" + questionsToAsk);
                io.println(currentQuestion.formatForConsole());

                long startTime = System.nanoTime();
                String response = io.readNonEmptyString(
                    "Your answer (e.g., A, B, C, D, T, F):"
                );
                long endTime = System.nanoTime();
                long elapsedTime = (endTime - startTime)/ 1_000_000;
                currentPlayer.addTime(elapsedTime);

                boolean isCorrect = AnswerValidator.isCorrect(currentQuestion, response);

                if (isCorrect) {
                    io.println("Correct!");
                    currentPlayer.addScore(1);
                } else {
                    io.println("Incorrect!");
                }

                io.println("Score for " + currentPlayer.getName() + ": " + currentPlayer.getScore());
            }

            io.println("\nEnd of turn for " + currentPlayer.getName() +
                    ". Current score: " + currentPlayer.getScore());
             io.println("Total time for " + currentPlayer.getName() + ": " + currentPlayer.formatTime());

         }

        // After both players finish, show final scores
        io.println("\n==================================");
        io.println("GAME OVER - FINAL SCORES");
        io.println("==================================");

        for (Player p : gameState.getPlayers()) {
            io.println(p.getName() + ": " + p.getScore());
        }
    }



    private void printWelcomeMessage() {
        io.println("WELCOME TO MINDWARS TRIVIA!");
    }

    private void setupPlayers() {
        io.println("\nThe game requires 2 players.");
        for (int i = 1; i <= 2; i++) {
            String name = io.readNonEmptyString(
                "Enter name for Player " + i + ":"
            );
            Player newPlayer = new Player(name);
            gameState.addPlayer(newPlayer);
            io.println("Hello, " + name + "! You have joined the game.");
        }
    }

    private void setStartingPlayer() {
        gameState.setCurrentPlayerIndex(0);

        String startName = gameState.getPlayers().get(0).getName();
        io.println("\n" + startName + " (Player 1) will start the game!");
    }
}
