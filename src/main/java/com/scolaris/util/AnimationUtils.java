package com.scolaris.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.util.Duration;

public class AnimationUtils {

    public static void animateNodeAppearance(Node node) {
        node.setOpacity(0.0);
        node.setTranslateY(15.0);
        node.setScaleX(0.98);
        node.setScaleY(0.98);

        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(350), node);
        slide.setToY(0.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(350), node);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition parallel = new ParallelTransition(fade, slide, scale);
        // Standard easing for modern "snappy but smooth" feel
        parallel.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));
        parallel.play();
    }
    
    public static void applyHoverAnimation(Node node) {
        node.setOnMouseEntered(e -> {
            TranslateTransition hover = new TranslateTransition(Duration.millis(200), node);
            hover.setToY(-2);
            hover.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            hover.play();
        });
        node.setOnMouseExited(e -> {
            TranslateTransition hover = new TranslateTransition(Duration.millis(200), node);
            hover.setToY(0);
            hover.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            hover.play();
        });
    }

    public static void applyPressAnimation(Node node) {
        node.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
            scale.setToX(0.96);
            scale.setToY(0.96);
            scale.play();
        });
        node.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    public static void bindAnimationsToAllButtons(Parent root) {
        if (root == null) return;
        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof ButtonBase) {
                applyHoverAnimation(node);
                applyPressAnimation(node);
            } else if (node instanceof Parent) {
                bindAnimationsToAllButtons((Parent) node);
            }
        }
    }
}
