package com.scolaris.service;

import com.scolaris.dao.CertificateRequestDao;
import com.scolaris.dao.impl.CertificateRequestDaoImpl;
import com.scolaris.model.CertificateRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CertificateService {
    private final CertificateRequestDao requestDao;

    public CertificateService() {
        this.requestDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.CertificateRequestDao.class);
    }

    public boolean requestCertificate(int studentId, String documentType) {
        // Prevent duplicate pending requests for the SAME document type
        List<CertificateRequest> existing = requestDao.findByStudentId(studentId);
        for (CertificateRequest cr : existing) {
            if ("PENDING".equals(cr.getStatus()) && 
                (cr.getDocumentType() == null || cr.getDocumentType().equals(documentType))) {
                return false; 
            }
        }
        
        CertificateRequest req = new CertificateRequest();
        req.setStudentId(studentId);
        req.setRequestDate(LocalDate.now());
        req.setStatus("PENDING");
        req.setDocumentType(documentType);
        return requestDao.save(req);
    }

    public boolean approveRequest(int requestId) {
        return requestDao.updateStatus(requestId, "APPROVED", LocalDate.now());
    }

    public boolean rejectRequest(int requestId) {
        return requestDao.updateStatus(requestId, "REJECTED", null);
    }

    public List<CertificateRequest> getAllRequests() {
        return requestDao.findAll();
    }

    public List<CertificateRequest> getRequestsByStudent(int studentId) {
        return requestDao.findByStudentId(studentId);
    }

    public Optional<CertificateRequest> getRequestById(int id) {
        return requestDao.findById(id);
    }
}
