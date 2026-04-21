import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import game.Game;
import game.UnitTests;
import model.GameModel;
import trivia.QuestionBank;
import util.AudioSettings;
import util.ConsoleIO;
import util.SoundManager;
import view.MainWindow;

/**
 * PURPOSE:
 * - Entry point of the program.
 * - Launches the Swing GUI by default.
 * - Pass {@code --console} to fall back to the original console game.
 */

public class Main {
    public static void main(String[] args) {

        boolean consoleMode = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                consoleMode = true;
            }
        }

        if (consoleMode) {
            UnitTests.runAll();
            ConsoleIO io = ConsoleIO.getConsole();
            QuestionBank bank = new QuestionBank("questions.json");
            Game engine = new Game(io, bank);
            engine.run();
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        QuestionBank bank = new QuestionBank("questions.json");
        GameModel model = new GameModel(bank);

        AudioSettings audioSettings = new AudioSettings();
        SoundManager soundManager = new SoundManager(audioSettings);

        if (audioSettings.isMusicEnabled()) {
            soundManager.startBackground();
        }

        SwingUtilities.invokeLater(() -> new MainWindow(model).setVisible(true));

    }
}
