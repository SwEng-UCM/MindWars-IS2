//AI-generated (ChatGPT) then reviewed by human

package mindwars;

import mindwars.game.Game;
import mindwars.trivia.QuestionBank;
import mindwars.ui.ConsoleIO;

/**
 * PURPOSE:
 * - Entry point of the program.
 * - Wires objects together and starts the game.
 *
 * @TODO:
 *        - Create ConsoleIO, QuestionBank, and Game.
 *        - Call engine.run().
 *        - Keep this class tiny (no game rules, no Scanner logic).
 */
public class Main {
    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO();
        QuestionBank bank = new QuestionBank();
        Game engine = new Game(io, bank);
        engine.run();
    }
}