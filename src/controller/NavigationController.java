package controller;

/**
 * Thin interface the views use to navigate between screens without having to
 * know about each other. Implemented by {@code view.MainFrame}.
 */
public interface NavigationController {
    void showMainMenu();
    void showGameSetup();
    void showLoadGame();
    void showSettings();
    void showLeaderboard();
    void showRules();
    void showGame();
    void showGameOver();
}
