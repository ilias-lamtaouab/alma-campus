package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.MainApp;
import com.scolaris.dao.StudentDao;
import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.GradeDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.dao.impl.SubjectDaoImpl;
import com.scolaris.dao.impl.GradeDaoImpl;
import com.scolaris.model.*;
import com.scolaris.service.AuthenticationService;
import com.scolaris.service.GradeService;
import com.scolaris.service.ImportExportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.scolaris.util.ModernDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;

public class StudentPersonalController {

    private static final Logger logger = LoggerFactory.getLogger(StudentPersonalController.class);


    @FXML private Label nameLabel;
    @FXML private Label cneLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
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

    @FXML private javafx.scene.layout.HBox notificationBanner;
    @FXML private TableView<com.scolaris.model.CertificateRequest> requestTable;
    @FXML private TableColumn<com.scolaris.model.CertificateRequest, String> colReqDate;
    @FXML private TableColumn<com.scolaris.model.CertificateRequest, String> colDocType;
    @FXML private TableColumn<com.scolaris.model.CertificateRequest, String> colReqStatus;
    @FXML private TableColumn<com.scolaris.model.CertificateRequest, String> colReqAppDate;
    @FXML private TableColumn<com.scolaris.model.CertificateRequest, Void> colReqAction;
    
    @FXML private javafx.scene.image.ImageView avatarImage;

    private StudentDao studentDao;
    private ModuleDao moduleDao;
    private SubjectDao subjectDao;
    private GradeDao gradeDao;
    private GradeService gradeService;
    private ImportExportService importExportService;
    private com.scolaris.service.CertificateService certificateService;
    private Student currentStudent = null;

    public void initialize() {
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        gradeDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.GradeDao.class);
        gradeService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.GradeService.class);
        importExportService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ImportExportService.class);
        certificateService = new com.scolaris.service.CertificateService();

        if (avatarImage != null) {
            javafx.animation.TranslateTransition floatAnim = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(2500), avatarImage);
            floatAnim.setByY(-10f);
            floatAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
            floatAnim.setAutoReverse(true);
            floatAnim.play();
        }

        setupTableColumns();
        loadStudentData();
    }

    private void setupTableColumns() {
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

        // Format validation column with colors
        colValidation.setCellFactory(column -> new TableCell<StudentReportRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.equals("V") || item.equals("Validé")) {
                        badge.setStyle("-fx-background-color: #55efc4; -fx-text-fill: #00b894; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #ffeaa7; -fx-text-fill: #d63031; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Setup columns for certificate requestTable
        colReqDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getRequestDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colReqAppDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getApprovedDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colDocType.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReqStatus.setCellFactory(column -> new TableCell<com.scolaris.model.CertificateRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("APPROVED".equals(item)) {
                        badge.setStyle("-fx-background-color: #55efc4; -fx-text-fill: #00b894; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else if ("REJECTED".equals(item)) {
                        badge.setStyle("-fx-background-color: #ffeaa7; -fx-text-fill: #d63031; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #636e72; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colReqAction.setCellFactory(param -> new TableCell<com.scolaris.model.CertificateRequest, Void>() {
            private final Button btnDownload = new Button("Télécharger");
            {
                btnDownload.getStyleClass().setAll("btn-primary", "btn-table-action");
                btnDownload.setOnAction(e -> {
                    com.scolaris.model.CertificateRequest req = getTableView().getItems().get(getIndex());
                    handleDownloadCertificate(req);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    com.scolaris.model.CertificateRequest req = getTableView().getItems().get(getIndex());
                    if ("APPROVED".equals(req.getStatus())) {
                        setGraphic(btnDownload);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadStudentData() {
        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;

        // The username of a student is their CNE code
        String username = user.getUsername();
        Optional<Student> optStudent = studentDao.findByCne(username);

        if (optStudent.isPresent()) {
            currentStudent = optStudent.get();
            
            // Set profile data
            nameLabel.setText(currentStudent.getLastName() + " " + currentStudent.getFirstName());
            cneLabel.setText(currentStudent.getCne());
            emailLabel.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "-");
            phoneLabel.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "-");

            // Calculate overall average and decision
            double genAvg = gradeService.calculateGeneralAverage(currentStudent.getId());
            generalAverageLabel.setText(String.format("%.2f / 20", genAvg));

            if (genAvg >= 10.0) {
                decisionLabel.setText("ADMIS");
                decisionLabel.setStyle("-fx-text-fill: white; -fx-background-color: #2ecc71;");
            } else {
                decisionLabel.setText("AJOURNÉ");
                decisionLabel.setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c;");
            }

            // Populate table rows
            List<StudentReportRow> rows = new ArrayList<>();
            List<com.scolaris.model.Module> modules = moduleDao.findAll();
            
            for (com.scolaris.model.Module mod : modules) {
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

                    // Create row
                    // We only display the Module Name and Module Average once per module grouping to keep table extremely clean
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
            loadCertificateRequests();
            checkApprovedCertificateNotification();
        } else {
            nameLabel.setText("Aucun profil étudiant lié à ce compte (" + username + ")");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        ModernDialog.showConfirm("Confirmation de Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?", confirmed -> {
            if (confirmed) {
                AuthenticationService.logout();
                try {
                    MainApp.setRoot("login");
                } catch (IOException e) {
                    logger.error("Error occurred in StudentPersonalController", e);
                }
            }
        });
    }

    @FXML
    private void handleRequestDocument(ActionEvent event) {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().showCertificatesView();
        }
    }

    private void loadCertificateRequests() {
        if (currentStudent == null) return;
        List<com.scolaris.model.CertificateRequest> requests = certificateService.getRequestsByStudent(currentStudent.getId());
        requestTable.setItems(FXCollections.observableArrayList(requests));
    }

    private void checkApprovedCertificateNotification() {
        if (currentStudent == null) return;
        List<com.scolaris.model.CertificateRequest> requests = certificateService.getRequestsByStudent(currentStudent.getId());
        boolean hasApproved = false;
        for (com.scolaris.model.CertificateRequest req : requests) {
            if ("APPROVED".equals(req.getStatus())) {
                hasApproved = true;
                break;
            }
        }
        notificationBanner.setVisible(hasApproved);
        notificationBanner.setManaged(hasApproved);
    }

    @FXML
    private void handleCloseBanner(ActionEvent event) {
        notificationBanner.setVisible(false);
        notificationBanner.setManaged(false);
    }

    @FXML
    private void handleNewRequest(ActionEvent event) {
        if (currentStudent == null) return;
        
        List<String> choices = java.util.Arrays.asList("Attestation de scolarité", "Bulletin de notes", "Emploi du Temps");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Attestation de scolarité", choices);
        dialog.setTitle("Demande de Document");
        dialog.setHeaderText("Sélectionnez le type de document");
        dialog.setContentText("Type:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            String selectedDoc = result.get();
            boolean success = certificateService.requestCertificate(currentStudent.getId(), selectedDoc);
            if (success) {
                loadCertificateRequests();
                ModernDialog.showMessage("Succès", "Votre demande de document a été soumise avec succès !", ModernDialog.Type.SUCCESS);
            } else {
                ModernDialog.showMessage("Attention", "Vous avez déjà une demande en cours pour ce type de document.", ModernDialog.Type.WARNING);
            }
        }
    }

    private void handleDownloadCertificate(com.scolaris.model.CertificateRequest req) {
        if (currentStudent == null) return;
        if (!"APPROVED".equals(req.getStatus())) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Télécharger Certificat de Scolarité");
        fileChooser.setInitialFileName("Certificat_Scolarite_" + currentStudent.getLastName() + "_" + currentStudent.getFirstName() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        File dest = fileChooser.showSaveDialog(stage);

        if (dest != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    importExportService.exportCertificateOfEnrollment(currentStudent, dest);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                ModernDialog.showMessage("Succès", "Votre certificat de scolarité PDF a été exporté avec succès !", ModernDialog.Type.SUCCESS);
            });
            
            task.setOnFailed(e -> {
                logger.error("Error occurred in StudentPersonalController", task.getException());
                ModernDialog.showMessage("Erreur", "Erreur lors de l'exportation du certificat.", ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
    }

    // --- Table Row DTO ---
    public static class StudentReportRow {
        private final String moduleCode;
        private final String moduleName;
        private final String subjectName;
        private final double subjectCoef;
        private final String ccGrade;
        private final String projectGrade;
        private final String examGrade;
        private final double subjectAvg;
        private final double moduleAvg;
        private final String validation;

        public StudentReportRow(String moduleCode, String moduleName, String subjectName, double subjectCoef, String ccGrade, String projectGrade, String examGrade, double subjectAvg, double moduleAvg, String validation) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.subjectName = subjectName;
            this.subjectCoef = subjectCoef;
            this.ccGrade = ccGrade;
            this.projectGrade = projectGrade;
            this.examGrade = examGrade;
            this.subjectAvg = Math.round(subjectAvg * 100.0) / 100.0;
            this.moduleAvg = Math.round(moduleAvg * 100.0) / 100.0;
            this.validation = validation;
        }

        public String getModuleCode() { return moduleCode; }
        public String getModuleName() { return moduleName; }
        public String getSubjectName() { return subjectName; }
        public double getSubjectCoef() { return subjectCoef; }
        public String getCcGrade() { return ccGrade; }
        public String getProjectGrade() { return projectGrade; }
        public String getExamGrade() { return examGrade; }
        public double getSubjectAvg() { return subjectAvg; }
        public double getModuleAvg() { return moduleAvg; }
        public String getValidation() { return validation; }
    }
}
