package game;

import ui.ConsoleIO;
import trivia.Question;
import trivia.QuestionBank;
import player.Player;

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
        Player currentPlayer = gameState.getPlayers().get(gameState.getCurrentPlayerIndex());
        
        // pull a question from the bank
        Question currentQuestion = questionBank.getRandomQuestion();

        if (currentQuestion != null) {
            io.println("\n" + currentPlayer.getName() + ", it's your turn!");
            
            // display formatted question (Prompt + Choices)
            io.println(currentQuestion.formatForConsole());

            // Read the user's input
            String response = io.readNonEmptyString("Your answer (e.g., A, B, C, D, T, F):");
            
        }
    } 

    private void printWelcomeMessage() {
        io.println("WELCOME TO MINDWARS TRIVIA!");
    }

    private void setupPlayers() {
        io.println("\nThe game requires 2 players.");
        for (int i = 1; i <= 2; i++) {
            String name = io.readNonEmptyString("Enter name for Player " + i + ":");
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