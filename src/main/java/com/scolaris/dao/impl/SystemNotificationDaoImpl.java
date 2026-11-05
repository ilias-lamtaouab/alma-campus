package com.scolaris.dao.impl;

import com.scolaris.dao.SystemNotificationDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.SystemNotification;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemNotificationDaoImpl implements SystemNotificationDao {

    private static final Logger logger = LoggerFactory.getLogger(SystemNotificationDaoImpl.class);


    @Override
    public SystemNotification save(SystemNotification notification) {
        String sql = "INSERT INTO system_notifications (sender, target_type, target_username, title, content, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, notification.getSender() != null ? notification.getSender() : "ADMIN");
            stmt.setString(2, notification.getTargetType());
            stmt.setString(3, notification.getTargetUsername());
            stmt.setString(4, notification.getTitle());
            stmt.setString(5, notification.getContent());
            
            LocalDateTime now = notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(now));

            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                    notification.setCreatedAt(now);
                }
            }
            return notification;
        } catch (SQLException e) {
            logger.error("Error occurred in SystemNotificationDaoImpl", e);
            return null;
        }
    }

    @Override
    public Optional<SystemNotification> findById(Integer id) {
        String sql = "SELECT * FROM system_notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SystemNotificationDaoImpl", e);
            throw new DatabaseException("Database error in SystemNotificationDaoImpl", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SystemNotification> findAll() {
        List<SystemNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM system_notifications ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in SystemNotificationDaoImpl", e);
            throw new DatabaseException("Database error in SystemNotificationDaoImpl", e);
        }
        return list;
    }

    @Override
    public SystemNotification update(SystemNotification notification) {
        String sql = "UPDATE system_notifications SET sender = ?, target_type = ?, target_username = ?, title = ?, content = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notification.getSender());
            stmt.setString(2, notification.getTargetType());
            stmt.setString(3, notification.getTargetUsername());
            stmt.setString(4, notification.getTitle());
            stmt.setString(5, notification.getContent());
            stmt.setInt(6, notification.getId());
            stmt.executeUpdate();
            return notification;
        } catch (SQLException e) {
            logger.error("Error occurred in SystemNotificationDaoImpl", e);
            return null;
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM system_notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in SystemNotificationDaoImpl", e);
            throw new DatabaseException("Database error in SystemNotificationDaoImpl", e);
        }
    }

    @Override
    public List<SystemNotification> findForStudent(String username) {
        List<SystemNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM system_notifications WHERE target_type = 'ALL' OR target_type = 'ALL_STUDENTS' " +
                     "OR (target_type = 'SPECIFIC_STUDENT' AND target_username = ?) ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SystemNotificationDaoImpl", e);
            throw new DatabaseException("Database error in SystemNotificationDaoImpl", e);
        }
        return list;
    }

    @Override
    public List<SystemNotification> findForTeacher(String username) {
        List<SystemNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM system_notifications WHERE target_type = 'ALL' OR target_type = 'ALL_TEACHERS' " +
                     "OR (target_type = 'SPECIFIC_TEACHER' AND target_username = ?) ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in SystemNotificationDaoImpl", e);
            throw new DatabaseException("Database error in SystemNotificationDaoImpl", e);
        }
        return list;
    }

    private SystemNotification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new SystemNotification(
            rs.getInt("id"),
            rs.getString("sender"),
            rs.getString("target_type"),
            rs.getString("target_username"),
            rs.getString("title"),
            rs.getString("content"),
            ldt
        );
    }
}
