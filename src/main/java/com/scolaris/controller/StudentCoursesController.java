package com.scolaris.controller;

import com.scolaris.dao.*;
import com.scolaris.dao.impl.*;
import com.scolaris.model.Module;
import com.scolaris.model.ModuleResource;
import com.scolaris.model.Subject;
import com.scolaris.model.Teacher;
import com.scolaris.service.ModuleResourceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentCoursesController {

    @FXML private TableView<StudentCourseRow> coursesTable;
    @FXML private TableColumn<StudentCourseRow, String> colModCode;
    @FXML private TableColumn<StudentCourseRow, String> colModName;
    @FXML private TableColumn<StudentCourseRow, String> colSubName;
    @FXML private TableColumn<StudentCourseRow, Double> colCoef;
    @FXML private TableColumn<StudentCourseRow, String> colTeacherName;

    private ModuleDao moduleDao;
    private SubjectDao subjectDao;
    private TeacherDao teacherDao;
    private ModuleResourceService resourceService;

    public void initialize() {
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        teacherDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.TeacherDao.class);
        resourceService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.ModuleResourceService.class);

        setupColumns();
        loadCourses();
    }

    private void setupColumns() {
        colModCode.setCellValueFactory(new PropertyValueFactory<>("moduleCode"));
        colModName.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        colSubName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colCoef.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        colTeacherName.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
    }

    private void loadCourses() {
        List<StudentCourseRow> rows = new ArrayList<>();
        List<Module> modules = moduleDao.findAll();

        for (Module mod : modules) {
            List<Subject> subjects = subjectDao.findByModuleId(mod.getId());
            for (Subject sub : subjects) {
                String teacherName = "Non assigne";
                if (sub.getTeacherId() > 0) {
                    Optional<Teacher> optTeacher = teacherDao.findById(sub.getTeacherId());
                    if (optTeacher.isPresent()) {
                        Teacher t = optTeacher.get();
                        teacherName = t.getLastName().toUpperCase() + " " + t.getFirstName();
                    }
                }
                rows.add(new StudentCourseRow(
                    mod.getCode(), mod.getName(), sub.getName(),
                    sub.getCoefficient(), teacherName
                ));
            }
        }
        coursesTable.setItems(FXCollections.observableArrayList(rows));
    }

    public static class StudentCourseRow {
        private final String moduleCode;
        private final String moduleName;
        private final String subjectName;
        private final double coefficient;
        private final String teacherName;

        public StudentCourseRow(String moduleCode, String moduleName, String subjectName, double coefficient, String teacherName) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.subjectName = subjectName;
            this.coefficient = coefficient;
            this.teacherName = teacherName;
        }

        public String getModuleCode() { return moduleCode; }
        public String getModuleName() { return moduleName; }
        public String getSubjectName() { return subjectName; }
        public double getCoefficient() { return coefficient; }
        public String getTeacherName() { return teacherName; }
    }
}
