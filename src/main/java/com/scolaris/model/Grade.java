package com.scolaris.model;

import java.time.LocalDate;

public class Grade {
    private int id;
    private int studentId;
    private int subjectId;
    private double gradeValue;
    private String evaluationType;
    private LocalDate evaluationDate;

    // Helper references
    private Student student;
    private Subject subject;

    public Grade() {}

    public Grade(int id, int studentId, int subjectId, double gradeValue, String evaluationType, LocalDate evaluationDate) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.gradeValue = gradeValue;
        this.evaluationType = evaluationType;
        this.evaluationDate = evaluationDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public double getGradeValue() { return gradeValue; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }

    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }

    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
}
