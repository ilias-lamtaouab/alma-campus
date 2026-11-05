package com.scolaris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);


    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Global Uncaught Exception Handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread " + thread.getName(), throwable);
            javafx.application.Platform.runLater(() -> {
                com.scolaris.util.ModernDialog.showMessage(
                    "Erreur Critique",
                    "Une erreur inattendue s'est produite : " + throwable.getMessage(),
                    com.scolaris.util.ModernDialog.Type.ERROR
                );
            });
        });
        
        // Seeding database with clean sample data if empty
        try {
            com.scolaris.util.DatabaseSeeder.seedIfEmpty();
        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
        }
        
        // We will load the login screen initially
        try {
            URL resource = getClass().getResource("/fxml/login.fxml");
            if (resource == null) {
                System.err.println("Cannot find login.fxml");
                // Fallback to simple scene if UI not built yet
                Parent root = new javafx.scene.layout.VBox();
                Scene scene = new Scene(root, 800, 600);
                stage.setScene(scene);
            } else {
                Parent root = FXMLLoader.load(resource);
                com.scolaris.util.AnimationUtils.bindAnimationsToAllButtons(root);
                Scene scene = new Scene(root, 1000, 700);
                
                URL css = getClass().getResource("/css/style.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
                
                stage.setScene(scene);
            }
            stage.setTitle("Alma Campus - Gestion de Scolarité");
            try {
                URL iconUrl = getClass().getResource("/logo.png");
                if (iconUrl != null) {
                    stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
                }
            } catch (Exception iconEx) {
                logger.warn("Could not load application icon", iconEx);
            }
            stage.show();
        } catch (Exception e) {
            logger.error("Error occurred in MainApp", e);
        }
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/" + fxml + ".fxml"));
        com.scolaris.util.AnimationUtils.bindAnimationsToAllButtons(root);
        root.setOpacity(0.0);
        root.setScaleX(0.97);
        root.setScaleY(0.97);
        primaryStage.getScene().setRoot(root);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), root);
        fade.setToValue(1.0);
        
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(400), root);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition(fade, scale);
        parallel.play();
    }

    public static void main(String[] args) {
        java.util.Locale.setDefault(java.util.Locale.FRANCE);
        launch();
    }
}
