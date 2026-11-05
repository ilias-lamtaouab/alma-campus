package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.model.CertificateRequest;
import com.scolaris.model.Student;
import com.scolaris.model.SystemNotification;
import com.scolaris.dao.StudentDao;
import com.scolaris.dao.SystemNotificationDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.dao.impl.SystemNotificationDaoImpl;
import com.scolaris.service.CertificateService;
import com.scolaris.service.ImportExportService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;
import com.scolaris.util.ModernDialog;

public class CertificateRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateRequestController.class);


    @FXML private TableView<CertificateRequest> adminRequestTable;
    @FXML private TableColumn<CertificateRequest, String> colStudentCne;
    @FXML private TableColumn<CertificateRequest, String> colStudentName;
    @FXML private TableColumn<CertificateRequest, String> colDocType;
    @FXML private TableColumn<CertificateRequest, String> colRequestDate;
    @FXML private TableColumn<CertificateRequest, String> colStatus;
    @FXML private TableColumn<CertificateRequest, String> colApprovedDate;
    @FXML private TableColumn<CertificateRequest, Void> colActions;

    private CertificateService certificateService;
    private StudentDao studentDao;
    private SystemNotificationDao notificationDao;
    private ImportExportService importExportService;

    public void initialize() {
        certificateService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.CertificateService.class);
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        notificationDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SystemNotificationDao.class);
        importExportService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ImportExportService.class);

        setupTableColumns();
        loadRequests();
    }

    private void setupTableColumns() {
        colStudentCne.setCellValueFactory(new PropertyValueFactory<>("studentCne"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colDocType.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        
        colRequestDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getRequestDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colApprovedDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getApprovedDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<CertificateRequest, String>() {
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

        // Set action buttons cell factory
        colActions.setCellFactory(param -> new TableCell<CertificateRequest, Void>() {
            private final Button btnApprove = new Button("Accepter");
            private final Button btnReject = new Button("Refuser");
            private final HBox container = new HBox(8, btnApprove, btnReject);

            {
                btnApprove.getStyleClass().setAll("btn-success", "btn-table-action");
                btnReject.getStyleClass().setAll("btn-danger", "btn-table-action");
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnApprove.setOnAction(e -> {
                    CertificateRequest req = getTableView().getItems().get(getIndex());
                    handleApprove(req);
                });

                btnReject.setOnAction(e -> {
                    CertificateRequest req = getTableView().getItems().get(getIndex());
                    handleReject(req);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CertificateRequest req = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(req.getStatus())) {
                        setGraphic(container);
                    } else {
                        // Display download button if approved
                        if ("APPROVED".equals(req.getStatus())) {
                            Button btnDownload = new Button("Imprimer PDF");
                            btnDownload.getStyleClass().setAll("btn-primary", "btn-table-action");
                            btnDownload.setOnAction(e -> handleDownload(req));
                            HBox dlContainer = new HBox(btnDownload);
                            dlContainer.setAlignment(javafx.geometry.Pos.CENTER);
                            setGraphic(dlContainer);
                        } else {
                            setGraphic(new Label("Refusé"));
                        }
                    }
                }
            }
        });
    }

    private void loadRequests() {
        List<CertificateRequest> requests = certificateService.getAllRequests();
        adminRequestTable.setItems(FXCollections.observableArrayList(requests));
    }

    private void handleApprove(CertificateRequest req) {
        boolean success = certificateService.approveRequest(req.getId());
        if (success) {
            SystemNotification notif = new SystemNotification();
            notif.setSender("ADMIN");
            notif.setTargetType("SPECIFIC_STUDENT");
            notif.setTargetUsername(req.getStudentCne());
            notif.setTitle("Demande de document approuvée");
            notif.setContent("Votre demande de document a été approuvée. Vous pouvez maintenant le télécharger.");
            notif.setCreatedAt(LocalDateTime.now());
            notificationDao.save(notif);

            loadRequests();
            com.scolaris.util.ModernDialog.showMessage("Information", "Demande acceptée avec succès.", com.scolaris.util.ModernDialog.Type.INFO);
        } else {
            com.scolaris.util.ModernDialog.showMessage("Erreur", "Échec lors de l'approbation.", com.scolaris.util.ModernDialog.Type.ERROR);
        }
    }

    private void handleReject(CertificateRequest req) {
        boolean success = certificateService.rejectRequest(req.getId());
        if (success) {
            SystemNotification notif = new SystemNotification();
            notif.setSender("ADMIN");
            notif.setTargetType("SPECIFIC_STUDENT");
            notif.setTargetUsername(req.getStudentCne());
            notif.setTitle("Demande de document refusée");
            notif.setContent("Votre demande de document a été refusée. Veuillez contacter l'administration pour plus d'informations.");
            notif.setCreatedAt(LocalDateTime.now());
            notificationDao.save(notif);

            loadRequests();
            com.scolaris.util.ModernDialog.showMessage("Information", "Demande refusée.", com.scolaris.util.ModernDialog.Type.INFO);
        } else {
            com.scolaris.util.ModernDialog.showMessage("Erreur", "Échec de l'opération.", com.scolaris.util.ModernDialog.Type.ERROR);
        }
    }

    private void handleDownload(CertificateRequest req) {
        Optional<Student> optStudent = studentDao.findById(req.getStudentId());
        if (optStudent.isEmpty()) {
            com.scolaris.util.ModernDialog.showMessage("Erreur", "Étudiant introuvable.", com.scolaris.util.ModernDialog.Type.ERROR);
            return;
        }
        Student student = optStudent.get();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Certificat");
        fileChooser.setInitialFileName("Certificat_Scolarite_" + student.getLastName() + "_" + student.getFirstName() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));

        Stage stage = (Stage) adminRequestTable.getScene().getWindow();
        File destFile = fileChooser.showSaveDialog(stage);

        if (destFile != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    importExportService.exportCertificateOfEnrollment(student, destFile);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                ModernDialog.showMessage("Succès", "Le certificat PDF a été généré avec succès !", ModernDialog.Type.SUCCESS);
            });
            
            task.setOnFailed(e -> {
                logger.error("Error occurred in CertificateRequestController", task.getException());
                ModernDialog.showMessage("Erreur", "Erreur lors de la génération du certificat.", ModernDialog.Type.ERROR);
            });
            
            new Thread(task).start();
        }
    }
}
