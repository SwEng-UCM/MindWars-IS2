package ui;

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

        mainContainer.add(new MenuPanel(this), "MENU");
        mainContainer.add(new RegisterPanel(this), "REGISTER");

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
        // Run Swing components on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow mw = new MainWindow();
            mw.setVisible(true);
        });
    }
}