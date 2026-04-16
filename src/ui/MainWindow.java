package ui;

import controller.RegisterController;
import persistence.DatabaseInitializer;
import persistence.UserRepository;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private player.Player sessionPlayer;

    public MainWindow() {
        setTitle("MindWars Trivia - Welcome");
        setSize(1000, 700); // Frame size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

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

    public void setSessionPlayer(player.Player p) {
        this.sessionPlayer = p;
    }

    public player.Player getSessionPlayer() {
        return sessionPlayer;
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        // Run Swing components on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow mw = new MainWindow();
            mw.setVisible(true);
        });
    }
}