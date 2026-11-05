package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.MainApp;
import com.scolaris.service.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private AuthenticationService authService;

    public void initialize() {
        authService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.AuthenticationService.class);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        boolean isAuthenticated = authService.authenticate(username, password);

        if (isAuthenticated) {
            try {
                MainApp.setRoot("dashboard");
            } catch (IOException e) {
                logger.error("Error occurred in LoginController", e);
                showError("Erreur lors du chargement du tableau de bord.");
            }
        } else {
            showError("Identifiants incorrects ou utilisateur inexistant.");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            MainApp.setRoot("register");
        } catch (IOException e) {
            logger.error("Error occurred in LoginController", e);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
