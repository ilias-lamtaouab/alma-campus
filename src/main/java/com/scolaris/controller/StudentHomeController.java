package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.*;
import com.scolaris.dao.impl.*;
import com.scolaris.model.*;
import com.scolaris.model.Module;
import com.scolaris.service.AuthenticationService;
import com.scolaris.service.GradeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentHomeController {

    private static final Logger logger = LoggerFactory.getLogger(StudentHomeController.class);


    @FXML private Label profileNameLabel;
    @FXML private Label profileCneLabel;
    
    @FXML private Label fullNameLabel;
    @FXML private Label cneLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label averageLabel;
    @FXML private Label validatedModulesLabel;
    @FXML private Label enrolledCoursesLabel;
    @FXML private Label statusLabel;
    
    @FXML private TableView<Grade> recentGradesTable;
    @FXML private TableColumn<Grade, String> colSubName;
    @FXML private TableColumn<Grade, String> colEvalType;
    @FXML private TableColumn<Grade, Double> colGradeValue;
    @FXML private TableColumn<Grade, String> colGradeDate;
    @FXML private VBox announcementsBox;

    @FXML private StackPane badgeContainer;
    @FXML private Label badgeCountLabel;
    @FXML private StackPane notificationBellContainer;
    @FXML private StackPane topProfileImageContainer;

    private StudentDao studentDao;
    private GradeDao gradeDao;
    private SubjectDao subjectDao;
    private ModuleDao moduleDao;
    private GradeService gradeService;
    
    private Student currentStudent;

    public void initialize() {
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        gradeDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.GradeDao.class);
        subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        gradeService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.GradeService.class);
        
        loadStudentAndProfile();
    }

    private void loadStudentAndProfile() {
        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;
        
        String username = user.getUsername();
        Optional<Student> optStudent = studentDao.findByCne(username);
        
        // AUTO-PROFILING AND GRADE SEEDING SAFEGUARD:
        if (optStudent.isEmpty()) {
            System.out.println("[StudentHome] Creating mock student profile for user: " + username);
            Student newStudent = new Student(
                0, 
                username, 
                username, 
                "Étudiant", 
                LocalDate.of(2004, 5, 12), 
                username + "@scolaris.ma", 
                "0611223344", 
                LocalDate.now(), 
                "ACTIVE"
            );
            Student savedStudent = studentDao.save(newStudent);
            if (savedStudent != null) {
                optStudent = studentDao.findByCne(username);
                if (optStudent.isPresent()) {
                    Student created = optStudent.get();
                    // Seed mock grades for this student
                    List<Subject> subjects = subjectDao.findAll();
                    if (!subjects.isEmpty()) {
                        gradeDao.save(new Grade(0, created.getId(), subjects.get(0).getId(), 15.5, "CC", LocalDate.now().minusDays(5)));
                        gradeDao.save(new Grade(0, created.getId(), subjects.get(0).getId(), 14.0, "EXAM", LocalDate.now().minusDays(1)));
                        if (subjects.size() > 1) {
                            gradeDao.save(new Grade(0, created.getId(), subjects.get(1).getId(), 12.0, "CC", LocalDate.now().minusDays(4)));
                            gradeDao.save(new Grade(0, created.getId(), subjects.get(1).getId(), 13.5, "EXAM", LocalDate.now().minusDays(2)));
                        }
                        if (subjects.size() > 2) {
                            gradeDao.save(new Grade(0, created.getId(), subjects.get(2).getId(), 10.0, "CC", LocalDate.now().minusDays(3)));
                            gradeDao.save(new Grade(0, created.getId(), subjects.get(2).getId(), 11.0, "EXAM", LocalDate.now().minusDays(1)));
                        }
                    }
                }
            }
        }
        
        if (optStudent.isPresent()) {
            currentStudent = optStudent.get();
            displayStudentInfo();
            calculateStats();
            loadRecentGrades();
            loadAnnouncements();
            updateNotificationsBadge();
        }
    }

    private void displayStudentInfo() {
        String fullName = currentStudent.getLastName().toUpperCase() + " " + currentStudent.getFirstName();
        profileNameLabel.setText(fullName);
        profileCneLabel.setText("CNE: " + currentStudent.getCne());
        
        if (topProfileImageContainer != null) {
            javafx.scene.image.Image img = com.scolaris.util.ProfileImageUtil.getProfileImage(currentStudent.getCne());
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
        cneLabel.setText(currentStudent.getCne());
        emailLabel.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "-");
        phoneLabel.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "-");
    }

    private void calculateStats() {
        int studentId = currentStudent.getId();
        
        // 1. General Average
        double genAvg = gradeService.calculateGeneralAverage(studentId);
        averageLabel.setText(String.format("%.2f / 20", genAvg));
        
        // 2. Academic Status
        if (genAvg >= 10.0) {
            statusLabel.setText("ADMIS");
            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); // Green
        } else {
            statusLabel.setText("AJOURNÉ");
            statusLabel.setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold;"); // Red
        }
        
        // 3. Registered courses (Subjects count in database)
        List<Subject> subjects = subjectDao.findAll();
        enrolledCoursesLabel.setText(String.valueOf(subjects.size()));
        
        // 4. Validated Modules
        List<Module> modules = moduleDao.findAll();
        int validatedCount = 0;
        for (Module mod : modules) {
            double modAvg = gradeService.calculateModuleAverage(studentId, mod.getId());
            if (modAvg >= 10.0) {
                validatedCount++;
            }
        }
        validatedModulesLabel.setText(String.valueOf(validatedCount));
    }

    private void loadRecentGrades() {
        List<Grade> grades = gradeDao.findByStudentId(currentStudent.getId());
        recentGradesTable.setItems(FXCollections.observableArrayList(grades));
        
        colSubName.setCellValueFactory(cellData -> {
            int subId = cellData.getValue().getSubjectId();
            Optional<Subject> optSub = subjectDao.findById(subId);
            return new SimpleStringProperty(optSub.map(Subject::getName).orElse("Matière Inconnue"));
        });
        
        colEvalType.setCellValueFactory(new PropertyValueFactory<>("evaluationType"));
        colGradeValue.setCellValueFactory(new PropertyValueFactory<>("gradeValue"));
        
        colGradeDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getEvaluationDate();
            if (date != null) {
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("-");
        });
    }

    private void loadAnnouncements() {
        if (currentStudent == null) return;
        
        announcementsBox.getChildren().clear();
        
        SystemNotificationDao notifDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SystemNotificationDao.class);
        List<SystemNotification> notifications = notifDao.findForStudent(currentStudent.getCne());
        
        if (notifications.isEmpty()) {
            Label noNotifLabel = new Label("Aucune annonce pour le moment.");
            noNotifLabel.setStyle("-fx-text-fill: #98A2B3; -fx-font-style: italic; -fx-font-size: 13px;");
            announcementsBox.getChildren().add(noNotifLabel);
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (SystemNotification notif : notifications) {
            VBox card = new VBox(5);
            card.getStyleClass().add("announcement-card");
            
            // Premium color styling based on keywords in title/content
            String titleLower = notif.getTitle().toLowerCase();
            String contentLower = notif.getContent().toLowerCase();
            
            if (titleLower.contains("urgent") || titleLower.contains("alerte") || titleLower.contains("important") || contentLower.contains("urgent") || contentLower.contains("rattrapage")) {
                card.getStyleClass().add("announcement-card-danger");
            } else if (titleLower.contains("success") || titleLower.contains("félicitation") || titleLower.contains("reçu") || titleLower.contains("valide")) {
                card.getStyleClass().add("announcement-card-success");
            } else if (titleLower.contains("attention") || titleLower.contains("rappel") || titleLower.contains("dernier")) {
                card.getStyleClass().add("announcement-card-warning");
            } else {
                card.getStyleClass().add("announcement-card-info");
            }
            
            Label titleLbl = new Label(notif.getTitle());
            titleLbl.getStyleClass().add("announcement-title");
            titleLbl.setWrapText(true);
            
            Label contentLbl = new Label(notif.getContent());
            contentLbl.getStyleClass().add("announcement-content");
            contentLbl.setWrapText(true);
            
            String dateStr = notif.getCreatedAt() != null ? notif.getCreatedAt().format(formatter) : "";
            Label dateLbl = new Label(dateStr);
            dateLbl.getStyleClass().add("announcement-date");
            
            card.getChildren().addAll(titleLbl, contentLbl, dateLbl);
            announcementsBox.getChildren().add(card);
        }
    }

    private void updateNotificationsBadge() {
        try {
            if (currentStudent == null) { badgeContainer.setVisible(false); return; }
            SystemNotificationDao notifDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SystemNotificationDao.class);
            List<SystemNotification> notifications = notifDao.findForStudent(currentStudent.getCne());
            if (notifications.isEmpty()) {
                badgeContainer.setVisible(false);
            } else {
                badgeCountLabel.setText(String.valueOf(notifications.size()));
                badgeContainer.setVisible(true);
            }
        } catch (Exception e) {
            logger.error("Error occurred in StudentHomeController", e);
            badgeContainer.setVisible(false);
        }
    }

    @FXML
    private void handleOpenNotificationsPopup(MouseEvent event) {
        if (currentStudent == null || notificationBellContainer.getScene() == null) return;

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
        List<SystemNotification> notifications = notifDao.findForStudent(currentStudent.getCne());

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
                
                itemCard.setOnMouseClicked(e -> {
                    popup.hide();
                    DashboardController.getInstance().showCertificatesView();
                });

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
        Label footerLbl = new Label("Voir les annonces");
        footerLbl.setStyle("-fx-text-fill: #0D9488; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        footerLbl.setOnMouseEntered(e -> footerLbl.setStyle("-fx-text-fill: #0F766E; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;"));
        footerLbl.setOnMouseExited(e -> footerLbl.setStyle("-fx-text-fill: #0D9488; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;"));
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView footerArrow = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        footerArrow.setGlyphName("ARROW_RIGHT");
        footerArrow.setSize("11");
        footerArrow.setStyle("-fx-fill: #0D9488;");
        footer.getChildren().addAll(footerLbl, footerArrow);
        
        footer.setOnMouseClicked(e -> {
            popup.hide();
            DashboardController.getInstance().showCertificatesView();
        });

        scrollPane.setContent(itemsBox);
        popupRoot.getChildren().addAll(scrollPane, footer);
        popup.getContent().add(popupRoot);

        Bounds bounds = notificationBellContainer.localToScreen(notificationBellContainer.getBoundsInLocal());
        double popupWidth = 380;
        double screenX = Math.max(10, bounds.getMaxX() - popupWidth);
        double screenY = bounds.getMaxY() + 10;
        popup.show(notificationBellContainer.getScene().getWindow(), screenX, screenY);
    }
    
    @FXML private HBox userProfileContainer;

    @FXML
    private void handleOpenProfilePopup(MouseEvent event) {
        if (currentStudent == null || userProfileContainer == null || userProfileContainer.getScene() == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupRoot = new VBox(10);
        popupRoot.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #E4E7EC; -fx-border-width: 1; -fx-border-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5); -fx-padding: 15; -fx-min-width: 220;");

        // Profile Info
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView userIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        userIcon.setGlyphName("GRADUATION_CAP");
        userIcon.setSize("36");
        userIcon.setStyle("-fx-fill: #0D9488;");
        
        VBox texts = new VBox(2);
        Label nameLbl = new Label(currentStudent.getFirstName() + " " + currentStudent.getLastName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 14px;");
        Label roleLbl = new Label("Étudiant");
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
                User current = com.scolaris.service.AuthenticationService.getCurrentUser();
                if (current != null) {
                    if (topProfileImageContainer != null) {
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
                }
            });
        });

        // Logout option
        HBox logoutItem = createPopupItem("SIGN_OUT", "Se déconnecter");
        logoutItem.setOnMouseClicked(e -> {
            popup.hide();
            com.scolaris.util.ModernDialog.showConfirm("Déconnexion", "Voulez-vous vraiment vous déconnecter ?", confirmed -> {
                if (confirmed) {
                    com.scolaris.service.AuthenticationService.logout();
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
