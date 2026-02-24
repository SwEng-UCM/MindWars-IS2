import game.Game;
import trivia.QuestionBank;
import ui.ConsoleIO;

/**
 * PURPOSE:
 * - Entry point of the program.
 * - Wires objects together and starts the game.
 */

public class Main {
    public static void main(String[] args) {

        ConsoleIO io = new ConsoleIO();
       QuestionBank bank = new QuestionBank("questions.json");

        Game engine = new Game(io, bank);

        engine.run();
    }
}
