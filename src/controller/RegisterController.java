package controller;

import model.User;
import persistence.PasswordUtil;
import persistence.UserRepository;
import player.Player;
import view.RegisterPanel;

import javax.swing.*;

public class RegisterController {
    private RegisterPanel ui;
    private UserRepository userRepo;

    public RegisterController(RegisterPanel ui, UserRepository userRepo) {
        this.ui = ui;
        this.userRepo = userRepo;
    }

    public void register() {
        System.out.println("RegisterController.register() called");

        String username = ui.getUsernameInput();
        String email = ui.getEmailInput();
        String password = ui.getPasswordInput();
        String confirmPassword = ui.getConfirmPasswordInput();

        // validation
        if (username.isEmpty() || username.equals("Choose a username") || email.isEmpty()
                || email.equals("email@example.com")) {ui.showError("Please enter a valid email address");
            return;
        }

        if (!password.equals(confirmPassword)) { ui.showError("Passwords do not match");
            return;
        }

        if (userRepo.usernameExists(username)) {
            ui.showError("Username already exists!");
            return;
        }

        if (userRepo.emailExists(email)) {
            ui.showError("Email already exists!");
            return;
        }

        try{
            String hash = PasswordUtil.hashPassword(password);
            User user = userRepo.registerUser(username, email, hash);

            Player player = new Player(user.getUsername(), user.getId());
            player.setSymbol('X');

            // create player object
            ui.getParentWindow().setSessionPlayer(player);
            ui.showSuccess("Account created successfully! Welcome " + username);
            ui.clearFields();
            ui.getParentWindow().showScreen("MENU");
        } catch(Exception e){
            e.printStackTrace();
            ui.showError("Could not create account" + e.getMessage());
        }


    }
}
