package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.SystemNotificationDao;
import com.scolaris.dao.impl.SystemNotificationDaoImpl;
import com.scolaris.model.Subject;
import com.scolaris.model.SystemNotification;
import com.scolaris.model.Teacher;
import com.scolaris.model.User;
import com.scolaris.service.AuthenticationService;
import com.scolaris.service.ModuleResourceService;
import com.scolaris.service.ModuleSubjectService;
import com.scolaris.service.TeacherService;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherHomeController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherHomeController.class);

    @FXML private Label profileNameLabel;
    @FXML private Label profileCinLabel;
    
    @FXML private Label fullNameLabel;
    @FXML private Label cinLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label specialtyLabel;
    
    @FXML private Label totalSubjectsLabel;
    @FXML private Label totalModulesLabel;
    @FXML private Label totalResourcesLabel;
    
    @FXML private StackPane badgeContainer;
    @FXML private Label badgeCountLabel;
    @FXML private StackPane notificationBellContainer;
    @FXML private StackPane topProfileImageContainer;
    @FXML private HBox userProfileContainer;

    private TeacherService teacherService;
    private ModuleSubjectService moduleSubjectService;
    private ModuleResourceService resourceService;
    
    private Teacher currentTeacher;

    public void initialize() {
        teacherService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.TeacherService.class);
        moduleSubjectService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleSubjectService.class);
        resourceService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleResourceService.class);
        
        loadTeacherAndProfile();
    }

    private void loadTeacherAndProfile() {
        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;
        
        String username = user.getUsername();
        List<Teacher> teachers = teacherService.getAllTeachers();
        for (Teacher t : teachers) {
            if (t.getCin().equals(username)) {
                currentTeacher = t;
                break;
            }
        }
        
        if (currentTeacher != null) {
            displayTeacherInfo();
            calculateStats();
            updateNotificationsBadge();
        }
    }

    private void displayTeacherInfo() {
        String fullName = currentTeacher.getLastName().toUpperCase() + " " + currentTeacher.getFirstName();
        profileNameLabel.setText(fullName);
        profileCinLabel.setText("CIN: " + currentTeacher.getCin());
        
        if (topProfileImageContainer != null) {
            javafx.scene.image.Image img = com.scolaris.util.ProfileImageUtil.getProfileImage(currentTeacher.getCin());
            if (img != null) {
                topProfileImageContainer.getChildren().clear();
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                imgView.setFitWidth(32);
                imgView.setFitHeight(32);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(16, 16, 16);
                imgView.setClip(clip);
                topProfileImageContainer.getChildren().add(imgView);
            }
        }
        
        fullNameLabel.setText(fullName);
        cinLabel.setText(currentTeacher.getCin());
        emailLabel.setText(currentTeacher.getEmail() != null ? currentTeacher.getEmail() : "-");
        phoneLabel.setText(currentTeacher.getPhone() != null ? currentTeacher.getPhone() : "-");
        specialtyLabel.setText(currentTeacher.getSpecialty() != null ? currentTeacher.getSpecialty() : "-");
    }

    private void calculateStats() {
        int teacherId = currentTeacher.getId();
        
        // Subjects & Modules
        List<Subject> subjects = moduleSubjectService.getSubjectsByTeacher(teacherId);
        totalSubjectsLabel.setText(String.valueOf(subjects.size()));
        
        long modulesCount = subjects.stream()
                                    .map(Subject::getModuleId)
                                    .distinct()
                                    .count();
        totalModulesLabel.setText(String.valueOf(modulesCount));
        
        // Resources
        long resourcesCount = resourceService.getAllResources().stream()
                                             .filter(r -> currentTeacher.getCin().equals(r.getUploadedBy()))
                                             .count();
        totalResourcesLabel.setText(String.valueOf(resourcesCount));
    }

    private void updateNotificationsBadge() {
        try {
            if (currentTeacher == null) { badgeContainer.setVisible(false); return; }
            SystemNotificationDao notifDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SystemNotificationDao.class);
            List<SystemNotification> notifications = notifDao.findForTeacher(currentTeacher.getCin());
            if (notifications.isEmpty()) {
                badgeContainer.setVisible(false);
            } else {
                badgeCountLabel.setText(String.valueOf(notifications.size()));
                badgeContainer.setVisible(true);
            }
        } catch (Exception e) {
            logger.error("Error occurred in TeacherHomeController", e);
            badgeContainer.setVisible(false);
        }
    }

    @FXML
    private void handleOpenNotificationsPopup(MouseEvent event) {
        if (currentTeacher == null || notificationBellContainer.getScene() == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupRoot = new VBox(0);
        popupRoot.setStyle("-fx-background-color: white; " +
                           "-fx-background-radius: 14; " +
                           "-fx-border-color: #E4E7EC; " +
                           "-fx-border-width: 1; " +
                           "-fx-border-radius: 14; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 24, 0.1, 0, 8); " +
                           "-fx-padding: 0; " +
                           "-fx-min-width: 340; " +
                           "-fx-max-width: 380; " +
                           "-fx-max-height: 440;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #0D9488, #14B8A6); " +
                        "-fx-padding: 14 16; " +
                        "-fx-background-radius: 13 13 0 0;");
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView bellIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        bellIcon.setGlyphName("BELL");
        bellIcon.setSize("16");
        bellIcon.setStyle("-fx-fill: white;");
        Label headerTitle = new Label("  Notifications");
        headerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView closeIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        closeIcon.setGlyphName("TIMES");
        closeIcon.setSize("14");
        closeIcon.setStyle("-fx-fill: rgba(255,255,255,0.7); -fx-cursor: hand;");
        closeIcon.setOnMouseClicked(e -> popup.hide());
        header.getChildren().addAll(bellIcon, headerTitle, headerSpacer, closeIcon);
        popupRoot.getChildren().add(header);
        
        try {
            popupRoot.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load stylesheet for popup");
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox itemsBox = new VBox(0);

        SystemNotificationDao notifDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SystemNotificationDao.class);
        List<SystemNotification> notifications = notifDao.findForTeacher(currentTeacher.getCin());

        if (notifications.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 35 20;");
            de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView emptyIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
            emptyIcon.setGlyphName("BELL_SLASH");
            emptyIcon.setSize("32");
            emptyIcon.setStyle("-fx-fill: #D0D5DD;");
            Label emptyLbl = new Label("Aucune notification");
            emptyLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-size: 13px;");
            Label emptySubLbl = new Label("Vous êtes à jour !");
            emptySubLbl.setStyle("-fx-text-fill: #D0D5DD; -fx-font-size: 11px;");
            emptyBox.getChildren().addAll(emptyIcon, emptyLbl, emptySubLbl);
            itemsBox.getChildren().add(emptyBox);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (SystemNotification notif : notifications) {
                HBox itemCard = new HBox(10);
                itemCard.setPickOnBounds(true);
                itemCard.setAlignment(Pos.CENTER_LEFT);
                itemCard.setStyle("-fx-padding: 14 16; " +
                                  "-fx-border-color: transparent transparent #F7F8FA transparent; " +
                                  "-fx-border-width: 0 0 1 0; " +
                                  "-fx-background-color: transparent;");

                StackPane iconWrapper = new StackPane();
                String titleLower = notif.getTitle().toLowerCase();
                if (titleLower.contains("approuv") || titleLower.contains("accept")) {
                    iconWrapper.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 8; -fx-padding: 8;");
                } else if (titleLower.contains("refus") || titleLower.contains("rejet")) {
                    iconWrapper.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-padding: 8;");
                } else {
                    iconWrapper.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 8; -fx-padding: 8;");
                }
                de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView notifIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
                notifIcon.setGlyphName("BELL");
                notifIcon.setSize("14");
                notifIcon.setStyle("-fx-fill: #667085;");
                iconWrapper.getChildren().add(notifIcon);

                VBox textBox = new VBox(3);
                HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);
                Label titleLbl = new Label(notif.getTitle());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 12px;");
                titleLbl.setWrapText(true);
                Label contentLbl = new Label(notif.getContent());
                contentLbl.setStyle("-fx-text-fill: #667085; -fx-font-size: 11px;");
                contentLbl.setWrapText(true);
                String dateStr = notif.getCreatedAt() != null ? notif.getCreatedAt().format(formatter) : "";
                Label dateLbl = new Label(dateStr);
                dateLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-size: 10px;");
                textBox.getChildren().addAll(titleLbl, contentLbl, dateLbl);

                iconWrapper.setMouseTransparent(true);
                textBox.setMouseTransparent(true);
                itemCard.getChildren().addAll(iconWrapper, textBox);
                itemCard.setOnMouseEntered(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-background-color: #F7F8FA; -fx-cursor: hand; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));
                itemCard.setOnMouseExited(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-background-color: transparent; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));

                itemsBox.getChildren().add(itemCard);
            }
        }

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #F7F8FA; " +
                        "-fx-padding: 10 16; " +
                        "-fx-background-radius: 0 0 13 13; " +
                        "-fx-border-color: #E4E7EC transparent transparent transparent; " +
                        "-fx-border-width: 1 0 0 0;");
        Label footerLbl = new Label("Fermer");
        footerLbl.setStyle("-fx-text-fill: #0D9488; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        footerLbl.setOnMouseEntered(e -> footerLbl.setStyle("-fx-text-fill: #0F766E; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;"));
        footerLbl.setOnMouseExited(e -> footerLbl.setStyle("-fx-text-fill: #0D9488; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;"));
        footer.getChildren().add(footerLbl);
        footer.setOnMouseClicked(e -> popup.hide());

        scrollPane.setContent(itemsBox);
        popupRoot.getChildren().addAll(scrollPane, footer);
        popup.getContent().add(popupRoot);

        Bounds bounds = notificationBellContainer.localToScreen(notificationBellContainer.getBoundsInLocal());
        double popupWidth = 380;
        double screenX = Math.max(10, bounds.getMaxX() - popupWidth);
        double screenY = bounds.getMaxY() + 10;
        popup.show(notificationBellContainer.getScene().getWindow(), screenX, screenY);
    }
    
    @FXML
    private void handleOpenProfilePopup(MouseEvent event) {
        if (currentTeacher == null || userProfileContainer == null || userProfileContainer.getScene() == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupRoot = new VBox(10);
        popupRoot.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #E4E7EC; -fx-border-width: 1; -fx-border-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5); -fx-padding: 15; -fx-min-width: 220;");

        // Profile Info
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView userIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        userIcon.setGlyphName("USER_MD");
        userIcon.setSize("36");
        userIcon.setStyle("-fx-fill: #0D9488;");
        
        VBox texts = new VBox(2);
        Label nameLbl = new Label(currentTeacher.getFirstName() + " " + currentTeacher.getLastName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 14px;");
        Label roleLbl = new Label("Enseignant");
        roleLbl.setStyle("-fx-text-fill: #667085; -fx-font-size: 11px;");
        texts.getChildren().addAll(nameLbl, roleLbl);
        
        infoBox.getChildren().addAll(userIcon, texts);

        javafx.scene.shape.Line separator1 = new javafx.scene.shape.Line(0, 0, 190, 0);
        separator1.setStroke(javafx.scene.paint.Color.web("#E4E7EC"));

        // Settings option
        HBox settingsItem = createPopupItem("COG", "Paramètres du compte");
        settingsItem.setOnMouseClicked(e -> {
            popup.hide();
            com.scolaris.util.SettingsDialog.show(() -> {
                User current = AuthenticationService.getCurrentUser();
                if (current != null && topProfileImageContainer != null) {
                    javafx.scene.image.Image img = com.scolaris.util.ProfileImageUtil.getProfileImage(current.getUsername());
                    if (img != null) {
                        topProfileImageContainer.getChildren().clear();
                        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                        imgView.setFitWidth(32);
                        imgView.setFitHeight(32);
                        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(16, 16, 16);
                        imgView.setClip(clip);
                        topProfileImageContainer.getChildren().add(imgView);
                    }
                }
            });
        });

        // Logout option
        HBox logoutItem = createPopupItem("SIGN_OUT", "Se déconnecter");
        logoutItem.setOnMouseClicked(e -> {
            popup.hide();
            com.scolaris.util.ModernDialog.showConfirm("Déconnexion", "Voulez-vous vraiment vous déconnecter ?", confirmed -> {
                if (confirmed) {
                    AuthenticationService.logout();
                    try {
                        javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
                        javafx.stage.Stage stage = (javafx.stage.Stage) userProfileContainer.getScene().getWindow();
                        stage.setScene(scene);
                        stage.centerOnScreen();
                    } catch (Exception ex) {
                        logger.error("Erreur déconnexion", ex);
                    }
                }
            });
        });

        popupRoot.getChildren().addAll(infoBox, separator1, settingsItem, logoutItem);
        popup.getContent().add(popupRoot);

        Bounds bounds = userProfileContainer.localToScreen(userProfileContainer.getBoundsInLocal());
        popup.show(userProfileContainer.getScene().getWindow(), bounds.getMinX() - 20, bounds.getMaxY() + 10);
    }

    private HBox createPopupItem(String iconName, String text) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 8 10; -fx-cursor: hand; -fx-background-radius: 8;");
        
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView icon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        icon.setGlyphName(iconName);
        icon.setSize("16");
        icon.setStyle("-fx-fill: #667085;");
        
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #475569; -fx-font-weight: 600;");
        
        item.getChildren().addAll(icon, label);
        
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-padding: 8 10; -fx-cursor: hand; -fx-background-radius: 8; -fx-background-color: #F7F8FA;");
            icon.setStyle("-fx-fill: #0D9488;");
            label.setStyle("-fx-text-fill: #0D9488;");
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-padding: 8 10; -fx-cursor: hand; -fx-background-radius: 8; -fx-background-color: transparent;");
            icon.setStyle("-fx-fill: #667085;");
            label.setStyle("-fx-text-fill: #475569;");
        });
        
        return item;
    }
}
