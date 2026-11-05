package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.StudentDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.model.CertificateRequest;
import com.scolaris.model.Student;
import com.scolaris.model.User;
import com.scolaris.service.AuthenticationService;
import com.scolaris.service.CertificateService;
import com.scolaris.service.ImportExportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;
import com.scolaris.util.ModernDialog;

public class StudentCertificatesController {

    private static final Logger logger = LoggerFactory.getLogger(StudentCertificatesController.class);


    @FXML private HBox notificationBanner;
    @FXML private TableView<CertificateRequest> requestTable;
    @FXML private TableColumn<CertificateRequest, String> colReqDate;
    @FXML private TableColumn<CertificateRequest, String> colDocType;
    @FXML private TableColumn<CertificateRequest, String> colReqStatus;
    @FXML private TableColumn<CertificateRequest, String> colReqAppDate;
    @FXML private TableColumn<CertificateRequest, Void> colReqAction;

    private StudentDao studentDao;
    private CertificateService certificateService;
    private ImportExportService importExportService;
    
    private Student currentStudent;

    public void initialize() {
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        certificateService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.CertificateService.class);
        importExportService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ImportExportService.class);

        setupColumns();
        loadStudentData();
    }

    private void setupColumns() {
        colReqDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getRequestDate();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colReqAppDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getApprovedDate();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colDocType.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colReqStatus.setCellFactory(column -> new TableCell<CertificateRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("APPROVED".equals(item)) {
                        badge.getStyleClass().add("badge-received");
                    } else if ("REJECTED".equals(item)) {
                        badge.getStyleClass().add("badge-due");
                    } else {
                        badge.getStyleClass().add("badge-request");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colReqAction.setCellFactory(param -> new TableCell<CertificateRequest, Void>() {
            private final Button btnDownload = new Button("Télécharger");
            {
                btnDownload.getStyleClass().setAll("btn-primary", "btn-table-action");
                btnDownload.setOnAction(e -> {
                    CertificateRequest req = getTableView().getItems().get(getIndex());
                    handleDownloadCertificate(req);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CertificateRequest req = getTableView().getItems().get(getIndex());
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

        String username = user.getUsername();
        Optional<Student> optStudent = studentDao.findByCne(username);
        
        if (optStudent.isEmpty()) {
            Student newStudent = new Student(0, username, username, "Étudiant", 
                LocalDate.of(2004, 1, 1), username + "@scolaris.ma", "0611223344", LocalDate.now(), "ACTIVE");
            studentDao.save(newStudent);
            optStudent = studentDao.findByCne(username);
        }

        if (optStudent.isPresent()) {
            currentStudent = optStudent.get();
            loadCertificateRequests();
            checkApprovedCertificateNotification();
        }
    }

    private void loadCertificateRequests() {
        if (currentStudent == null) return;
        List<CertificateRequest> requests = certificateService.getRequestsByStudent(currentStudent.getId());
        requestTable.setItems(FXCollections.observableArrayList(requests));
    }

    private void checkApprovedCertificateNotification() {
        if (currentStudent == null) return;
        List<CertificateRequest> requests = certificateService.getRequestsByStudent(currentStudent.getId());
        boolean hasApproved = false;
        for (CertificateRequest req : requests) {
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
                com.scolaris.util.ModernDialog.showMessage("Information", "Votre demande de document a été soumise avec succès !", com.scolaris.util.ModernDialog.Type.INFO);
            } else {
                com.scolaris.util.ModernDialog.showMessage("Attention", "Vous avez déjà une demande en cours pour ce type de document.", com.scolaris.util.ModernDialog.Type.WARNING);
            }
        }
    }

    private void handleDownloadCertificate(CertificateRequest req) {
        if (currentStudent == null) return;
        if (!"APPROVED".equals(req.getStatus())) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Télécharger Certificat de Scolarité");
        fileChooser.setInitialFileName("Certificat_Scolarite_" + currentStudent.getLastName() + "_" + currentStudent.getFirstName() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        
        Stage stage = (Stage) requestTable.getScene().getWindow();
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
                ModernDialog.showMessage("Succès", "Votre document PDF a été téléchargé avec succès !", ModernDialog.Type.SUCCESS);
            });
            
            task.setOnFailed(e -> {
                logger.error("Error occurred in StudentCertificatesController", task.getException());
                ModernDialog.showMessage("Erreur", "Erreur lors du téléchargement du document.", ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
    }
}
