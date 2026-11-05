package com.scolaris.model;

public class Subject {
    private int id;
    private String name;
    private double coefficient;
    private Integer moduleId; // Can be null
    private Integer teacherId; // Can be null

    // Helper references (optional, useful for UI)
    private Module module;
    private Teacher teacher;

    public Subject() {}

    public Subject(int id, String name, double coefficient, Integer moduleId, Integer teacherId) {
        this.id = id;
        this.name = name;
        this.coefficient = coefficient;
        this.moduleId = moduleId;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public Integer getModuleId() { return moduleId; }
    public void setModuleId(Integer moduleId) { this.moduleId = moduleId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}
