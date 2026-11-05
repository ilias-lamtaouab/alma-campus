package com.scolaris.dao.impl;

import com.scolaris.dao.StudentDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.Student;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDaoImpl implements StudentDao {

    private static final Logger logger = LoggerFactory.getLogger(StudentDaoImpl.class);

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO students (cne, first_name, last_name, date_of_birth, email, phone, enrollment_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, student);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        student.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to save student: {}", student.getCne(), e);
            throw new DatabaseException("Failed to save student", e);
        }
        return student;
    }

    @Override
    public Optional<Student> findById(Integer id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractStudentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find student by id: {}", id, e);
            throw new DatabaseException("Failed to find student by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Failed to find all students", e);
            throw new DatabaseException("Failed to find all students", e);
        }
        return students;
    }

    @Override
    public Student update(Student student) {
        String sql = "UPDATE students SET cne = ?, first_name = ?, last_name = ?, date_of_birth = ?, email = ?, phone = ?, enrollment_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStatementParameters(stmt, student);
            stmt.setInt(9, student.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update student: {}", student.getCne(), e);
            throw new DatabaseException("Failed to update student", e);
        }
        return student;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to delete student with id: {}", id, e);
            throw new DatabaseException("Failed to delete student with id: " + id, e);
        }
    }

    @Override
    public Optional<Student> findByCne(String cne) {
        String sql = "SELECT * FROM students WHERE cne = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cne);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractStudentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find student by cne: {}", cne, e);
            throw new DatabaseException("Failed to find student by cne: " + cne, e);
        }
        return Optional.empty();
    }

    private void setStatementParameters(PreparedStatement stmt, Student student) throws SQLException {
        stmt.setString(1, student.getCne());
        stmt.setString(2, student.getFirstName());
        stmt.setString(3, student.getLastName());
        stmt.setDate(4, Date.valueOf(student.getDateOfBirth()));
        stmt.setString(5, student.getEmail());
        stmt.setString(6, student.getPhone());
        stmt.setDate(7, Date.valueOf(student.getEnrollmentDate()));
        stmt.setString(8, student.getStatus());
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setCne(rs.getString("cne"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
        student.setStatus(rs.getString("status"));
        return student;
    }
}
