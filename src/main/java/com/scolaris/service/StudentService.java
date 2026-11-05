package com.scolaris.service;

import com.scolaris.dao.StudentDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.model.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentService {

    private final StudentDao studentDao;

    public StudentService() {
        this.studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
    }

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public boolean addStudent(String cne, String firstName, String lastName, LocalDate dob, String email, String phone, String status) {
        if (cne == null || cne.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return false;
        }

        // Check if CNE already exists
        if (studentDao.findByCne(cne).isPresent()) {
            return false;
        }

        Student student = new Student();
        student.setCne(cne);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setDateOfBirth(dob != null ? dob : LocalDate.now().minusYears(18));
        student.setEmail(email);
        student.setPhone(phone);
        student.setEnrollmentDate(LocalDate.now());
        student.setStatus(status != null ? status : "ACTIVE");

        return studentDao.save(student).getId() > 0;
    }

    public boolean updateStudent(int id, String cne, String firstName, String lastName, LocalDate dob, String email, String phone, String status) {
        Optional<Student> optionalStudent = studentDao.findById(id);
        if (optionalStudent.isEmpty()) return false;

        Student student = optionalStudent.get();
        
        // Ensure new CNE is not used by someone else
        Optional<Student> cneCheck = studentDao.findByCne(cne);
        if (cneCheck.isPresent() && cneCheck.get().getId() != id) {
            return false;
        }

        student.setCne(cne);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        if (dob != null) student.setDateOfBirth(dob);
        student.setEmail(email);
        student.setPhone(phone);
        student.setStatus(status);

        studentDao.update(student);
        return true;
    }

    public void deleteStudent(int id) {
        studentDao.delete(id);
    }
}
