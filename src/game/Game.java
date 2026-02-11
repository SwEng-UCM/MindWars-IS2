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

        // get the current player from the state
        Player currentPlayer = gameState
            .getPlayers()
            .get(gameState.getCurrentPlayerIndex());

        // pull a question from the bank
        Question currentQuestion = questionBank.getRandomQuestion();

        if (currentQuestion != null) {
            io.println("\n" + currentPlayer.getName() + ", it's your turn!");

            // display formatted question (Prompt + Choices)
            io.println(currentQuestion.formatForConsole());

            // Read the user's input
            String response = io.readNonEmptyString(
                "Your answer (e.g., A, B, C, D, T, F):"
            );

            // Validate the answer
            boolean isCorrect = AnswerValidator.isCorrect(
                currentQuestion,
                response
            );
            if (isCorrect) {
                io.println("Correct!");
                currentPlayer.addScore(1);
            } else {
                io.println("Incorrect!");
            }
            //Show the score of the current player
            io.println("Score: " + currentPlayer.getScore());
        }

        // Determine the winner at the end of the game
        determineWinner();
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

    private void determineWinner() {
        io.println("\n=== GAME OVER ===");
        io.println("\nFinal Scores:");
        
        // show all player scores
        for (Player player : gameState.getPlayers()) {
            io.println(player.getName() + ": " + player.getScore() + " points");
        }

        // calculate the winner
        Player winner = WinnerCalculator.getWinnerOrNull(gameState.getPlayers());
        
        if (winner == null) {
            io.println("\nIt's a TIE! No clear winner.");
        } else {
            io.println("\n The winner is: " + winner.getName() + "!");
        }
    }
}
