package ui;

import controller.RegisterController;
import persistence.DatabaseInitializer;
import persistence.UserRepository;

import javax.swing.*;
import java.awt.*;
import model.GameModel;
import view.MainFrame;

/**
 * Main authentication window (Login/Register).
 * This is the primary entry point for the user interface upon startup.
 */
public class MainWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private final GameModel model;
    private player.Player sessionPlayer;

    public MainWindow(GameModel model) {
        this.model = model;

        setTitle("MindWars Trivia - Welcome");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window on the screen

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Add panels. MenuPanel contains the login UI with logo and gradient.
        // We pass 'this' so MenuPanel can call methods like startGameSession().
        // If you have a separate RegisterPanel, add it here:
        // mainContainer.add(new RegisterPanel(this), "REGISTER");
        MenuPanel menuPanel = new MenuPanel(this);
        RegisterPanel registerPanel = new RegisterPanel(this);
        RegisterController registerController =
                new RegisterController(registerPanel, new UserRepository());
        registerPanel.setController(registerController);

        MainMenuPanel mainMenuPanel = new MainMenuPanel(this);
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(registerPanel, "REGISTER");
        mainContainer.add(mainMenuPanel, "MAIN_MENU");
        add(mainContainer);
    }

    /**
     * Closes the login window and launches the game engine (MainFrame).
     * This method is triggered from MenuPanel when the "Login to Game" button is
     * clicked.
     */
    public void startGameSession() {

        MainFrame gameFrame = new MainFrame(model);

        gameFrame.setBounds(this.getBounds());

        gameFrame.setVisible(true);

        this.dispose();
    }

    public void setSessionPlayer(player.Player p) {
        this.sessionPlayer = p;
    }

    public player.Player getSessionPlayer() {
        return sessionPlayer;
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }

    /**
     * Main method for rapid testing of the login window standalone.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        DatabaseInitializer.initialize();
        // Run Swing components on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Usually initialized in GuiMain, but added here for testing purposes
            // GameModel dummyModel = new GameModel(new
            // trivia.QuestionBank("questions.json"));
            // new MainWindow(dummyModel).setVisible(true);
        });
    }
}