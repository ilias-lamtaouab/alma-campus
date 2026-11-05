package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.model.Grade;
import com.scolaris.model.Student;
import com.scolaris.model.Subject;
import com.scolaris.model.Module;
import com.scolaris.service.GradeService;
import com.scolaris.service.StudentService;
import com.scolaris.service.ModuleSubjectService;
import com.scolaris.service.ImportExportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javafx.concurrent.Task;
import java.util.List;

public class GradeController {

    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);


    // --- Saisie Notes ---
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private TextField gradeValueField;
    @FXML private ComboBox<String> evalTypeComboBox;
    @FXML private DatePicker evalDatePicker;

    // --- Table ---
    @FXML private TableView<Grade> gradeTable;
    @FXML private TableColumn<Grade, Integer> colGradeId;
    @FXML private TableColumn<Grade, String> colGradeSubject;
    @FXML private TableColumn<Grade, Double> colGradeValue;
    @FXML private TableColumn<Grade, String> colGradeType;
    @FXML private TableColumn<Grade, LocalDate> colGradeDate;
    @FXML private TableColumn<Grade, String> colGradeAction;

    // --- Synthèse ---
    @FXML private Label generalAverageLabel;
    @FXML private Label decisionLabel;
    @FXML private ListView<String> moduleAveragesListView;

    @FXML private Label messageLabel;

    private GradeService gradeService;
    private StudentService studentService;
    private ModuleSubjectService moduleSubjectService;
    private ImportExportService importExportService;

    public void initialize() {
        gradeService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.GradeService.class);
        studentService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.StudentService.class);
        moduleSubjectService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleSubjectService.class);
        importExportService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ImportExportService.class);

        // Populate evaluation types
        evalTypeComboBox.getItems().addAll("CC", "EXAM", "PROJECT");
        evalDatePicker.setValue(LocalDate.now());
        setupDatePicker(evalDatePicker);

        // Configure ComboBox converters
        configureComboBoxes();

        // Load ComboBox Lists
        refreshStudentAndSubjectCombos();

        // Configure Table Columns custom rendering
        colGradeSubject.setCellValueFactory(cellData -> {
            Subject s = cellData.getValue().getSubject();
            return new SimpleStringProperty(s == null ? "" : s.getName());
        });

        // Add action button for delete inside row
        colGradeAction.setCellFactory(param -> new TableCell<Grade, String>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.getStyleClass().setAll("btn-danger", "btn-table-action");
                deleteBtn.setOnAction(event -> {
                    Grade g = getTableView().getItems().get(getIndex());
                    gradeService.deleteGrade(g.getId());
                    showMessage("Note supprimée.", false);
                    refreshStudentData();
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    private void configureComboBoxes() {
        studentComboBox.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student s) {
                return s == null ? "" : s.getLastName() + " " + s.getFirstName() + " (" + s.getCne() + ")";
            }
            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        subjectComboBox.setConverter(new StringConverter<Subject>() {
            @Override
            public String toString(Subject s) {
                return s == null ? "" : s.getName() + " (coef: " + s.getCoefficient() + ")";
            }
            @Override
            public Subject fromString(String string) {
                return null;
            }
        });
    }

    private void refreshStudentAndSubjectCombos() {
        studentComboBox.setItems(FXCollections.observableArrayList(studentService.getAllStudents()));
        
        com.scolaris.model.User currentUser = com.scolaris.service.AuthenticationService.getCurrentUser();
        if (currentUser != null && "TEACHER".equals(currentUser.getRole())) {
            com.scolaris.dao.TeacherDao teacherDao = new com.scolaris.dao.impl.TeacherDaoImpl();
            java.util.Optional<com.scolaris.model.Teacher> teacherOpt = teacherDao.findByCin(currentUser.getUsername());
            if (teacherOpt.isPresent()) {
                List<Subject> teacherSubjects = moduleSubjectService.getSubjectsByTeacher(teacherOpt.get().getId());
                subjectComboBox.setItems(FXCollections.observableArrayList(teacherSubjects));
            } else {
                subjectComboBox.setItems(FXCollections.emptyObservableList());
            }
        } else {
            subjectComboBox.setItems(FXCollections.observableArrayList(moduleSubjectService.getAllSubjects()));
        }
    }

    @FXML
    private void handleStudentSelection(ActionEvent event) {
        refreshStudentData();
    }

    private void refreshStudentData() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent == null) {
            gradeTable.setItems(FXCollections.emptyObservableList());
            generalAverageLabel.setText("0.00 / 20");
            decisionLabel.setText("-");
            decisionLabel.setStyle("");
            moduleAveragesListView.setItems(FXCollections.emptyObservableList());
            return;
        }

        // 1. Refresh Table
        List<Grade> grades = gradeService.getGradesByStudent(selectedStudent.getId());
        gradeTable.setItems(FXCollections.observableArrayList(grades));

        // 2. Refresh General Average & Decision
        double genAvg = gradeService.calculateGeneralAverage(selectedStudent.getId());
        generalAverageLabel.setText(String.format("%.2f / 20", genAvg));

        decisionLabel.getStyleClass().removeAll("badge", "badge-received", "badge-due");
        if (genAvg >= 10.0) {
            decisionLabel.setText("ADMIS");
            decisionLabel.getStyleClass().addAll("badge", "badge-received");
            decisionLabel.setStyle("");
        } else if (genAvg > 0.0) {
            decisionLabel.setText("AJOURNÉ");
            decisionLabel.getStyleClass().addAll("badge", "badge-due");
            decisionLabel.setStyle("");
        } else {
            decisionLabel.setText("-");
            decisionLabel.setStyle("");
        }

        // 3. Refresh Module Averages List
        ObservableList<String> modAvgStrings = FXCollections.observableArrayList();
        List<Module> allModules = moduleSubjectService.getAllModules();
        for (Module mod : allModules) {
            double avg = gradeService.calculateModuleAverage(selectedStudent.getId(), mod.getId());
            modAvgStrings.add(mod.getCode() + " : " + String.format("%.2f / 20", avg) + (avg >= 10.0 ? " (Validé)" : " (Non Validé)"));
        }
        moduleAveragesListView.setItems(modAvgStrings);
    }

    @FXML
    private void handleSaveGrade(ActionEvent event) {
        Student student = studentComboBox.getValue();
        Subject subject = subjectComboBox.getValue();
        String type = evalTypeComboBox.getValue();
        LocalDate date = evalDatePicker.getValue();

        if (student == null || subject == null || type == null || date == null) {
            showMessage("Veuillez remplir tous les champs de saisie.", true);
            return;
        }

        double val;
        try {
            val = Double.parseDouble(gradeValueField.getText());
        } catch (NumberFormatException e) {
            showMessage("Note invalide (doit être numérique).", true);
            return;
        }

        boolean success = gradeService.addOrUpdateGrade(student.getId(), subject.getId(), val, type, date);
        if (success) {
            showMessage("Note enregistrée avec succès.", false);
            clearGradeForm();
            refreshStudentData();
        } else {
            showMessage("Erreur : Valeur hors limites (0-20).", true);
        }
    }

    @FXML
    private void clearGradeForm() {
        gradeValueField.clear();
        evalTypeComboBox.setValue(null);
        evalDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        Student student = studentComboBox.getValue();
        if (student == null) {
            showMessage("Sélectionnez d'abord un étudiant pour son bulletin.", true);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Bulletin PDF");
        fileChooser.setInitialFileName("Bulletin_" + student.getLastName() + "_" + student.getFirstName() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        
        Stage stage = (Stage) studentComboBox.getScene().getWindow();
        File dest = fileChooser.showSaveDialog(stage);

        if (dest != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    importExportService.exportStudentBulletin(student.getId(), dest);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                com.scolaris.util.ModernDialog.showMessage("Information", "Bulletin PDF généré avec succès !", com.scolaris.util.ModernDialog.Type.INFO);
            });
            
            task.setOnFailed(e -> {
                logger.error("Error occurred in GradeController PDF export", task.getException());
                com.scolaris.util.ModernDialog.showMessage("Erreur", "Erreur lors de la génération du PDF.", com.scolaris.util.ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
    }

    @FXML
    private void handleImportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier CSV des étudiants");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        Stage stage = (Stage) studentComboBox.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return importExportService.importStudentsFromCsv(file);
                }
            };
            
            task.setOnSucceeded(e -> {
                com.scolaris.util.ModernDialog.showMessage("Succès", task.getValue() + " étudiants importés avec succès depuis le CSV.", com.scolaris.util.ModernDialog.Type.SUCCESS);
                refreshStudentAndSubjectCombos();
            });
            
            task.setOnFailed(e -> {
                com.scolaris.util.ModernDialog.showMessage("Erreur", "Erreur lors de l'importation.", com.scolaris.util.ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
    }

    @FXML
    private void handleImportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier Excel des étudiants");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));

        Stage stage = (Stage) studentComboBox.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return importExportService.importStudentsFromExcel(file);
                }
            };
            
            task.setOnSucceeded(e -> {
                com.scolaris.util.ModernDialog.showMessage("Succès", task.getValue() + " étudiants importés avec succès depuis Excel.", com.scolaris.util.ModernDialog.Type.SUCCESS);
                refreshStudentAndSubjectCombos();
            });
            
            task.setOnFailed(e -> {
                com.scolaris.util.ModernDialog.showMessage("Erreur", "Erreur lors de l'importation.", com.scolaris.util.ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
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
