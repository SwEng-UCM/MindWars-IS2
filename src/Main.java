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
       QuestionBank bank = new QuestionBank("/Users/ashleyumeghalu/Documents/V26/INGENIERÍA DEL SOFTWARE II - 805347/mindwars/MindWars-IS2/src/questions.txt");

        Game engine = new Game(io, bank);

        engine.run();
    }
}
