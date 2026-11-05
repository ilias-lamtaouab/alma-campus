package com.scolaris.dao.impl;

import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.Module;
import com.scolaris.model.Subject;
import com.scolaris.model.Teacher;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDaoImpl implements SubjectDao {

    private static final Logger logger = LoggerFactory.getLogger(SubjectDaoImpl.class);


    @Override
    public Subject save(Subject subject) {
        String sql = "INSERT INTO subjects (name, coefficient, module_id, teacher_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, subject.getName());
            stmt.setDouble(2, subject.getCoefficient());
            if (subject.getModuleId() != null) {
                stmt.setInt(3, subject.getModuleId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            if (subject.getTeacherId() != null) {
                stmt.setInt(4, subject.getTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        subject.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return subject;
    }

    @Override
    public Optional<Subject> findById(Integer id) {
        String sql = "SELECT s.*, m.code AS module_code, m.name AS module_name, m.semester AS module_semester, " +
                     "t.first_name AS teacher_first, t.last_name AS teacher_last, t.cin AS teacher_cin, t.email AS teacher_email, t.phone AS teacher_phone, t.specialty AS teacher_specialty " +
                     "FROM subjects s " +
                     "LEFT JOIN modules m ON s.module_id = m.id " +
                     "LEFT JOIN teachers t ON s.teacher_id = t.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractSubjectFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, m.code AS module_code, m.name AS module_name, m.semester AS module_semester, " +
                     "t.first_name AS teacher_first, t.last_name AS teacher_last, t.cin AS teacher_cin, t.email AS teacher_email, t.phone AS teacher_phone, t.specialty AS teacher_specialty " +
                     "FROM subjects s " +
                     "LEFT JOIN modules m ON s.module_id = m.id " +
                     "LEFT JOIN teachers t ON s.teacher_id = t.id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subjects.add(extractSubjectFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return subjects;
    }

    @Override
    public Subject update(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, coefficient = ?, module_id = ?, teacher_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subject.getName());
            stmt.setDouble(2, subject.getCoefficient());
            if (subject.getModuleId() != null) {
                stmt.setInt(3, subject.getModuleId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            if (subject.getTeacherId() != null) {
                stmt.setInt(4, subject.getTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, subject.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return subject;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
    }

    @Override
    public List<Subject> findByModuleId(int moduleId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, m.code AS module_code, m.name AS module_name, m.semester AS module_semester, " +
                     "t.first_name AS teacher_first, t.last_name AS teacher_last, t.cin AS teacher_cin, t.email AS teacher_email, t.phone AS teacher_phone, t.specialty AS teacher_specialty " +
                     "FROM subjects s " +
                     "LEFT JOIN modules m ON s.module_id = m.id " +
                     "LEFT JOIN teachers t ON s.teacher_id = t.id " +
                     "WHERE s.module_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(extractSubjectFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return subjects;
    }

    @Override
    public List<Subject> findByTeacherId(int teacherId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, m.code AS module_code, m.name AS module_name, m.semester AS module_semester, " +
                     "t.first_name AS teacher_first, t.last_name AS teacher_last, t.cin AS teacher_cin, t.email AS teacher_email, t.phone AS teacher_phone, t.specialty AS teacher_specialty " +
                     "FROM subjects s " +
                     "LEFT JOIN modules m ON s.module_id = m.id " +
                     "LEFT JOIN teachers t ON s.teacher_id = t.id " +
                     "WHERE s.teacher_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(extractSubjectFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SubjectDaoImpl", e);
            throw new DatabaseException("Database error in SubjectDaoImpl", e);
        }
        return subjects;
    }

    private Subject extractSubjectFromResultSet(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getInt("id"));
        subject.setName(rs.getString("name"));
        subject.setCoefficient(rs.getDouble("coefficient"));
        
        int modId = rs.getInt("module_id");
        if (!rs.wasNull()) {
            subject.setModuleId(modId);
            Module module = new Module();
            module.setId(modId);
            module.setCode(rs.getString("module_code"));
            module.setName(rs.getString("module_name"));
            module.setSemester(rs.getInt("module_semester"));
            subject.setModule(module);
        }
        
        int teachId = rs.getInt("teacher_id");
        if (!rs.wasNull()) {
            subject.setTeacherId(teachId);
            Teacher teacher = new Teacher();
            teacher.setId(teachId);
            teacher.setCin(rs.getString("teacher_cin"));
            teacher.setFirstName(rs.getString("teacher_first"));
            teacher.setLastName(rs.getString("teacher_last"));
            teacher.setEmail(rs.getString("teacher_email"));
            teacher.setPhone(rs.getString("teacher_phone"));
            teacher.setSpecialty(rs.getString("teacher_specialty"));
            subject.setTeacher(teacher);
        }
        
        return subject;
    }
}
