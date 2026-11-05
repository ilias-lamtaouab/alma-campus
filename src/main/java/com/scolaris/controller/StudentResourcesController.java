package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.model.Module;
import com.scolaris.model.ModuleResource;
import com.scolaris.service.ModuleResourceService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentResourcesController {

    private static final Logger logger = LoggerFactory.getLogger(StudentResourcesController.class);


    @FXML private Label moduleCountLabel;
    @FXML private VBox modulesContainer;

    private ModuleDao moduleDao;
    private ModuleResourceService resourceService;

    private static final String CARD_NORMAL = "-fx-background-color: white; -fx-padding: 14 16; -fx-background-radius: 10; -fx-border-color: #E4E7EC; -fx-border-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);";
    private static final String CARD_HOVER = "-fx-background-color: #F7F8FA; -fx-padding: 14 16; -fx-background-radius: 10; -fx-border-color: #99F6E4; -fx-border-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);";

    public void initialize() {
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        resourceService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleResourceService.class);
        loadModules();
    }

    private void loadModules() {
        modulesContainer.getChildren().clear();
        List<Module> modules = moduleDao.findAll();

        for (Module mod : modules) {
            modulesContainer.getChildren().add(createModuleCard(mod));
        }

        moduleCountLabel.setText(modules.size() + " cours");
    }

    private VBox createModuleCard(Module mod) {
        VBox card = new VBox(8);
        card.setStyle(CARD_NORMAL);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setStyle("-fx-cursor: hand;");

        StackPane iconWrap = new StackPane();
        iconWrap.setStyle("-fx-background-color: #F0FDFA; -fx-background-radius: 12; -fx-padding: 12;");
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25");
        icon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        icon.setStroke(javafx.scene.paint.Color.web("#0D9488"));
        icon.setStrokeWidth(1.8);
        icon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        icon.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        iconWrap.getChildren().add(icon);

        VBox textBox = new VBox(3);
        Label codeLbl = new Label(mod.getCode() + " - " + mod.getName());
        codeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #101828; -fx-font-size: 14px;");
        textBox.getChildren().add(codeLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label semBadge = new Label("S" + mod.getSemester());
        semBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8;");

        topRow.getChildren().addAll(iconWrap, textBox, spacer, semBadge);
        card.getChildren().add(topRow);

        VBox resourcesBox = new VBox(10);
        resourcesBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: transparent transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;"); // Border will be set visible on expand
        resourcesBox.setVisible(false);
        resourcesBox.setManaged(false);

        topRow.setOnMouseClicked(e -> {
            if (resourcesBox.isVisible()) {
                resourcesBox.setVisible(false);
                resourcesBox.setManaged(false);
                resourcesBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");
                card.getChildren().remove(resourcesBox);
            } else {
                loadResources(mod, resourcesBox);
                if (!card.getChildren().contains(resourcesBox)) {
                    card.getChildren().add(resourcesBox);
                }
                resourcesBox.setVisible(true);
                resourcesBox.setManaged(true);
                resourcesBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #E4E7EC transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");
            }
        });

        card.setOnMouseEntered(e -> card.setStyle(CARD_HOVER));
        card.setOnMouseExited(e -> card.setStyle(CARD_NORMAL));

        return card;
    }

    private void loadResources(Module mod, VBox container) {
        container.getChildren().clear();
        List<ModuleResource> resources = resourceService.getResourcesByModule(mod.getId());
        
        if (resources.isEmpty()) {
            Label emptyLbl = new Label("Aucun cours ou ressource pour le moment.");
            emptyLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-style: italic; -fx-padding: 5;");
            container.getChildren().add(emptyLbl);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (ModuleResource res : resources) {
            HBox resRow = new HBox(10);
            resRow.setAlignment(Pos.CENTER_LEFT);
            resRow.setStyle("-fx-padding: 8; -fx-background-color: #F7F8FA; -fx-background-radius: 6; -fx-cursor: hand;");

            FontAwesomeIconView fileIcon = new FontAwesomeIconView();
            String fn = res.getFileName() != null ? res.getFileName().toLowerCase() : "";
            if (fn.endsWith(".pdf")) {
                fileIcon.setGlyphName("FILE_PDF_ALT");
                fileIcon.setStyle("-fx-fill: #ef4444;");
            } else if (fn.endsWith(".doc") || fn.endsWith(".docx")) {
                fileIcon.setGlyphName("FILE_WORD_ALT");
                fileIcon.setStyle("-fx-fill: #3b82f6;");
            } else if (fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg")) {
                fileIcon.setGlyphName("FILE_IMAGE_ALT");
                fileIcon.setStyle("-fx-fill: #10b981;");
            } else {
                fileIcon.setGlyphName("FILE_ALT");
                fileIcon.setStyle("-fx-fill: #667085;");
            }
            fileIcon.setSize("16");

            VBox textCol = new VBox(2);
            Label titleLbl = new Label(res.getTitle());
            titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
            String dateStr = res.getUploadedAt() != null ? res.getUploadedAt().format(formatter) : "";
            Label dateLbl = new Label(dateStr);
            dateLbl.setStyle("-fx-text-fill: #98A2B3; -fx-font-size: 11px;");
            textCol.getChildren().addAll(titleLbl, dateLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button dlBtn = new Button();
            dlBtn.setStyle("-fx-background-color: #0D9488; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 4 8;");
            javafx.scene.shape.SVGPath dlIcon = new javafx.scene.shape.SVGPath();
            dlIcon.setContent("M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4");
            dlIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dlIcon.setStroke(javafx.scene.paint.Color.WHITE);
            dlIcon.setStrokeWidth(1.8);
            dlIcon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            dlIcon.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            dlBtn.setGraphic(dlIcon);
            dlBtn.setOnAction(e -> {
                e.consume();
                openResourceFile(res.getFilePath());
            });
            
            resRow.setOnMouseClicked(e -> {
                e.consume();
                openResourceFile(res.getFilePath());
            });

            resRow.getChildren().addAll(fileIcon, textCol, spacer, dlBtn);
            container.getChildren().add(resRow);
        }
    }

    private void openResourceFile(String filePath) {
        try {
            File file = new File(filePath);
            
            // If the absolute path doesn't exist (project was moved), try to resolve it dynamically
            if (!file.exists()) {
                String keyword1 = "resources" + File.separator + "modules";
                String keyword2 = "resources/modules";
                String keyword3 = "resources\\modules";
                
                int idx = filePath.indexOf(keyword1);
                if (idx == -1) idx = filePath.indexOf(keyword2);
                if (idx == -1) idx = filePath.indexOf(keyword3);
                
                if (idx != -1) {
                    String relativePath = filePath.substring(idx);
                    file = new File(System.getProperty("user.dir"), relativePath);
                }
            }

            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                com.scolaris.util.ModernDialog.showMessage("Information", "Le fichier n'est pas disponible.", com.scolaris.util.ModernDialog.Type.INFO);
            }
        } catch (Exception ex) {
            logger.error("Error occurred in StudentResourcesController", ex);
            com.scolaris.util.ModernDialog.showMessage("Erreur", "Impossible d'ouvrir le fichier.", com.scolaris.util.ModernDialog.Type.ERROR);
        }
    }
}
