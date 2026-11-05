package com.scolaris.controller;

import com.scolaris.model.Teacher;
import com.scolaris.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class TeacherController {

    @FXML private TextField cinField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField specialtyField;
    @FXML private Label messageLabel;
    @FXML private Button saveButton;

    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, Integer> colId;
    @FXML private TableColumn<Teacher, String> colCin;
    @FXML private TableColumn<Teacher, String> colFirstName;
    @FXML private TableColumn<Teacher, String> colLastName;
    @FXML private TableColumn<Teacher, String> colEmail;
    @FXML private TableColumn<Teacher, String> colPhone;
    @FXML private TableColumn<Teacher, String> colSpecialty;

    private TeacherService teacherService;
    private ObservableList<Teacher> teacherList;
    private Teacher selectedTeacher = null;

    public void initialize() {
        teacherService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.TeacherService.class);
        loadTeachers();

        teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void loadTeachers() {
        teacherList = FXCollections.observableArrayList(teacherService.getAllTeachers());
        teacherTable.setItems(teacherList);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (selectedTeacher == null) {
            // Add new
            boolean success = teacherService.addTeacher(
                cinField.getText(), firstNameField.getText(), lastNameField.getText(),
                emailField.getText(), phoneField.getText(), specialtyField.getText()
            );
            if (success) {
                showMessage("Enseignant ajouté avec succès.", false);
                clearForm();
                loadTeachers();
            } else {
                showMessage("Erreur : Vérifiez les champs (CIN unique).", true);
            }
        } else {
            // Update existing
            boolean success = teacherService.updateTeacher(
                selectedTeacher.getId(), cinField.getText(), firstNameField.getText(), lastNameField.getText(),
                emailField.getText(), phoneField.getText(), specialtyField.getText()
            );
            if (success) {
                showMessage("Enseignant mis à jour avec succès.", false);
                clearForm();
                loadTeachers();
            } else {
                showMessage("Erreur lors de la mise à jour.", true);
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedTeacher != null) {
            teacherService.deleteTeacher(selectedTeacher.getId());
            showMessage("Enseignant supprimé.", false);
            clearForm();
            loadTeachers();
        } else {
            showMessage("Sélectionnez un enseignant à supprimer.", true);
        }
    }

    @FXML
    private void clearForm() {
        selectedTeacher = null;
        cinField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        specialtyField.clear();
        saveButton.setText("Ajouter");
        teacherTable.getSelectionModel().clearSelection();
    }

    private void populateForm(Teacher teacher) {
        selectedTeacher = teacher;
        cinField.setText(teacher.getCin());
        firstNameField.setText(teacher.getFirstName());
        lastNameField.setText(teacher.getLastName());
        emailField.setText(teacher.getEmail());
        phoneField.setText(teacher.getPhone());
        specialtyField.setText(teacher.getSpecialty());
        saveButton.setText("Modifier");
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }
}
