package com.scolaris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.MainApp;
import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.TeacherDao;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.dao.impl.SubjectDaoImpl;
import com.scolaris.dao.impl.TeacherDaoImpl;
import com.scolaris.model.Module;
import com.scolaris.model.Subject;
import com.scolaris.model.Teacher;
import com.scolaris.model.Student;
import com.scolaris.dao.StudentDao;
import com.scolaris.service.AuthenticationService;
import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);


    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private VBox teacherSection;

    @FXML
    private TextField specialtyField;

    @FXML
    private VBox moduleSection;

    @FXML
    private ComboBox<Module> moduleComboBox;

    @FXML
    private Label messageLabel;

    private AuthenticationService authService;
    private ModuleDao moduleDao;
    private SubjectDao subjectDao;
    private TeacherDao teacherDao;
    private StudentDao studentDao;

    public void initialize() {
        authService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.AuthenticationService.class);
        moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        teacherDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.TeacherDao.class);
        studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);

        roleComboBox.getItems().addAll("ADMIN", "TEACHER", "STUDENT");

        moduleComboBox.setConverter(new javafx.util.StringConverter<Module>() {
            @Override
            public String toString(Module m) {
                return m == null ? "" : m.getCode() + " - " + m.getName();
            }
            @Override
            public Module fromString(String string) { return null; }
        });

        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTeacher = "TEACHER".equals(newVal);
            teacherSection.setVisible(isTeacher);
            teacherSection.setManaged(isTeacher);
            moduleSection.setVisible(isTeacher);
            moduleSection.setManaged(isTeacher);
            if (isTeacher) {
                populateModules();
            }
        });
    }

    private void populateModules() {
        moduleComboBox.getItems().clear();
        moduleComboBox.setValue(null);
        moduleComboBox.setPromptText("Chargement...");

        List<Module> allModules = moduleDao.findAll();
        moduleComboBox.getItems().addAll(allModules);

        if (moduleComboBox.getItems().isEmpty()) {
            moduleComboBox.setPromptText("Aucun module disponible");
        } else {
            moduleComboBox.setPromptText("Sélectionnez votre module");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (username == null || username.isEmpty() || password == null || password.isEmpty() || role == null ||
            firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
            email == null || email.isEmpty() || phone == null || phone.isEmpty()) {
            showMessage("Veuillez remplir tous les champs obligatoires.", true);
            return;
        }

        Module selectedModule = null;
        String specialty = "";
        if ("TEACHER".equals(role)) {
            selectedModule = moduleComboBox.getValue();
            specialty = specialtyField.getText();
            if (selectedModule == null || specialty == null || specialty.isEmpty()) {
                showMessage("Veuillez remplir la spécialité et sélectionner un module.", true);
                return;
            }
        }

        boolean userCreated = authService.register(username, password, role);

        if (!userCreated) {
            showMessage("Erreur : Ce nom d'utilisateur existe déjà.", true);
            return;
        }

        if ("TEACHER".equals(role) && selectedModule != null) {
            Optional<Teacher> existingTeacher = teacherDao.findByCin(username);
            if (existingTeacher.isEmpty()) {
                Teacher newTeacher = new Teacher();
                newTeacher.setCin(username);
                newTeacher.setFirstName(firstName);
                newTeacher.setLastName(lastName);
                newTeacher.setEmail(email);
                newTeacher.setPhone(phone);
                newTeacher.setSpecialty(specialty);
                teacherDao.save(newTeacher);

                existingTeacher = teacherDao.findByCin(username);
            }

            if (existingTeacher.isPresent()) {
                Teacher teacher = existingTeacher.get();
                List<Subject> moduleSubjects = subjectDao.findByModuleId(selectedModule.getId());
                for (Subject s : moduleSubjects) {
                    s.setTeacherId(teacher.getId());
                    subjectDao.update(s);
                }
            }
        } else if ("STUDENT".equals(role)) {
            Optional<Student> existingStudent = studentDao.findByCne(username);
            if (existingStudent.isEmpty()) {
                Student newStudent = new Student(
                    0, 
                    username, 
                    firstName, 
                    lastName, 
                    LocalDate.now().minusYears(20), 
                    email, 
                    phone, 
                    LocalDate.now(), 
                    "ACTIVE"
                );
                studentDao.save(newStudent);
            }
        }

        showMessage("Compte créé avec succès ! Vous pouvez vous connecter.", false);
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        specialtyField.clear();
        roleComboBox.setValue(null);
        moduleComboBox.setValue(null);
        teacherSection.setVisible(false);
        teacherSection.setManaged(false);
        moduleSection.setVisible(false);
        moduleSection.setManaged(false);
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            MainApp.setRoot("login");
        } catch (IOException e) {
            logger.error("Error occurred in RegisterController", e);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
        messageLabel.setVisible(true);
    }
}
