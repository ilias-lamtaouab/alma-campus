package com.scolaris.dao;

import com.scolaris.model.CertificateRequest;
import java.util.List;
import java.util.Optional;

public interface CertificateRequestDao {
    boolean save(CertificateRequest request);
    boolean updateStatus(int requestId, String status, java.time.LocalDate approvedDate);
    List<CertificateRequest> findAll();
    List<CertificateRequest> findByStudentId(int studentId);
    Optional<CertificateRequest> findById(int id);
}
