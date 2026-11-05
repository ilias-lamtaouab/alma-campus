package com.scolaris.service;

import com.scolaris.dao.GradeDao;
import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.impl.GradeDaoImpl;
import com.scolaris.dao.impl.SubjectDaoImpl;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.model.Grade;
import com.scolaris.model.Subject;
import com.scolaris.model.Module;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeService {

    private final GradeDao gradeDao;
    private final SubjectDao subjectDao;
    private final ModuleDao moduleDao;

    public GradeService() {
        this.gradeDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.GradeDao.class);
        this.subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        this.moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
    }

    public List<Grade> getAllGrades() {
        return gradeDao.findAll();
    }

    public List<Grade> getGradesByStudent(int studentId) {
        return gradeDao.findByStudentId(studentId);
    }

    public boolean addOrUpdateGrade(int studentId, int subjectId, double value, String type, LocalDate date) {
        if (value < 0 || value > 20 || type == null || date == null) {
            return false;
        }

        Grade grade = new Grade();
        grade.setStudentId(studentId);
        grade.setSubjectId(subjectId);
        grade.setGradeValue(value);
        grade.setEvaluationType(type);
        grade.setEvaluationDate(date);

        return gradeDao.save(grade).getId() > 0;
    }

    public void deleteGrade(int id) {
        gradeDao.delete(id);
    }

    /**
     * Calcul de la moyenne pour une matière spécifique d'un étudiant.
     * Formule de pondération standard :
     * - CC (30%), PROJECT (20%), EXAM (50%) si les trois sont présents.
     * - CC (40%), EXAM (60%) si CC et EXAM sont présents.
     * - EXAM (100%) si seul l'examen est présent.
     */
    public double calculateSubjectAverage(int studentId, int subjectId) {
        List<Grade> studentGrades = gradeDao.findByStudentId(studentId);
        double cc = -1;
        double exam = -1;
        double project = -1;

        for (Grade g : studentGrades) {
            if (g.getSubjectId() == subjectId) {
                switch (g.getEvaluationType()) {
                    case "CC": cc = g.getGradeValue(); break;
                    case "EXAM": exam = g.getGradeValue(); break;
                    case "PROJECT": project = g.getGradeValue(); break;
                }
            }
        }

        if (cc >= 0 && exam >= 0 && project >= 0) {
            return (cc * 0.3) + (project * 0.2) + (exam * 0.5);
        } else if (cc >= 0 && exam >= 0) {
            return (cc * 0.4) + (exam * 0.6);
        } else if (exam >= 0) {
            return exam;
        } else if (cc >= 0) {
            return cc;
        } else if (project >= 0) {
            return project;
        }
        return 0.0;
    }

    /**
     * Calcul de la moyenne d'un module pour un étudiant.
     * Moyenne pondérée par le coefficient de chaque matière.
     */
    public double calculateModuleAverage(int studentId, int moduleId) {
        List<Subject> moduleSubjects = subjectDao.findByModuleId(moduleId);
        if (moduleSubjects.isEmpty()) return 0.0;

        double weightedSum = 0;
        double coeffSum = 0;
        int evaluatedSubjects = 0;

        for (Subject sub : moduleSubjects) {
            double avg = calculateSubjectAverage(studentId, sub.getId());
            // Double check if subject has any grades before counting it, to avoid penalizing students for unreleased marks
            if (hasGrades(studentId, sub.getId())) {
                weightedSum += avg * sub.getCoefficient();
                coeffSum += sub.getCoefficient();
                evaluatedSubjects++;
            }
        }

        if (coeffSum == 0) return 0.0;
        return weightedSum / coeffSum;
    }

    private boolean hasGrades(int studentId, int subjectId) {
        List<Grade> grades = gradeDao.findByStudentId(studentId);
        for (Grade g : grades) {
            if (g.getSubjectId() == subjectId) return true;
        }
        return false;
    }

    /**
     * Calcul de la moyenne générale d'un étudiant.
     * Moyenne simple ou pondérée de tous les modules suivis.
     */
    public double calculateGeneralAverage(int studentId) {
        List<Module> allModules = moduleDao.findAll();
        if (allModules.isEmpty()) return 0.0;

        double sum = 0;
        int count = 0;

        for (Module mod : allModules) {
            // Count module only if it has subjects and at least one subject has grades
            List<Subject> subs = subjectDao.findByModuleId(mod.getId());
            boolean active = false;
            for (Subject s : subs) {
                if (hasGrades(studentId, s.getId())) {
                    active = true;
                    break;
                }
            }
            if (active) {
                sum += calculateModuleAverage(studentId, mod.getId());
                count++;
            }
        }

        if (count == 0) return 0.0;
        return sum / count;
    }

    /**
     * Calcule la moyenne générale de l'ensemble de l'école.
     */
    public double calculateSchoolAverage() {
        // Fetch all students and compute their general average
        List<Grade> allGrades = gradeDao.findAll();
        if (allGrades.isEmpty()) return 0.0;

        // Distinct student IDs
        List<Integer> studentIds = new ArrayList<>();
        for (Grade g : allGrades) {
            if (!studentIds.contains(g.getStudentId())) {
                studentIds.add(g.getStudentId());
            }
        }

        if (studentIds.isEmpty()) return 0.0;

        double sum = 0;
        for (int id : studentIds) {
            sum += calculateGeneralAverage(id);
        }
        return sum / studentIds.size();
    }
}
