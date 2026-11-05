package com.scolaris.service;

import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.dao.impl.SubjectDaoImpl;
import com.scolaris.model.Module;
import com.scolaris.model.Subject;

import java.util.List;
import java.util.Optional;

public class ModuleSubjectService {

    private final ModuleDao moduleDao;
    private final SubjectDao subjectDao;

    public ModuleSubjectService() {
        this.moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        this.subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
    }

    // --- Module Operations ---

    public List<Module> getAllModules() {
        return moduleDao.findAll();
    }

    public boolean addModule(String code, String name, int semester) {
        if (code == null || code.isEmpty() || name == null || name.isEmpty() || semester < 1) {
            return false;
        }

        if (moduleDao.findByCode(code).isPresent()) {
            return false; // Code duplicate
        }

        Module module = new Module();
        module.setCode(code);
        module.setName(name);
        module.setSemester(semester);

        return moduleDao.save(module).getId() > 0;
    }

    public boolean updateModule(int id, String code, String name, int semester) {
        Optional<Module> optModule = moduleDao.findById(id);
        if (optModule.isEmpty()) {
            return false;
        }

        Optional<Module> checkCode = moduleDao.findByCode(code);
        if (checkCode.isPresent() && checkCode.get().getId() != id) {
            return false; // Code duplicate
        }

        Module module = optModule.get();
        module.setCode(code);
        module.setName(name);
        module.setSemester(semester);

        moduleDao.update(module);
        return true;
    }

    public void deleteModule(int id) {
        moduleDao.delete(id);
    }

    // --- Subject Operations ---

    public List<Subject> getAllSubjects() {
        return subjectDao.findAll();
    }

    public List<Subject> getSubjectsByModule(int moduleId) {
        return subjectDao.findByModuleId(moduleId);
    }

    public List<Subject> getSubjectsByTeacher(int teacherId) {
        return subjectDao.findByTeacherId(teacherId);
    }

    public boolean addSubject(String name, double coefficient, Integer moduleId, Integer teacherId) {
        if (name == null || name.isEmpty() || coefficient <= 0) {
            return false;
        }

        Subject subject = new Subject();
        subject.setName(name);
        subject.setCoefficient(coefficient);
        subject.setModuleId(moduleId);
        subject.setTeacherId(teacherId);

        return subjectDao.save(subject).getId() > 0;
    }

    public boolean updateSubject(int id, String name, double coefficient, Integer moduleId, Integer teacherId) {
        Optional<Subject> optSubject = subjectDao.findById(id);
        if (optSubject.isEmpty()) {
            return false;
        }

        Subject subject = optSubject.get();
        subject.setName(name);
        subject.setCoefficient(coefficient);
        subject.setModuleId(moduleId);
        subject.setTeacherId(teacherId);

        subjectDao.update(subject);
        return true;
    }

    public void deleteSubject(int id) {
        subjectDao.delete(id);
    }
}
