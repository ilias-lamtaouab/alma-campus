package com.scolaris.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfileImageUtil {

    private static final String PROFILE_DIR = System.getProperty("user.home") + "/.scolaris/profiles/";

    static {
        File dir = new File(PROFILE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void saveProfileImage(String username, File imageFile) throws Exception {
        if (imageFile == null) return;
        Path dest = Paths.get(PROFILE_DIR + username + ".png");
        Files.copy(imageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
    }

    public static Image getProfileImage(String username) {
        File file = new File(PROFILE_DIR + username + ".png");
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }
        return null;
    }

    public static Node getProfileNode(String username, double size) {
        Image img = getProfileImage(username);
        if (img != null) {
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            imageView.setClip(clip);
            return imageView;
        } else {
            FontAwesomeIconView icon = new FontAwesomeIconView();
            icon.setGlyphName("USER_CIRCLE");
            icon.setSize(String.valueOf((int)size));
            icon.setStyle("-fx-fill: #0D9488;");
            return icon;
        }
    }
}
