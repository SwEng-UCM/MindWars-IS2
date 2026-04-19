package controller;

import model.User;
import persistence.PasswordUtil;
import persistence.UserRepository;
import player.Player;
import ui.MenuPanel;

public class LoginController {

    private MenuPanel ui;
    private UserRepository repo;

    public LoginController(MenuPanel ui, UserRepository repo) {
        this.ui = ui;
        this.repo = repo;
    }

    public void login() {
        String email = ui.getEmailInput();
        String password = ui.getPasswordInput();

        String hash = PasswordUtil.hashPassword(password);

        User user = repo.login(email, hash);

        if (
            email.isEmpty() ||
            password.isEmpty() ||
            email.equals("your.email@example.com")
        ) {
            ui.showError("Please enter valid credentials");
            return;
        }

        if (user == null) {
            ui.showError("Invalid email or password");
            return;
        }

        Player player = new Player(user.getUsername(), user.getId());
        ui.getParentWindow().setSessionPlayer(player);

        ui.showSuccess("Welcome " + user.getUsername());

        ui.getParentWindow().startGameSession();
    }
}
