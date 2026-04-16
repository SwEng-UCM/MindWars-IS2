import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import game.UnitTests;
import model.GameModel;
import trivia.QuestionBank;
import ui.MainWindow;

/**
 * PURPOSE:
 * - Entry point of the program.
 * - Runs unit tests, then launches the Swing GUI.
 */

public class Main {
    public static void main(String[] args) {

        UnitTests.runAll();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        QuestionBank bank = new QuestionBank("questions.json");
        GameModel model = new GameModel(bank);

        SwingUtilities.invokeLater(() -> {
            new MainWindow(model).setVisible(true);
        });
    }
}
