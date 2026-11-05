package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.MainApp;
import com.scolaris.service.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import com.scolaris.util.ModernDialog;

import java.io.IOException;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML private javafx.scene.layout.VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private javafx.scene.control.Button homeBtn;
    @FXML private javafx.scene.control.Button studentsBtn;
    @FXML private javafx.scene.control.Button teachersBtn;
    @FXML private javafx.scene.control.Button modulesBtn;
    @FXML private javafx.scene.control.Button gradesBtn;
    @FXML private javafx.scene.control.Button certRequestsBtn;
    @FXML private javafx.scene.control.Button resourcesBtn;
    @FXML private javafx.scene.control.Button usersBtn;

    public void initialize() {
        instance = this;
        
        // Sidebar Hover Animation
        if (sidebar != null) {
            sidebar.setOnMouseEntered(e -> {
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 250)
                    )
                );
                timeline.play();
                for (Node node : sidebar.getChildren()) {
                    if (node instanceof javafx.scene.control.Button) {
                        ((javafx.scene.control.Button) node).setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                    }
                }
            });

            sidebar.setOnMouseExited(e -> {
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 75)
                    )
                );
                timeline.play();
                for (Node node : sidebar.getChildren()) {
                    if (node instanceof javafx.scene.control.Button) {
                        ((javafx.scene.control.Button) node).setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            
            // Explicitly force alignment, width and padding via Java to override any CSS specificity issues
            javafx.scene.control.Button[] allSidebarBtns = {homeBtn, studentsBtn, teachersBtn, modulesBtn, gradesBtn, certRequestsBtn, resourcesBtn, usersBtn};
            for (javafx.scene.control.Button btn : allSidebarBtns) {
                if (btn != null) {
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    btn.setStyle("-fx-alignment: center-left; -fx-padding: 12 18 12 25;");
                }
            }
        }

        com.scolaris.model.User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null) {
            String role = currentUser.getRole();
            if ("STUDENT".equals(role)) {
                studentsBtn.setVisible(false);
                studentsBtn.setManaged(false);
                teachersBtn.setVisible(false);
                teachersBtn.setManaged(false);
                usersBtn.setVisible(false);
                usersBtn.setManaged(false);
                
                if (homeBtn != null) homeBtn.setText("Accueil");
                if (modulesBtn != null) modulesBtn.setText("Mes Enseignements");
                if (gradesBtn != null) gradesBtn.setText("Mes Notes & Bulletins");
                if (certRequestsBtn != null) certRequestsBtn.setText("Demandes de Certificats");
                if (resourcesBtn != null) resourcesBtn.setText("Mes Cours");
                
                loadView("student_home_view");
                updateActiveButton(homeBtn);
            } else if ("TEACHER".equals(role)) {
                studentsBtn.setVisible(false);
                studentsBtn.setManaged(false);
                teachersBtn.setVisible(false);
                teachersBtn.setManaged(false);
                certRequestsBtn.setVisible(false);
                certRequestsBtn.setManaged(false);
                usersBtn.setVisible(false);
                usersBtn.setManaged(false);
                resourcesBtn.setVisible(false);
                resourcesBtn.setManaged(false);
                
                if (homeBtn != null) homeBtn.setText("Accueil");
                if (modulesBtn != null) modulesBtn.setText("Mes Matières & Ressources");
                
                loadView("teacher_home_view");
                updateActiveButton(homeBtn);
            } else {
                resourcesBtn.setVisible(false);
                resourcesBtn.setManaged(false);
                loadView("dashboard_home");
                updateActiveButton(homeBtn);
            }
        } else {
            loadView("dashboard_home");
            updateActiveButton(homeBtn);
        }
    }

    public void showCertificatesView() {
        loadView("certificate_request_view");
        updateActiveButton(certRequestsBtn);
    }

    @FXML
    private void handleLoadResources(ActionEvent event) {
        loadView("student_resources_view");
        updateActiveButton(resourcesBtn);
    }

    public void updateActiveButton(javafx.scene.control.Button activeBtn) {
        javafx.scene.control.Button[] buttons = {homeBtn, studentsBtn, teachersBtn, modulesBtn, gradesBtn, certRequestsBtn, resourcesBtn, usersBtn};
        for (javafx.scene.control.Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-btn-active");
            }
        }
        if (activeBtn != null) {
            if (!activeBtn.getStyleClass().contains("sidebar-btn-active")) {
                activeBtn.getStyleClass().add("sidebar-btn-active");
            }
        }
    }

    @FXML
    private void handleLoadHome(ActionEvent event) {
        com.scolaris.model.User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser == null) {
            loadView("dashboard_home");
        } else if ("STUDENT".equals(currentUser.getRole())) {
            loadView("student_home_view");
        } else if ("TEACHER".equals(currentUser.getRole())) {
            loadView("teacher_home_view");
        } else {
            loadView("dashboard_home");
        }
        updateActiveButton(homeBtn);
    }

    @FXML
    private void handleLoadStudents(ActionEvent event) {
        loadView("student_view");
        updateActiveButton(studentsBtn);
    }

    @FXML
    private void handleLoadTeachers(ActionEvent event) {
        loadView("teacher_view");
        updateActiveButton(teachersBtn);
    }

    @FXML
    private void handleLoadModules(ActionEvent event) {
        com.scolaris.model.User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
            loadView("student_courses_view");
        } else if (currentUser != null && "TEACHER".equals(currentUser.getRole())) {
            loadView("teacher_module_view");
        } else {
            loadView("module_subject_view");
        }
        updateActiveButton(modulesBtn);
    }

    @FXML
    private void handleLoadGrades(ActionEvent event) {
        com.scolaris.model.User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
            loadView("student_grades_view");
        } else {
            loadView("grade_view");
        }
        updateActiveButton(gradesBtn);
    }

    @FXML
    private void handleLoadCertRequests(ActionEvent event) {
        com.scolaris.model.User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
            loadView("student_certificates_view");
        } else {
            loadView("certificate_request_view");
        }
        updateActiveButton(certRequestsBtn);
    }

    @FXML
    private void handleLoadUsers(ActionEvent event) {
        loadView("user_management_view");
        updateActiveButton(usersBtn);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        ModernDialog.showConfirm("Confirmation de Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?", confirmed -> {
            if (confirmed) {
                AuthenticationService.logout();
                try {
                    MainApp.setRoot("login");
                } catch (IOException e) {
                    logger.error("Error occurred in DashboardController", e);
                }
            }
        });
    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml + ".fxml"));
            Node view = loader.load();
            if (view instanceof javafx.scene.Parent) {
                com.scolaris.util.AnimationUtils.bindAnimationsToAllButtons((javafx.scene.Parent) view);
            }
            
            // Animation values initial setup: Fade + Shift + Scale
            view.setOpacity(0.0);
            view.setTranslateY(15.0);
            view.setScaleX(0.98);
            view.setScaleY(0.98);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            // Transitions
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), view);
            fade.setToValue(1.0);
            
            javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(350), view);
            slide.setToY(0.0);
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(350), view);
            scale.setToX(1.0);
            scale.setToY(1.0);
            
            javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition(fade, slide, scale);
            parallel.play();
        } catch (IOException e) {
            logger.error("Error occurred in DashboardController", e);
            System.err.println("Failed to load view: " + fxml);
        }
    }
}
