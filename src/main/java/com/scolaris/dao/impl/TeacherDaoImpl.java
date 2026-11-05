package com.scolaris.dao.impl;

import com.scolaris.dao.TeacherDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.Teacher;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherDaoImpl implements TeacherDao {

    private static final Logger logger = LoggerFactory.getLogger(TeacherDaoImpl.class);


    @Override
    public Teacher save(Teacher teacher) {
        String sql = "INSERT INTO teachers (cin, first_name, last_name, email, phone, specialty) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setStatementParameters(stmt, teacher);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        teacher.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
        return teacher;
    }

    @Override
    public Optional<Teacher> findById(Integer id) {
        String sql = "SELECT * FROM teachers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractTeacherFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Teacher> findAll() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                teachers.add(extractTeacherFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
        return teachers;
    }

    @Override
    public Teacher update(Teacher teacher) {
        String sql = "UPDATE teachers SET cin = ?, first_name = ?, last_name = ?, email = ?, phone = ?, specialty = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStatementParameters(stmt, teacher);
            stmt.setInt(7, teacher.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
        return teacher;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
    }

    @Override
    public Optional<Teacher> findByCin(String cin) {
        String sql = "SELECT * FROM teachers WHERE cin = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractTeacherFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in TeacherDaoImpl", e);
            throw new DatabaseException("Database error in TeacherDaoImpl", e);
        }
        return Optional.empty();
    }

    private void setStatementParameters(PreparedStatement stmt, Teacher teacher) throws SQLException {
        stmt.setString(1, teacher.getCin());
        stmt.setString(2, teacher.getFirstName());
        stmt.setString(3, teacher.getLastName());
        stmt.setString(4, teacher.getEmail());
        stmt.setString(5, teacher.getPhone());
        stmt.setString(6, teacher.getSpecialty());
    }

    private Teacher extractTeacherFromResultSet(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setId(rs.getInt("id"));
        teacher.setCin(rs.getString("cin"));
        teacher.setFirstName(rs.getString("first_name"));
        teacher.setLastName(rs.getString("last_name"));
        teacher.setEmail(rs.getString("email"));
        teacher.setPhone(rs.getString("phone"));
        teacher.setSpecialty(rs.getString("specialty"));
        return teacher;
    }
}
