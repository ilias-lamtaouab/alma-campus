package com.scolaris.model;

import java.time.LocalDate;

public class CertificateRequest {
    private int id;
    private int studentId;
    private LocalDate requestDate;
    private String status;
    private LocalDate approvedDate;
    private String documentType;

    // Helper fields for UI display
    private String studentName;
    private String studentCne;

    public CertificateRequest() {}

    public CertificateRequest(int id, int studentId, LocalDate requestDate, String status, LocalDate approvedDate) {
        this.id = id;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.status = status;
        this.approvedDate = approvedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCne() { return studentCne; }
    public void setStudentCne(String studentCne) { this.studentCne = studentCne; }
}
