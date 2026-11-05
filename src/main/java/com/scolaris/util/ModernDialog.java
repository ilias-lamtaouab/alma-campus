package com.scolaris.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.function.Consumer;

public class ModernDialog {

    public enum Type {
        INFO("INFO_CIRCLE", "#3B82F6"),
        SUCCESS("CHECK_CIRCLE", "#10B981"),
        WARNING("EXCLAMATION_TRIANGLE", "#F59E0B"),
        ERROR("TIMES_CIRCLE", "#EF4444");

        String iconName;
        String color;

        Type(String iconName, String color) {
            this.iconName = iconName;
            this.color = color;
        }
    }

    public static void showMessage(String title, String message, Type type) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = createDialogLayout(title, message, type);
        
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("primary-btn");
        okButton.setOnAction(e -> {
            closeWithAnimation(dialogStage, root);
        });

        HBox btnBox = new HBox(okButton);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(20, 0, 0, 0));
        root.getChildren().add(btnBox);

        Scene scene = new Scene(root);
        scene.setFill(null);
        try {
            scene.getStylesheets().add(ModernDialog.class.getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        
        dialogStage.setScene(scene);
        
        AnimationUtils.animateNodeAppearance(root);
        dialogStage.showAndWait();
    }

    public static void showConfirm(String title, String message, Consumer<Boolean> onResult) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = createDialogLayout(title, message, Type.WARNING);
        
        Button yesButton = new Button("Confirm");
        yesButton.getStyleClass().addAll("button", "btn-danger");
        yesButton.setOnAction(e -> {
            closeWithAnimation(dialogStage, root);
            onResult.accept(true);
        });

        Button noButton = new Button("Cancel");
        noButton.getStyleClass().addAll("button", "btn-secondary");
        noButton.setOnAction(e -> {
            closeWithAnimation(dialogStage, root);
            onResult.accept(false);
        });

        HBox btnBox = new HBox(10, noButton, yesButton);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(20, 0, 0, 0));
        root.getChildren().add(btnBox);

        Scene scene = new Scene(root);
        scene.setFill(null);
        try {
            scene.getStylesheets().add(ModernDialog.class.getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        
        dialogStage.setScene(scene);
        
        AnimationUtils.animateNodeAppearance(root);
        dialogStage.showAndWait();
    }

    private static VBox createDialogLayout(String title, String message, Type type) {
        VBox root = new VBox(15);
        root.getStyleClass().add("dialog-pane");
        root.setPadding(new Insets(30));
        root.setPrefWidth(400);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName(type.iconName);
        icon.setSize("24");
        icon.setStyle("-fx-fill: " + type.color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        header.getChildren().addAll(icon, titleLabel);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563; -fx-line-spacing: 4px;");

        root.getChildren().addAll(header, messageLabel);
        return root;
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
