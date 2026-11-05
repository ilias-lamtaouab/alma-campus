package com.scolaris.model;

public class Teacher {
    private int id;
    private String cin;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialty;

    public Teacher() {}

    public Teacher(int id, String cin, String firstName, String lastName, String email, String phone, String specialty) {
        this.id = id;
        this.cin = cin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.specialty = specialty;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
