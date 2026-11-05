package com.scolaris.dao.impl;

import com.scolaris.dao.GradeDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.Grade;
import com.scolaris.model.Student;
import com.scolaris.model.Subject;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GradeDaoImpl implements GradeDao {

    private static final Logger logger = LoggerFactory.getLogger(GradeDaoImpl.class);


    @Override
    public Grade save(Grade grade) {
        String sql = "INSERT INTO grades (student_id, subject_id, grade_value, evaluation_type, evaluation_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE grade_value = ?, evaluation_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, grade.getStudentId());
            stmt.setInt(2, grade.getSubjectId());
            stmt.setDouble(3, grade.getGradeValue());
            stmt.setString(4, grade.getEvaluationType());
            stmt.setDate(5, Date.valueOf(grade.getEvaluationDate()));
            
            // For ON DUPLICATE KEY UPDATE
            stmt.setDouble(6, grade.getGradeValue());
            stmt.setDate(7, Date.valueOf(grade.getEvaluationDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        grade.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return grade;
    }

    @Override
    public Optional<Grade> findById(Integer id) {
        String sql = "SELECT g.*, " +
                     "st.cne AS student_cne, st.first_name AS student_first, st.last_name AS student_last, st.date_of_birth AS student_dob, st.email AS student_email, st.phone AS student_phone, st.enrollment_date AS student_enrollment, st.status AS student_status, " +
                     "su.name AS subject_name, su.coefficient AS subject_coefficient, su.module_id AS subject_module_id " +
                     "FROM grades g " +
                     "JOIN students st ON g.student_id = st.id " +
                     "JOIN subjects su ON g.subject_id = su.id " +
                     "WHERE g.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractGradeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, " +
                     "st.cne AS student_cne, st.first_name AS student_first, st.last_name AS student_last, st.date_of_birth AS student_dob, st.email AS student_email, st.phone AS student_phone, st.enrollment_date AS student_enrollment, st.status AS student_status, " +
                     "su.name AS subject_name, su.coefficient AS subject_coefficient, su.module_id AS subject_module_id " +
                     "FROM grades g " +
                     "JOIN students st ON g.student_id = st.id " +
                     "JOIN subjects su ON g.subject_id = su.id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return grades;
    }

    @Override
    public Grade update(Grade grade) {
        String sql = "UPDATE grades SET student_id = ?, subject_id = ?, grade_value = ?, evaluation_type = ?, evaluation_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, grade.getStudentId());
            stmt.setInt(2, grade.getSubjectId());
            stmt.setDouble(3, grade.getGradeValue());
            stmt.setString(4, grade.getEvaluationType());
            stmt.setDate(5, Date.valueOf(grade.getEvaluationDate()));
            stmt.setInt(6, grade.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return grade;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
    }

    @Override
    public List<Grade> findByStudentId(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, " +
                     "st.cne AS student_cne, st.first_name AS student_first, st.last_name AS student_last, st.date_of_birth AS student_dob, st.email AS student_email, st.phone AS student_phone, st.enrollment_date AS student_enrollment, st.status AS student_status, " +
                     "su.name AS subject_name, su.coefficient AS subject_coefficient, su.module_id AS subject_module_id " +
                     "FROM grades g " +
                     "JOIN students st ON g.student_id = st.id " +
                     "JOIN subjects su ON g.subject_id = su.id " +
                     "WHERE g.student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(extractGradeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return grades;
    }

    @Override
    public List<Grade> findBySubjectId(int subjectId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, " +
                     "st.cne AS student_cne, st.first_name AS student_first, st.last_name AS student_last, st.date_of_birth AS student_dob, st.email AS student_email, st.phone AS student_phone, st.enrollment_date AS student_enrollment, st.status AS student_status, " +
                     "su.name AS subject_name, su.coefficient AS subject_coefficient, su.module_id AS subject_module_id " +
                     "FROM grades g " +
                     "JOIN students st ON g.student_id = st.id " +
                     "JOIN subjects su ON g.subject_id = su.id " +
                     "WHERE g.subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(extractGradeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in GradeDaoImpl", e);
            throw new DatabaseException("Database error in GradeDaoImpl", e);
        }
        return grades;
    }

    private Grade extractGradeFromResultSet(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getInt("id"));
        grade.setStudentId(rs.getInt("student_id"));
        grade.setSubjectId(rs.getInt("subject_id"));
        grade.setGradeValue(rs.getDouble("grade_value"));
        grade.setEvaluationType(rs.getString("evaluation_type"));
        grade.setEvaluationDate(rs.getDate("evaluation_date").toLocalDate());
        
        // Populate student helper
        Student student = new Student();
        student.setId(rs.getInt("student_id"));
        student.setCne(rs.getString("student_cne"));
        student.setFirstName(rs.getString("student_first"));
        student.setLastName(rs.getString("student_last"));
        student.setDateOfBirth(rs.getDate("student_dob").toLocalDate());
        student.setEmail(rs.getString("student_email"));
        student.setPhone(rs.getString("student_phone"));
        student.setEnrollmentDate(rs.getDate("student_enrollment").toLocalDate());
        student.setStatus(rs.getString("student_status"));
        grade.setStudent(student);
        
        // Populate subject helper
        Subject subject = new Subject();
        subject.setId(rs.getInt("subject_id"));
        subject.setName(rs.getString("subject_name"));
        subject.setCoefficient(rs.getDouble("subject_coefficient"));
        int modId = rs.getInt("subject_module_id");
        if (!rs.wasNull()) {
            subject.setModuleId(modId);
        }
        grade.setSubject(subject);
        
        return grade;
    }
}
