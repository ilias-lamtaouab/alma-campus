package com.scolaris.controller;

import com.scolaris.dao.*;
import com.scolaris.dao.impl.*;
import com.scolaris.model.*;
import com.scolaris.model.Module;
import com.scolaris.service.*;
import com.scolaris.controller.StudentPersonalController.StudentReportRow;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentGradesController {

    @FXML private Label generalAverageLabel;
    @FXML private Label decisionLabel;

    @FXML private TableView<StudentReportRow> detailsTable;
    @FXML private TableColumn<StudentReportRow, String> colModCode;
    @FXML private TableColumn<StudentReportRow, String> colModName;
    @FXML private TableColumn<StudentReportRow, String> colSubName;
    @FXML private TableColumn<StudentReportRow, Double> colSubCoef;
    @FXML private TableColumn<StudentReportRow, String> colCC;
    @FXML private TableColumn<StudentReportRow, String> colProject;
    @FXML private TableColumn<StudentReportRow, String> colExam;
    @FXML private TableColumn<StudentReportRow, Double> colSubAvg;
    @FXML private TableColumn<StudentReportRow, Double> colModAvg;
    @FXML private TableColumn<StudentReportRow, String> colValidation;

    private StudentDao studentDao;
    private GradeDao gradeDao;
    private SubjectDao subjectDao;
    private ModuleDao moduleDao;
    private GradeService gradeService;
    private ImportExportService importExportService;
    
    private Student currentStudent;

    public void initialize() {
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        gradeDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.GradeDao.class);
        subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        gradeService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.GradeService.class);
        importExportService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ImportExportService.class);

        setupColumns();
        loadStudentData();
    }

    private void setupColumns() {
        colModCode.setCellValueFactory(new PropertyValueFactory<>("moduleCode"));
        colModName.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        colSubName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colSubCoef.setCellValueFactory(new PropertyValueFactory<>("subjectCoef"));
        colCC.setCellValueFactory(new PropertyValueFactory<>("ccGrade"));
        colProject.setCellValueFactory(new PropertyValueFactory<>("projectGrade"));
        colExam.setCellValueFactory(new PropertyValueFactory<>("examGrade"));
        colSubAvg.setCellValueFactory(new PropertyValueFactory<>("subjectAvg"));
        colModAvg.setCellValueFactory(new PropertyValueFactory<>("moduleAvg"));
        colValidation.setCellValueFactory(new PropertyValueFactory<>("validation"));

        // Format validation badge column
        colValidation.setCellFactory(column -> new TableCell<StudentReportRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.equals("Validé") || item.equals("V")) {
                        badge.getStyleClass().add("badge-received");
                    } else if (item.equals("Rattrapage") || item.equals("NV")) {
                        badge.getStyleClass().add("badge-due");
                    } else {
                        badge.getStyleClass().add("badge-request");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void loadStudentData() {
        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;

        String username = user.getUsername();
        Optional<Student> optStudent = studentDao.findByCne(username);
        
        // Safeguard auto-creation (already handled by Home, but good for redundancy)
        if (optStudent.isEmpty()) {
            Student newStudent = new Student(0, username, username, "Étudiant", 
                LocalDate.of(2004, 1, 1), username + "@scolaris.ma", "0611223344", LocalDate.now(), "ACTIVE");
            studentDao.save(newStudent);
            optStudent = studentDao.findByCne(username);
        }

        if (optStudent.isPresent()) {
            currentStudent = optStudent.get();
            
            // Calculate overall average and decision
            double genAvg = gradeService.calculateGeneralAverage(currentStudent.getId());
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

            // Populate table rows
            List<StudentReportRow> rows = new ArrayList<>();
            List<Module> modules = moduleDao.findAll();
            
            for (Module mod : modules) {
                List<Subject> subjects = subjectDao.findByModuleId(mod.getId());
                if (subjects.isEmpty()) continue;

                double modAvg = gradeService.calculateModuleAverage(currentStudent.getId(), mod.getId());
                String modVal = modAvg >= 10.0 ? "Validé" : "Rattrapage";

                // Add row for each subject in this module
                for (int i = 0; i < subjects.size(); i++) {
                    Subject sub = subjects.get(i);
                    double subAvg = gradeService.calculateSubjectAverage(currentStudent.getId(), sub.getId());

                    // Get grades details
                    List<Grade> studentGrades = gradeDao.findByStudentId(currentStudent.getId());
                    String ccVal = "-";
                    String projVal = "-";
                    String examVal = "-";
                    for (Grade g : studentGrades) {
                        if (g.getSubjectId() == sub.getId()) {
                            switch (g.getEvaluationType()) {
                                case "CC": ccVal = String.valueOf(g.getGradeValue()); break;
                                case "PROJECT": projVal = String.valueOf(g.getGradeValue()); break;
                                case "EXAM": examVal = String.valueOf(g.getGradeValue()); break;
                            }
                        }
                    }

                    // For subjects, show subject validation V/NV
                    String subVal = subAvg >= 10.0 ? "V" : "NV";

                    StudentReportRow row;
                    if (i == 0) {
                        row = new StudentReportRow(mod.getCode(), mod.getName(), sub.getName(), sub.getCoefficient(), ccVal, projVal, examVal, subAvg, modAvg, modVal);
                    } else {
                        row = new StudentReportRow("", "", sub.getName(), sub.getCoefficient(), ccVal, projVal, examVal, subAvg, modAvg, subVal);
                    }
                    rows.add(row);
                }
            }
            detailsTable.setItems(FXCollections.observableArrayList(rows));
        }
    }

    @FXML
    private void handleRequestDocument(ActionEvent event) {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().showCertificatesView();
        }
    }
}
