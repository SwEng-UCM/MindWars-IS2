import javax.swing.SwingUtilities;

import game.Game;
import trivia.QuestionBank;
import ui.ConsoleIO;
import ui.MainWindow;

/**
 * PURPOSE:
 * - Entry point of the program.
 * - Wires objects together and starts the game.
 */

public class Main {
    public static void main(String[] args) {

        ConsoleIO io = ConsoleIO.getConsole();
        QuestionBank bank = new QuestionBank("questions.json");
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();

            window.setVisible(true);
        });
        Game engine = new Game(io, bank);

        // engine.run();
    }
}
