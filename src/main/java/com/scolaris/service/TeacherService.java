package com.scolaris.service;

import com.scolaris.dao.TeacherDao;
import com.scolaris.dao.impl.TeacherDaoImpl;
import com.scolaris.model.Teacher;

import java.util.List;
import java.util.Optional;

public class TeacherService {

    private final TeacherDao teacherDao;

    public TeacherService() {
        this.teacherDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.TeacherDao.class);
    }

    public List<Teacher> getAllTeachers() {
        return teacherDao.findAll();
    }

    public Optional<Teacher> getTeacherById(int id) {
        return teacherDao.findById(id);
    }

    public boolean addTeacher(String cin, String firstName, String lastName, String email, String phone, String specialty) {
        if (cin == null || cin.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return false;
        }

        // Check unique CIN
        if (teacherDao.findByCin(cin).isPresent()) {
            return false;
        }

        Teacher teacher = new Teacher();
        teacher.setCin(cin);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setPhone(phone);
        teacher.setSpecialty(specialty);

        return teacherDao.save(teacher).getId() > 0;
    }

    public boolean updateTeacher(int id, String cin, String firstName, String lastName, String email, String phone, String specialty) {
        Optional<Teacher> optTeacher = teacherDao.findById(id);
        if (optTeacher.isEmpty()) {
            return false;
        }

        // Check if new CIN is taken by another teacher
        Optional<Teacher> checkCin = teacherDao.findByCin(cin);
        if (checkCin.isPresent() && checkCin.get().getId() != id) {
            return false;
        }

        Teacher teacher = optTeacher.get();
        teacher.setCin(cin);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setPhone(phone);
        teacher.setSpecialty(specialty);

        teacherDao.update(teacher);
        return true;
    }

    public void deleteTeacher(int id) {
        teacherDao.delete(id);
    }
}
