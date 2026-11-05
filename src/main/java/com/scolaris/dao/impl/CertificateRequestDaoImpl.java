package com.scolaris.dao.impl;

import com.scolaris.dao.CertificateRequestDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.CertificateRequest;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CertificateRequestDaoImpl implements CertificateRequestDao {

    private static final Logger logger = LoggerFactory.getLogger(CertificateRequestDaoImpl.class);


    @Override
    public boolean save(CertificateRequest request) {
        String sql = "INSERT INTO certificate_requests (student_id, request_date, status, document_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getStudentId());
            ps.setDate(2, Date.valueOf(request.getRequestDate()));
            ps.setString(3, request.getStatus());
            ps.setString(4, request.getDocumentType() != null ? request.getDocumentType() : "Attestation de scolarité");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        request.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Database error in CertificateRequestDaoImpl", e);
            throw new DatabaseException("Database error in CertificateRequestDaoImpl", e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(int requestId, String status, LocalDate approvedDate) {
        String sql = "UPDATE certificate_requests SET status = ?, approved_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedDate != null) {
                ps.setDate(2, Date.valueOf(approvedDate));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error in CertificateRequestDaoImpl", e);
            throw new DatabaseException("Database error in CertificateRequestDaoImpl", e);
        }
    }

    @Override
    public List<CertificateRequest> findAll() {
        List<CertificateRequest> list = new ArrayList<>();
        String sql = "SELECT cr.*, s.first_name, s.last_name, s.cne FROM certificate_requests cr " +
                     "JOIN students s ON cr.student_id = s.id ORDER BY cr.request_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CertificateRequest cr = mapResultSet(rs);
                cr.setStudentName(rs.getString("last_name").toUpperCase() + " " + rs.getString("first_name"));
                cr.setStudentCne(rs.getString("cne"));
                list.add(cr);
            }
        } catch (SQLException e) {
            logger.error("Database error in CertificateRequestDaoImpl", e);
            throw new DatabaseException("Database error in CertificateRequestDaoImpl", e);
        }
        return list;
    }

    @Override
    public List<CertificateRequest> findByStudentId(int studentId) {
        List<CertificateRequest> list = new ArrayList<>();
        String sql = "SELECT cr.*, s.first_name, s.last_name, s.cne FROM certificate_requests cr " +
                     "JOIN students s ON cr.student_id = s.id WHERE cr.student_id = ? ORDER BY cr.request_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CertificateRequest cr = mapResultSet(rs);
                    cr.setStudentName(rs.getString("last_name").toUpperCase() + " " + rs.getString("first_name"));
                    cr.setStudentCne(rs.getString("cne"));
                    list.add(cr);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in CertificateRequestDaoImpl", e);
            throw new DatabaseException("Database error in CertificateRequestDaoImpl", e);
        }
        return list;
    }

    @Override
    public Optional<CertificateRequest> findById(int id) {
        String sql = "SELECT cr.*, s.first_name, s.last_name, s.cne FROM certificate_requests cr " +
                     "JOIN students s ON cr.student_id = s.id WHERE cr.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CertificateRequest cr = mapResultSet(rs);
                    cr.setStudentName(rs.getString("last_name").toUpperCase() + " " + rs.getString("first_name"));
                    cr.setStudentCne(rs.getString("cne"));
                    return Optional.of(cr);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in CertificateRequestDaoImpl", e);
            throw new DatabaseException("Database error in CertificateRequestDaoImpl", e);
        }
        return Optional.empty();
    }

    private CertificateRequest mapResultSet(ResultSet rs) throws SQLException {
        CertificateRequest cr = new CertificateRequest();
        cr.setId(rs.getInt("id"));
        cr.setStudentId(rs.getInt("student_id"));
        cr.setRequestDate(rs.getDate("request_date").toLocalDate());
        cr.setStatus(rs.getString("status"));
        Date appDate = rs.getDate("approved_date");
        if (appDate != null) {
            cr.setApprovedDate(appDate.toLocalDate());
        }
        
        try {
            cr.setDocumentType(rs.getString("document_type"));
        } catch (SQLException e) {
            cr.setDocumentType("Attestation de scolarité");
        }
        
        return cr;
    }
}
