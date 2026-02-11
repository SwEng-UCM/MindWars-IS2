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

        
        // ============================
        // NEW LOGIC: Ask 5 questions to Player 1 
        // ============================
        int questionsToAsk = 5;

        io.println("\n" + currentPlayer.getName() + ", you will answer " + questionsToAsk + " questions!");


        for (int i = 1; i <= questionsToAsk; i++) {


            // pull a question from the bank (each call should return a new question)
            Question currentQuestion = questionBank.getRandomQuestion();


            // Safety: If the bank runs out of questions, stop early.
            if (currentQuestion == null) {
                io.println("\nNo more questions available in the bank. Ending early.");
                break;
            }

            io.println("\nQuestion " + i + "/" + questionsToAsk);
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

            // Show the score after each question
            io.println("Score: " + currentPlayer.getScore());
        }

        // Game ends after Player 1 answers 5 questions
        io.println("\nGame Over (Player 1 only for now). Final score for "
            + currentPlayer.getName() + ": " + currentPlayer.getScore());



        /* 

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
        */
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
