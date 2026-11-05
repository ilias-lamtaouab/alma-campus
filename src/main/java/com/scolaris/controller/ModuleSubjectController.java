package com.scolaris.controller;

import com.scolaris.model.Module;
import com.scolaris.model.Subject;
import com.scolaris.model.Teacher;
import com.scolaris.service.ModuleSubjectService;
import com.scolaris.service.TeacherService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class ModuleSubjectController {

    // --- Modules UI ---
    @FXML private TableView<Module> moduleTable;
    @FXML private TableColumn<Module, Integer> colModId;
    @FXML private TableColumn<Module, String> colModCode;
    @FXML private TableColumn<Module, String> colModName;
    @FXML private TableColumn<Module, Integer> colModSem;

    @FXML private TextField modCodeField;
    @FXML private TextField modNameField;
    @FXML private Spinner<Integer> modSemSpinner;
    @FXML private Button saveModButton;

    // --- Subjects UI ---
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, Integer> colSubId;
    @FXML private TableColumn<Subject, String> colSubName;
    @FXML private TableColumn<Subject, Double> colSubCoef;
    @FXML private TableColumn<Subject, String> colSubMod;
    @FXML private TableColumn<Subject, String> colSubTeacher;

    @FXML private TextField subNameField;
    @FXML private TextField subCoefField;
    @FXML private ComboBox<Module> subModComboBox;
    @FXML private ComboBox<Teacher> subTeacherComboBox;
    @FXML private Button saveSubButton;

    @FXML private Label messageLabel;

    private ModuleSubjectService moduleSubjectService;
    private TeacherService teacherService;

    private ObservableList<Module> moduleList;
    private ObservableList<Subject> subjectList;

    private Module selectedModule = null;
    private Subject selectedSubject = null;

    public void initialize() {
        moduleSubjectService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleSubjectService.class);
        teacherService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.TeacherService.class);

        // Initialize Module Spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        modSemSpinner.setValueFactory(valueFactory);

        // Load Tables and Combos
        loadModules();
        loadSubjects();
        loadComboBoxes();

        // Listeners for selection
        moduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateModuleForm(newVal);
            }
        });

        subjectTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateSubjectForm(newVal);
            }
        });

        // Custom Cell Factories for Subject Table
        colSubMod.setCellValueFactory(cellData -> {
            Module m = cellData.getValue().getModule();
            return new SimpleStringProperty(m == null ? "Aucun" : m.getCode() + " - " + m.getName());
        });

        colSubTeacher.setCellValueFactory(cellData -> {
            Teacher t = cellData.getValue().getTeacher();
            return new SimpleStringProperty(t == null ? "Non assigné" : t.getLastName() + " " + t.getFirstName());
        });

        // Set converters for Combos
        subModComboBox.setConverter(new StringConverter<Module>() {
            @Override
            public String toString(Module module) {
                return module == null ? "" : module.getCode() + " - " + module.getName();
            }
            @Override
            public Module fromString(String string) {
                return null;
            }
        });

        subTeacherComboBox.setConverter(new StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" : teacher.getLastName() + " " + teacher.getFirstName();
            }
            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });
    }

    private void loadModules() {
        List<Module> list = moduleSubjectService.getAllModules();
        moduleList = FXCollections.observableArrayList(list);
        moduleTable.setItems(moduleList);
    }

    private void loadSubjects() {
        List<Subject> list = moduleSubjectService.getAllSubjects();
        subjectList = FXCollections.observableArrayList(list);
        subjectTable.setItems(subjectList);
    }

    private void loadComboBoxes() {
        subModComboBox.setItems(FXCollections.observableArrayList(moduleSubjectService.getAllModules()));
        subTeacherComboBox.setItems(FXCollections.observableArrayList(teacherService.getAllTeachers()));
    }

    // --- Module Actions ---

    @FXML
    private void handleSaveModule(ActionEvent event) {
        String code = modCodeField.getText();
        String name = modNameField.getText();
        int sem = modSemSpinner.getValue();

        if (selectedModule == null) {
            boolean success = moduleSubjectService.addModule(code, name, sem);
            if (success) {
                showMessage("Module ajouté avec succès.", false);
                clearModuleForm();
                loadModules();
                loadComboBoxes(); // Refresh Combos in Subjects tab
            } else {
                showMessage("Erreur : Vérifiez les champs (code unique).", true);
            }
        } else {
            boolean success = moduleSubjectService.updateModule(selectedModule.getId(), code, name, sem);
            if (success) {
                showMessage("Module modifié avec succès.", false);
                clearModuleForm();
                loadModules();
                loadComboBoxes();
            } else {
                showMessage("Erreur lors de la modification.", true);
            }
        }
    }

    @FXML
    private void handleDeleteModule(ActionEvent event) {
        if (selectedModule != null) {
            moduleSubjectService.deleteModule(selectedModule.getId());
            showMessage("Module supprimé.", false);
            clearModuleForm();
            loadModules();
            loadComboBoxes();
        } else {
            showMessage("Sélectionnez un module à supprimer.", true);
        }
    }

    @FXML
    private void clearModuleForm() {
        selectedModule = null;
        modCodeField.clear();
        modNameField.clear();
        modSemSpinner.getValueFactory().setValue(1);
        saveModButton.setText("Ajouter");
        moduleTable.getSelectionModel().clearSelection();
    }

    private void populateModuleForm(Module m) {
        selectedModule = m;
        modCodeField.setText(m.getCode());
        modNameField.setText(m.getName());
        modSemSpinner.getValueFactory().setValue(m.getSemester());
        saveModButton.setText("Modifier");
    }

    // --- Subject Actions ---

    @FXML
    private void handleSaveSubject(ActionEvent event) {
        String name = subNameField.getText();
        double coef;
        try {
            coef = Double.parseDouble(subCoefField.getText());
        } catch (NumberFormatException e) {
            showMessage("Erreur : Le coefficient doit être un nombre.", true);
            return;
        }

        Module mod = subModComboBox.getValue();
        Teacher teach = subTeacherComboBox.getValue();

        Integer modId = mod != null ? mod.getId() : null;
        Integer teachId = teach != null ? teach.getId() : null;

        if (selectedSubject == null) {
            boolean success = moduleSubjectService.addSubject(name, coef, modId, teachId);
            if (success) {
                showMessage("Matière ajoutée avec succès.", false);
                clearSubjectForm();
                loadSubjects();
            } else {
                showMessage("Erreur lors de l'ajout.", true);
            }
        } else {
            boolean success = moduleSubjectService.updateSubject(selectedSubject.getId(), name, coef, modId, teachId);
            if (success) {
                showMessage("Matière modifiée avec succès.", false);
                clearSubjectForm();
                loadSubjects();
            } else {
                showMessage("Erreur lors de la modification.", true);
            }
        }
    }

    @FXML
    private void handleDeleteSubject(ActionEvent event) {
        if (selectedSubject != null) {
            moduleSubjectService.deleteSubject(selectedSubject.getId());
            showMessage("Matière supprimée.", false);
            clearSubjectForm();
            loadSubjects();
        } else {
            showMessage("Sélectionnez une matière à supprimer.", true);
        }
    }

    @FXML
    private void clearSubjectForm() {
        selectedSubject = null;
        subNameField.clear();
        subCoefField.clear();
        subModComboBox.setValue(null);
        subTeacherComboBox.setValue(null);
        saveSubButton.setText("Ajouter");
        subjectTable.getSelectionModel().clearSelection();
    }

    private void populateSubjectForm(Subject s) {
        selectedSubject = s;
        subNameField.setText(s.getName());
        subCoefField.setText(String.valueOf(s.getCoefficient()));
        
        // Find matching combobox items
        if (s.getModuleId() != null) {
            for (Module m : subModComboBox.getItems()) {
                if (m.getId() == s.getModuleId()) {
                    subModComboBox.setValue(m);
                    break;
                }
            }
        } else {
            subModComboBox.setValue(null);
        }

        if (s.getTeacherId() != null) {
            for (Teacher t : subTeacherComboBox.getItems()) {
                if (t.getId() == s.getTeacherId()) {
                    subTeacherComboBox.setValue(t);
                    break;
                }
            }
        } else {
            subTeacherComboBox.setValue(null);
        }

        saveSubButton.setText("Modifier");
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }
}
