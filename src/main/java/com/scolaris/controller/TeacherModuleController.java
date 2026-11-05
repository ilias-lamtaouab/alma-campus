package com.scolaris.controller;

import com.scolaris.model.Module;
import com.scolaris.model.ModuleResource;
import com.scolaris.model.Subject;
import com.scolaris.model.Teacher;
import com.scolaris.model.User;
import com.scolaris.service.AuthenticationService;
import com.scolaris.service.ModuleResourceService;
import com.scolaris.service.ModuleSubjectService;
import com.scolaris.service.TeacherService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.scolaris.util.ModernDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TeacherModuleController {

    @FXML private Label teacherNameLabel;

    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, String> colSubName;
    @FXML private TableColumn<Subject, Double> colSubCoef;
    @FXML private TableColumn<Subject, String> colSubMod;
    @FXML private TableColumn<Subject, String> colSubSem;

    @FXML private TableView<ModuleResource> resourceTable;
    @FXML private TableColumn<ModuleResource, String> colResTitle;
    @FXML private TableColumn<ModuleResource, String> colResDesc;
    @FXML private TableColumn<ModuleResource, String> colResModule;
    @FXML private TableColumn<ModuleResource, String> colResFile;
    @FXML private TableColumn<ModuleResource, String> colResDate;
    @FXML private TableColumn<ModuleResource, Void> colResActions;

    @FXML private VBox resourceForm;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private TextField filePathField;
    @FXML private Label messageLabel;

    private ModuleSubjectService moduleSubjectService;
    private ModuleResourceService resourceService;
    private TeacherService teacherService;

    private Teacher currentTeacher;
    private File selectedFile;

    public void initialize() {
        moduleSubjectService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleSubjectService.class);
        resourceService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleResourceService.class);
        teacherService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.TeacherService.class);

        loadTeacher();
        loadSubjects();
        loadModulesCombo();
        loadResources();

        setupSubjectColumns();
        setupResourceColumns();

        subjectTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadResourcesForModule(newVal.getModuleId());
            }
        });
    }

    private void loadTeacher() {
        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;

        List<Teacher> teachers = teacherService.getAllTeachers();
        for (Teacher t : teachers) {
            if (t.getCin().equals(user.getUsername())) {
                currentTeacher = t;
                break;
            }
        }
        if (currentTeacher != null) {
            teacherNameLabel.setText("Enseignant: " + currentTeacher.getLastName() + " " + currentTeacher.getFirstName());
        }
    }

    private void setupSubjectColumns() {
        colSubMod.setCellValueFactory(cellData -> {
            Module m = cellData.getValue().getModule();
            return new SimpleStringProperty(m == null ? "Aucun" : m.getName());
        });
        colSubSem.setCellValueFactory(cellData -> {
            Module m = cellData.getValue().getModule();
            return new SimpleStringProperty(m == null ? "-" : "S" + m.getSemester());
        });
    }

    private void setupResourceColumns() {
        colResTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colResDesc.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            return new SimpleStringProperty(desc != null && !desc.isEmpty() ? desc : "-");
        });
        colResModule.setCellValueFactory(cellData -> {
            String mn = cellData.getValue().getModuleName();
            return new SimpleStringProperty(mn != null ? mn : "-");
        });
        colResFile.setCellValueFactory(cellData -> {
            String fn = cellData.getValue().getFileName();
            return new SimpleStringProperty(fn != null && !fn.isEmpty() ? fn : "Aucun fichier");
        });
        colResDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("-");
        });

        colResActions.setCellFactory(param -> new TableCell<ModuleResource, Void>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.getStyleClass().setAll("btn-danger", "btn-table-action");
                deleteBtn.setOnAction(e -> {
                    ModuleResource res = getTableView().getItems().get(getIndex());
                    handleDeleteResource(res);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(deleteBtn);
                    container.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    private void loadSubjects() {
        if (currentTeacher == null) return;
        List<Subject> subjects = moduleSubjectService.getSubjectsByTeacher(currentTeacher.getId());
        subjectTable.setItems(FXCollections.observableArrayList(subjects));
    }

    private void loadModulesCombo() {
        if (currentTeacher == null) return;
        List<Subject> subjects = moduleSubjectService.getSubjectsByTeacher(currentTeacher.getId());
        for (Subject s : subjects) {
            if (s.getModule() != null && !moduleComboBox.getItems().contains(s.getModule())) {
                moduleComboBox.getItems().add(s.getModule());
            }
        }
    }

    private void loadResources() {
        resourceTable.setItems(FXCollections.observableArrayList(resourceService.getAllResources()));
    }

    private void loadResourcesForModule(Integer moduleId) {
        if (moduleId == null) {
            loadResources();
            return;
        }
        resourceTable.setItems(FXCollections.observableArrayList(resourceService.getResourcesByModule(moduleId)));
    }

    @FXML
    private void handleNewResource(ActionEvent event) {
        resourceForm.setVisible(true);
        titleField.clear();
        descField.clear();
        filePathField.clear();
        selectedFile = null;
        messageLabel.setVisible(false);
        moduleComboBox.setValue(null);
    }

    @FXML
    private void handleCancelResource(ActionEvent event) {
        resourceForm.setVisible(false);
    }

    @FXML
    private void handleBrowseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
            new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            filePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSaveResource(ActionEvent event) {
        Module module = moduleComboBox.getValue();
        String title = titleField.getText();
        String desc = descField.getText();

        if (module == null || title == null || title.trim().isEmpty()) {
            showMessage("Veuillez sélectionner un module et saisir un titre.", true);
            return;
        }

        String uploadedBy = currentTeacher != null ? currentTeacher.getCin() : "TEACHER";
        boolean success;

        if (selectedFile != null) {
            success = resourceService.addResource(module.getId(), title.trim(), desc, selectedFile, uploadedBy);
        } else {
            success = resourceService.addResourceWithoutFile(module.getId(), title.trim(), desc, uploadedBy);
        }

        if (success) {
            showMessage("Ressource ajoutée avec succès.", false);
            resourceForm.setVisible(false);
            loadResources();
        } else {
            showMessage("Erreur lors de l'ajout de la ressource.", true);
        }
    }

    private void handleDeleteResource(ModuleResource resource) {
        ModernDialog.showConfirm("Confirmation", "Supprimer cette ressource ?", confirmed -> {
            if (confirmed) {
                if (resourceService.deleteResource(resource.getId())) {
                    loadResources();
                    showMessage("Ressource supprimée.", false);
                } else {
                    showMessage("Erreur lors de la suppression.", true);
                }
            }
        });
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }
}