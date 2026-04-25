package view;

import controller.LoginController;
import controller.RegisterController;
import java.awt.*;
import javax.swing.*;
import model.GameModel;
import persistence.DatabaseInitializer;
import persistence.UserRepository;
import util.SoundManager;

public class MainWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MenuPanel menuPanel;
    private RegisterPanel registerPanel;
    private final GameModel model;
    private final SoundManager soundManager;
    private player.Player sessionPlayer;

    public MainWindow(GameModel model, SoundManager soundManager) {
        this.model = model;
        this.soundManager = soundManager;

        setTitle("MindWars Trivia - Welcome");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this);
        registerPanel = new RegisterPanel(this);

        RegisterController registerController = new RegisterController(
                registerPanel,
                new UserRepository());
        registerPanel.setController(registerController);

        LoginController loginController = new LoginController(
                menuPanel,
                new UserRepository());
        menuPanel.setController(loginController);

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(registerPanel, "REGISTER");
        add(mainContainer);
    }

    public void startGameSession() {
        MainFrame gameFrame = new MainFrame(model, soundManager);
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
        SwingUtilities.invokeLater(() -> {
            if ("MENU".equals(screenName)) {
                menuPanel.scrollToTop();
            } else if ("REGISTER".equals(screenName)) {
                registerPanel.scrollToTop();
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(() -> {
        });
    }
}