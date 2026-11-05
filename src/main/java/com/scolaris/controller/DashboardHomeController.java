package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.StudentDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import com.scolaris.service.AuthenticationService;
import com.scolaris.model.User;
import com.scolaris.model.CertificateRequest;
import com.scolaris.service.CertificateService;
import com.scolaris.dao.SystemNotificationDao;
import com.scolaris.dao.impl.SystemNotificationDaoImpl;
import com.scolaris.model.SystemNotification;

import java.util.List;

public class DashboardHomeController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardHomeController.class);


    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label schoolAverageLabel;
    
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colCne;
    @FXML private TableColumn<Student, String> colFirstName;
    @FXML private TableColumn<Student, String> colLastName;
    @FXML private TableColumn<Student, String> colStatus;
    
    @FXML private TextField searchField;
    @FXML private StackPane chartPane;
    
    @FXML private StackPane badgeContainer;
    @FXML private Label badgeCountLabel;
    @FXML private StackPane notificationBellContainer;
    @FXML private Label topProfileName;
    @FXML private Label topProfileRole;
    @FXML private javafx.scene.image.ImageView topProfileImage;

    private StudentDao studentDao;

    public void initialize() {
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        
        User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null) {
            if (topProfileName != null) topProfileName.setText(currentUser.getUsername());
            if (topProfileRole != null) topProfileRole.setText(currentUser.getRole());
            
            if (topProfileImage != null) {
                javafx.scene.image.Image img = com.scolaris.util.ProfileImageUtil.getProfileImage(currentUser.getUsername());
                if (img != null) {
                    topProfileImage.setImage(img);
                }
            }
        }
        
        loadRealData();
        setupChart();
        updateNotificationsBadge();
    }
    
    private void loadRealData() {
        List<Student> students = studentDao.findAll();
        totalStudentsLabel.setText(String.valueOf(students.size()));
        
        com.scolaris.service.TeacherService teacherService = new com.scolaris.service.TeacherService();
        int totalTeachers = teacherService.getAllTeachers().size();
        totalTeachersLabel.setText(String.valueOf(totalTeachers));

        com.scolaris.service.ModuleSubjectService msService = new com.scolaris.service.ModuleSubjectService();
        int totalSubjects = msService.getAllSubjects().size();
        totalSubjectsLabel.setText(String.valueOf(totalSubjects));

        com.scolaris.service.GradeService gradeService = new com.scolaris.service.GradeService();
        double schoolAvg = gradeService.calculateSchoolAverage();
        schoolAverageLabel.setText(String.format("%.2f", schoolAvg));
        
        // Take last 5 registered students (recent)
        ObservableList<Student> data = FXCollections.observableArrayList();
        if (students.size() > 5) {
            data.addAll(students.subList(students.size() - 5, students.size()));
        } else {
            data.addAll(students);
        }
        
        // Dynamic search filtering
        javafx.collections.transformation.FilteredList<Student> filteredData = new javafx.collections.transformation.FilteredList<>(data, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(student -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lower = newValue.toLowerCase();
                    if (student.getFirstName().toLowerCase().contains(lower)) return true;
                    if (student.getLastName().toLowerCase().contains(lower)) return true;
                    if (student.getCne().toLowerCase().contains(lower)) return true;
                    return false;
                });
            });
        }
        studentTable.setItems(filteredData);
        
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.equals("ACTIVE")) badge.getStyleClass().add("badge-received");
                    else if (item.equals("SUSPENDED")) badge.getStyleClass().add("badge-due");
                    else badge.getStyleClass().add("badge-request");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }
    
    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        
        com.scolaris.service.ModuleSubjectService msService = new com.scolaris.service.ModuleSubjectService();
        com.scolaris.service.GradeService gradeService = new com.scolaris.service.GradeService();
        List<com.scolaris.model.Module> modules = msService.getAllModules();
        
        if (modules.isEmpty()) {
            series1.getData().add(new XYChart.Data<>("Aucun", 0));
        } else {
            // Check cache
            @SuppressWarnings("unchecked")
            List<XYChart.Data<String, Number>> cachedData = (List<XYChart.Data<String, Number>>) com.scolaris.util.CacheManager.get("dashboard_chart_data");
            
            if (cachedData != null) {
                series1.getData().addAll(cachedData);
            } else {
                List<XYChart.Data<String, Number>> newData = new java.util.ArrayList<>();
                int count = 0;
                for (com.scolaris.model.Module mod : modules) {
                    if (count++ >= 5) break;
                    
                    List<Student> students = studentDao.findAll();
                    double modSum = 0;
                    int studentCount = 0;
                    for (Student s : students) {
                        double avg = gradeService.calculateModuleAverage(s.getId(), mod.getId());
                        if (avg > 0) {
                            modSum += avg;
                            studentCount++;
                        }
                    }
                    double modGlobalAvg = studentCount > 0 ? (modSum / studentCount) : 0;
                    newData.add(new XYChart.Data<>(mod.getCode(), Math.round(modGlobalAvg * 100.0)/100.0));
                }
                series1.getData().addAll(newData);
                com.scolaris.util.CacheManager.put("dashboard_chart_data", newData);
            }
        }
        
        barChart.getData().add(series1);
        chartPane.getChildren().add(barChart);
    }

    private void updateNotificationsBadge() {
        try {
            User currentUser = AuthenticationService.getCurrentUser();
            if (currentUser == null) {
                badgeContainer.setVisible(false);
                return;
            }
            
            long count = 0;
            if ("ADMIN".equals(currentUser.getRole())) {
                com.scolaris.service.CertificateService certificateService = new com.scolaris.service.CertificateService();
                count = certificateService.getAllRequests().stream()
                        .filter(req -> "PENDING".equals(req.getStatus()))
                        .count();
            } else if ("TEACHER".equals(currentUser.getRole())) {
                com.scolaris.dao.SystemNotificationDao notifDao = new com.scolaris.dao.impl.SystemNotificationDaoImpl();
                count = notifDao.findForTeacher(currentUser.getUsername()).size();
            }
            
            if (count > 0) {
                badgeCountLabel.setText(String.valueOf(count));
                badgeContainer.setVisible(true);
            } else {
                badgeContainer.setVisible(false);
            }
        } catch (Exception e) {
            logger.error("Error occurred in DashboardHomeController", e);
            badgeContainer.setVisible(false);
        }
    }

    private void redirectToCertificates() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().showCertificatesView();
        }
    }

    @FXML
    private void handleOpenNotificationsPopup(MouseEvent event) {
        User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser == null || notificationBellContainer.getScene() == null) return;

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

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox itemsBox = new VBox(0);

        if ("ADMIN".equals(currentUser.getRole())) {
            com.scolaris.service.CertificateService certificateService = new com.scolaris.service.CertificateService();
            List<com.scolaris.model.CertificateRequest> requests = certificateService.getAllRequests();
            List<com.scolaris.model.CertificateRequest> pendingRequests = requests.stream()
                    .filter(req -> "PENDING".equals(req.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (pendingRequests.isEmpty()) {
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
                com.scolaris.dao.StudentDao studentDao = new com.scolaris.dao.impl.StudentDaoImpl();
                for (com.scolaris.model.CertificateRequest req : pendingRequests) {
                    String studentName = "Étudiant #" + req.getStudentId();
                    java.util.Optional<com.scolaris.model.Student> optStudent = studentDao.findById(req.getStudentId());
                    if (optStudent.isPresent()) {
                        studentName = optStudent.get().getFirstName() + " " + optStudent.get().getLastName().toUpperCase();
                    }

                    HBox itemCard = new HBox(10);
                    itemCard.setPickOnBounds(true);
                    itemCard.setAlignment(Pos.CENTER_LEFT);
                    itemCard.setStyle("-fx-padding: 14 16; " +
                                      "-fx-cursor: hand; " +
                                      "-fx-border-color: transparent transparent #F7F8FA transparent; " +
                                      "-fx-border-width: 0 0 1 0; " +
                                      "-fx-background-color: transparent;");

                    StackPane iconWrapper = new StackPane();
                    iconWrapper.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 8; -fx-padding: 8;");
                    de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView notifIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
                    notifIcon.setGlyphName("FILE_TEXT");
                    notifIcon.setSize("14");
                    notifIcon.setStyle("-fx-fill: #f59e0b;");
                    iconWrapper.getChildren().add(notifIcon);

                    VBox textBox = new VBox(3);
                    HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);
                    Label titleLbl = new Label("Demande de certificat");
                    titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 12px;");
                    Label detailLbl = new Label(studentName);
                    detailLbl.setStyle("-fx-text-fill: #667085; -fx-font-size: 11px;");
                    String dateStr = req.getRequestDate() != null ? req.getRequestDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    Label dateLbl = new Label(dateStr);
                    dateLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-size: 10px;");
                    textBox.getChildren().addAll(titleLbl, detailLbl, dateLbl);

                    de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView arrowIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
                    arrowIcon.setGlyphName("CHEVRON_RIGHT");
                    arrowIcon.setSize("12");
                    arrowIcon.setStyle("-fx-fill: #D0D5DD;");

                    iconWrapper.setMouseTransparent(true);
                    textBox.setMouseTransparent(true);
                    arrowIcon.setMouseTransparent(true);
                    itemCard.getChildren().addAll(iconWrapper, textBox, arrowIcon);

                    itemCard.setOnMouseEntered(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-cursor: hand; -fx-background-color: #F7F8FA; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));
                    itemCard.setOnMouseExited(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));
                    
                    itemCard.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                        popup.hide();
                        redirectToCertificates();
                    });

                    itemsBox.getChildren().add(itemCard);
                }
            }
        } else if ("TEACHER".equals(currentUser.getRole())) {
            com.scolaris.dao.SystemNotificationDao notifDao = new com.scolaris.dao.impl.SystemNotificationDaoImpl();
            List<com.scolaris.model.SystemNotification> notifications = notifDao.findForTeacher(currentUser.getUsername());

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
                for (com.scolaris.model.SystemNotification notif : notifications) {
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
                    String dateStr = notif.getCreatedAt() != null ? notif.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
                    Label dateLbl = new Label(dateStr);
                    dateLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-size: 10px;");
                    textBox.getChildren().addAll(titleLbl, contentLbl, dateLbl);

                    iconWrapper.setMouseTransparent(true);
                    textBox.setMouseTransparent(true);
                    itemCard.getChildren().addAll(iconWrapper, textBox);

                    itemCard.setOnMouseEntered(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-background-color: #F7F8FA; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));
                    itemCard.setOnMouseExited(e -> itemCard.setStyle("-fx-padding: 14 16; -fx-background-color: transparent; -fx-border-color: transparent transparent #F7F8FA transparent; -fx-border-width: 0 0 1 0;"));

                    itemsBox.getChildren().add(itemCard);
                }
            }
        }

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #F7F8FA; " +
                        "-fx-padding: 10 16; " +
                        "-fx-background-radius: 0 0 13 13; " +
                        "-fx-border-color: #E4E7EC transparent transparent transparent; " +
                        "-fx-border-width: 1 0 0 0;");
        Label footerLbl = new Label("Voir toutes les notifications");
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
            if ("ADMIN".equals(currentUser.getRole())) {
                redirectToCertificates();
            }
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
        User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser == null || userProfileContainer == null || userProfileContainer.getScene() == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupRoot = new VBox(10);
        popupRoot.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #E4E7EC; -fx-border-width: 1; -fx-border-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5); -fx-padding: 15; -fx-min-width: 220;");

        // Profile Info
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView userIcon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        userIcon.setGlyphName("USER_CIRCLE");
        userIcon.setSize("36");
        userIcon.setStyle("-fx-fill: #0D9488;");
        
        VBox texts = new VBox(2);
        Label nameLbl = new Label(currentUser.getUsername());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 14px;");
        Label roleLbl = new Label(currentUser.getRole());
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
                if (current != null) {
                    if (topProfileName != null) topProfileName.setText(current.getUsername());
                    if (topProfileImage != null) {
                        javafx.scene.image.Image img = com.scolaris.util.ProfileImageUtil.getProfileImage(current.getUsername());
                        if (img != null) {
                            topProfileImage.setImage(img);
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
        popup.show(userProfileContainer.getScene().getWindow(), bounds.getMinX() - 40, bounds.getMaxY() + 10);
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
