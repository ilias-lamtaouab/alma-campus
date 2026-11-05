package com.scolaris.controller;

import com.scolaris.dao.UserDao;
import com.scolaris.dao.impl.UserDaoImpl;
import com.scolaris.model.User;
import com.scolaris.service.AuthenticationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.scolaris.util.ModernDialog;

import java.util.List;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colAction;

    @FXML private TextField searchField;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    
    // Broadcast notification controls
    @FXML private ComboBox<String> notifTargetComboBox;
    @FXML private VBox notifUsernameContainer;
    @FXML private TextField notifUsernameField;
    @FXML private TextField notifTitleField;
    @FXML private TextArea notifContentArea;

    private UserDao userDao;
    private AuthenticationService authService;

    public void initialize() {
        userDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.UserDao.class);
        authService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.AuthenticationService.class);

        roleComboBox.getItems().addAll("ADMIN", "TEACHER", "STUDENT");

        // Initialize notification targets
        notifTargetComboBox.getItems().addAll("TOUS", "TOUS_ETUDIANTS", "ETUDIANT_SPECIFIQUE", "TOUS_ENSEIGNANTS", "ENSEIGNANT_SPECIFIQUE");
        notifTargetComboBox.setValue("TOUS");

        loadUsers();
        setupColumns();
        setupSearch();
    }

    private ObservableList<User> masterData = FXCollections.observableArrayList();

    private void loadUsers() {
        List<User> list = userDao.findAll();
        masterData.setAll(list);
        userTable.setItems(masterData);
    }

    private void setupSearch() {
        if (searchField == null) return;
        
        javafx.collections.transformation.FilteredList<User> filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getRole() != null && user.getRole().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(user.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        javafx.collections.transformation.SortedList<User> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void setupColumns() {
        // Setup status badge
        colStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("ACTIVE".equals(item)) {
                        badge.getStyleClass().setAll("badge-received");
                    } else {
                        badge.getStyleClass().setAll("badge-due");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox actionsContainer = new HBox(8, toggleBtn, deleteBtn);

            {
                actionsContainer.setAlignment(javafx.geometry.Pos.CENTER);
                toggleBtn.setOnAction(event -> {
                    User u = getTableView().getItems().get(getIndex());
                    if ("admin".equals(u.getUsername())) {
                        ModernDialog.showMessage("Attention", "Impossible de modifier le compte de l'administrateur principal.", ModernDialog.Type.WARNING);
                        return;
                    }
                    // Toggle status
                    u.setStatus("ACTIVE".equals(u.getStatus()) ? "BLOCKED" : "ACTIVE");
                    userDao.update(u);
                    loadUsers();
                });

                deleteBtn.setOnAction(event -> {
                    User u = getTableView().getItems().get(getIndex());
                    if ("admin".equals(u.getUsername())) {
                        ModernDialog.showMessage("Attention", "Impossible de supprimer l'administrateur principal.", ModernDialog.Type.WARNING);
                        return;
                    }
                    
                    ModernDialog.showConfirm("Confirmation de Suppression", "Êtes-vous sûr de vouloir supprimer cet utilisateur ?", confirmed -> {
                        if (confirmed) {
                            userDao.delete(u.getId());
                            loadUsers();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User u = getTableView().getItems().get(getIndex());
                    if ("ACTIVE".equals(u.getStatus())) {
                        toggleBtn.setText("Bloquer");
                        toggleBtn.getStyleClass().setAll("btn-warning", "btn-table-action");
                    } else {
                        toggleBtn.setText("Débloquer");
                        toggleBtn.getStyleClass().setAll("btn-success", "btn-table-action");
                    }
                    deleteBtn.getStyleClass().setAll("btn-danger", "btn-table-action");
                    
                    setGraphic(actionsContainer);
                }
            }
        });
    }

    @FXML
    private void handleCreateUser(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showMessage("Veuillez remplir tous les champs.", true);
            return;
        }

        boolean success = authService.register(username, password, role);
        if (success) {
            showMessage("Compte créé avec succès !", false);
            clearForm();
            loadUsers();
        } else {
            showMessage("Erreur : Ce nom d'utilisateur existe déjà.", true);
        }
    }

    @FXML
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }

    @FXML
    private void handleTargetChange(ActionEvent event) {
        String target = notifTargetComboBox.getValue();
        boolean needsUser = "ETUDIANT_SPECIFIQUE".equals(target) || "ENSEIGNANT_SPECIFIQUE".equals(target);
        notifUsernameContainer.setVisible(needsUser);
        notifUsernameContainer.setManaged(needsUser);
    }

    @FXML
    private void clearNotifForm(ActionEvent event) {
        notifTargetComboBox.setValue("TOUS");
        notifUsernameContainer.setVisible(false);
        notifUsernameContainer.setManaged(false);
        notifUsernameField.clear();
        notifTitleField.clear();
        notifContentArea.clear();
    }

    @FXML
    private void handleSendNotification(ActionEvent event) {
        String target = notifTargetComboBox.getValue();
        String targetUser = notifUsernameField.getText().trim();
        String title = notifTitleField.getText().trim();
        String content = notifContentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showMessage("Veuillez remplir le titre et le contenu de l'annonce.", true);
            return;
        }

        boolean needsUser = "ETUDIANT_SPECIFIQUE".equals(target) || "ENSEIGNANT_SPECIFIQUE".equals(target);
        if (needsUser && targetUser.isEmpty()) {
            showMessage("Veuillez saisir l'identifiant cible (CNE / CIN).", true);
            return;
        }

        // Map UI selection to Database values
        String dbTargetType = "ALL";
        if ("TOUS_ETUDIANTS".equals(target)) dbTargetType = "ALL_STUDENTS";
        else if ("ETUDIANT_SPECIFIQUE".equals(target)) dbTargetType = "SPECIFIC_STUDENT";
        else if ("TOUS_ENSEIGNANTS".equals(target)) dbTargetType = "ALL_TEACHERS";
        else if ("ENSEIGNANT_SPECIFIQUE".equals(target)) dbTargetType = "SPECIFIC_TEACHER";

        com.scolaris.model.SystemNotification notif = new com.scolaris.model.SystemNotification(
            0, "ADMIN", dbTargetType, targetUser.isEmpty() ? null : targetUser, title, content, java.time.LocalDateTime.now()
        );

        com.scolaris.dao.SystemNotificationDao notifDao = new com.scolaris.dao.impl.SystemNotificationDaoImpl();
        com.scolaris.model.SystemNotification saved = notifDao.save(notif);

        if (saved != null) {
            showMessage("Annonce diffusée avec succès !", false);
            clearNotifForm(null);
        } else {
            showMessage("Erreur lors de la diffusion de l'annonce.", true);
        }
    }
}
