import model.GameModel;
import trivia.QuestionBank;
import view.MainFrame;
import ui.MainWindow;
import javax.swing.SwingUtilities;

/**
 * Entry point for the new MVC Swing GUI introduced in Sprint 4 (#65).
 * Loads the question bank, builds a {@link GameModel}, and hands it to
 * {@link MainFrame} on the EDT. The original console {@code Main} class
 * still exists as a fallback.
 */
public class GuiMain {
    public static void main(String[] args) {

        QuestionBank bank = new QuestionBank("questions.json");
        GameModel model = new GameModel(bank);

        SwingUtilities.invokeLater(() -> {
            new ui.MainWindow(model).setVisible(true);
        });
    }
}