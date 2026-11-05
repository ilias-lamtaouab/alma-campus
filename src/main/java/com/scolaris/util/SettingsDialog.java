package com.scolaris.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import com.scolaris.service.AuthenticationService;
import com.scolaris.model.User;
import com.scolaris.dao.UserDao;
import com.scolaris.dao.impl.UserDaoImpl;
import java.util.Optional;
import javafx.scene.Node;
import java.io.File;

public class SettingsDialog {

    public static void show(Runnable onSuccess) {
        User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser == null) return;

        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.getStyleClass().add("dialog-pane");
        root.setPadding(new Insets(30));
        root.setPrefWidth(450);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("COG");
        icon.setSize("24");
        icon.setStyle("-fx-fill: #0D9488;");

        Label titleLabel = new Label("Paramètres du compte");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        FontAwesomeIconView closeIcon = new FontAwesomeIconView();
        closeIcon.setGlyphName("TIMES");
        closeIcon.setSize("16");
        closeIcon.setStyle("-fx-fill: #9CA3AF; -fx-cursor: hand;");
        closeIcon.setOnMouseClicked(e -> closeWithAnimation(dialogStage, root));

        header.getChildren().addAll(icon, titleLabel, spacer, closeIcon);

        VBox form = new VBox(15);
        
        // Profile Picture
        HBox picBox = new HBox(15);
        picBox.setAlignment(Pos.CENTER_LEFT);
        Node currentProfilePic = ProfileImageUtil.getProfileNode(currentUser.getUsername(), 50);
        
        VBox picTexts = new VBox(5);
        Label picLabel = new Label("Photo de profil");
        picLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
        Button changePicBtn = new Button("Changer la photo");
        changePicBtn.setStyle("-fx-background-color: #F7F8FA; -fx-text-fill: #475569; -fx-cursor: hand; -fx-border-color: #CBD5E1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        final java.util.concurrent.atomic.AtomicReference<File> selectedImage = new java.util.concurrent.atomic.AtomicReference<>(null);
        
        changePicBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Choisir une photo de profil");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {
                selectedImage.set(file);
                changePicBtn.setText(file.getName());
            }
        });
        
        picTexts.getChildren().addAll(picLabel, changePicBtn);
        picBox.getChildren().addAll(currentProfilePic, picTexts);

        // Username / Email
        VBox usernameBox = new VBox(5);
        Label userLabel = new Label("Nom d'utilisateur / Email");
        userLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
        TextField userField = new TextField(currentUser.getUsername());
        userField.getStyleClass().add("text-field");
        usernameBox.getChildren().addAll(userLabel, userField);
        
        // Password
        VBox pwdBox = new VBox(5);
        Label pwdLabel = new Label("Nouveau mot de passe (laisser vide pour ne pas changer)");
        pwdLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
        PasswordField pwdField = new PasswordField();
        pwdField.getStyleClass().add("text-field");
        pwdBox.getChildren().addAll(pwdLabel, pwdField);

        form.getChildren().addAll(picBox, usernameBox, pwdBox);

        Button saveBtn = new Button("Enregistrer les modifications");
        saveBtn.getStyleClass().add("primary-btn");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        
        saveBtn.setOnAction(e -> {
            String newUsername = userField.getText().trim();
            String newPwd = pwdField.getText();
            
            if (newUsername.isEmpty()) {
                ModernDialog.showMessage("Erreur", "Le nom d'utilisateur ne peut pas être vide.", ModernDialog.Type.ERROR);
                return;
            }
            
            try {
                if (selectedImage.get() != null) {
                    ProfileImageUtil.saveProfileImage(newUsername, selectedImage.get());
                }
            } catch (Exception ex) {
                ModernDialog.showMessage("Erreur", "Erreur lors de la sauvegarde de l'image.", ModernDialog.Type.ERROR);
            }
            
            UserDao userDao = new UserDaoImpl();
            Optional<User> userOpt = userDao.findByUsername(currentUser.getUsername());
            if (userOpt.isPresent()) {
                User dbUser = userOpt.get();
                dbUser.setUsername(newUsername);
                if (!newPwd.isEmpty()) {
                    String hashedPwd = org.mindrot.jbcrypt.BCrypt.hashpw(newPwd, org.mindrot.jbcrypt.BCrypt.gensalt(10));
                    dbUser.setPasswordHash(hashedPwd);
                    currentUser.setPasswordHash(hashedPwd);
                }
                userDao.update(dbUser);
                currentUser.setUsername(newUsername);
                
                if (onSuccess != null) {
                    onSuccess.run();
                }
                
                ModernDialog.showMessage("Succès", "Vos paramètres ont été mis à jour avec succès.", ModernDialog.Type.SUCCESS);
                closeWithAnimation(dialogStage, root);
            } else {
                ModernDialog.showMessage("Erreur", "Utilisateur introuvable en base de données.", ModernDialog.Type.ERROR);
            }
        });

        root.getChildren().addAll(header, form, saveBtn);

        Scene scene = new Scene(root);
        scene.setFill(null);
        try {
            scene.getStylesheets().add(SettingsDialog.class.getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        
        dialogStage.setScene(scene);
        
        AnimationUtils.animateNodeAppearance(root);
        dialogStage.showAndWait();
    }

    private static void closeWithAnimation(Stage stage, VBox root) {
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), root);
        scale.setToX(0.8);
        scale.setToY(0.8);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), root);
        fade.setToValue(0.0);
        
        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(scale, fade);
        pt.setOnFinished(e -> stage.close());
        pt.play();
    }
}
