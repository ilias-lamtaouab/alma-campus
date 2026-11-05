package com.scolaris.model;

public class Module {
    private int id;
    private String code;
    private String name;
    private int semester;

    public Module() {}

    public Module(int id, String code, String name, int semester) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.semester = semester;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
}
