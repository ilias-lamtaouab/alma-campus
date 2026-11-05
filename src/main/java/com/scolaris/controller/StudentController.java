package com.scolaris.controller;

import com.scolaris.model.Student;
import com.scolaris.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentController {

    @FXML private TextField cneField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label messageLabel;
    @FXML private Button saveButton;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colCne;
    @FXML private TableColumn<Student, String> colFirstName;
    @FXML private TableColumn<Student, String> colLastName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colDob;
    @FXML private TableColumn<Student, String> colStatus;

    private StudentService studentService;
    private ObservableList<Student> studentList;
    private Student selectedStudent = null;

    public void initialize() {
        studentService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.StudentService.class);
        statusComboBox.getItems().addAll("ACTIVE", "SUSPENDED", "GRADUATED");
        statusComboBox.setValue("ACTIVE");
        
        setupDatePicker(dobPicker);

        loadStudents();

        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
        
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

    private void loadStudents() {
        studentList = FXCollections.observableArrayList(studentService.getAllStudents());
        studentTable.setItems(studentList);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (selectedStudent == null) {
            // Add new student
            boolean success = studentService.addStudent(
                cneField.getText(), firstNameField.getText(), lastNameField.getText(),
                dobPicker.getValue(), emailField.getText(), phoneField.getText(),
                statusComboBox.getValue()
            );
            if (success) {
                showMessage("Étudiant ajouté avec succès.", false);
                clearForm();
                loadStudents();
            } else {
                showMessage("Erreur : Vérifiez les champs (CNE unique).", true);
            }
        } else {
            // Update existing student
            boolean success = studentService.updateStudent(
                selectedStudent.getId(), cneField.getText(), firstNameField.getText(), lastNameField.getText(),
                dobPicker.getValue(), emailField.getText(), phoneField.getText(),
                statusComboBox.getValue()
            );
            if (success) {
                showMessage("Étudiant mis à jour avec succès.", false);
                clearForm();
                loadStudents();
            } else {
                showMessage("Erreur lors de la mise à jour.", true);
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedStudent != null) {
            studentService.deleteStudent(selectedStudent.getId());
            showMessage("Étudiant supprimé.", false);
            clearForm();
            loadStudents();
        } else {
            showMessage("Sélectionnez un étudiant à supprimer.", true);
        }
    }

    @FXML
    private void clearForm() {
        selectedStudent = null;
        cneField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        dobPicker.setValue(null);
        statusComboBox.setValue("ACTIVE");
        saveButton.setText("Ajouter");
        studentTable.getSelectionModel().clearSelection();
    }

    private void populateForm(Student student) {
        selectedStudent = student;
        cneField.setText(student.getCne());
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getPhone());
        dobPicker.setValue(student.getDateOfBirth());
        statusComboBox.setValue(student.getStatus());
        saveButton.setText("Modifier");
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }

    private void setupDatePicker(DatePicker picker) {
        picker.setPromptText("JJ/MM/AAAA");
        picker.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
            private final java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(java.time.LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public java.time.LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return java.time.LocalDate.parse(string, dateFormatter);
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
    }
}
