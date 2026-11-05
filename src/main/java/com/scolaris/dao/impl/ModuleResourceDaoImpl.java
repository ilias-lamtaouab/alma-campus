package com.scolaris.dao.impl;

import com.scolaris.dao.ModuleResourceDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.ModuleResource;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModuleResourceDaoImpl implements ModuleResourceDao {

    private static final Logger logger = LoggerFactory.getLogger(ModuleResourceDaoImpl.class);


    @Override
    public ModuleResource save(ModuleResource resource) {
        String sql = "INSERT INTO module_resources (module_id, title, description, file_name, file_path, file_size, uploaded_by, uploaded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, resource.getModuleId());
            stmt.setString(2, resource.getTitle());
            stmt.setString(3, resource.getDescription());
            stmt.setString(4, resource.getFileName());
            stmt.setString(5, resource.getFilePath());
            stmt.setLong(6, resource.getFileSize());
            stmt.setString(7, resource.getUploadedBy());
            stmt.setTimestamp(8, Timestamp.valueOf(resource.getUploadedAt() != null ? resource.getUploadedAt() : LocalDateTime.now()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    resource.setId(rs.getInt(1));
                }
            }
            return resource;
        } catch (SQLException e) {
            logger.error("Error occurred in ModuleResourceDaoImpl", e);
            return null;
        }
    }

    @Override
    public List<ModuleResource> findAll() {
        List<ModuleResource> list = new ArrayList<>();
        String sql = "SELECT r.*, m.name AS module_name, m.code AS module_code FROM module_resources r JOIN modules m ON r.module_id = m.id ORDER BY r.uploaded_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleResourceDaoImpl", e);
            throw new DatabaseException("Database error in ModuleResourceDaoImpl", e);
        }
        return list;
    }

    @Override
    public List<ModuleResource> findByModuleId(int moduleId) {
        List<ModuleResource> list = new ArrayList<>();
        String sql = "SELECT r.*, m.name AS module_name, m.code AS module_code FROM module_resources r JOIN modules m ON r.module_id = m.id WHERE r.module_id = ? ORDER BY r.uploaded_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleResourceDaoImpl", e);
            throw new DatabaseException("Database error in ModuleResourceDaoImpl", e);
        }
        return list;
    }

    @Override
    public ModuleResource findById(int id) {
        String sql = "SELECT r.*, m.name AS module_name, m.code AS module_code FROM module_resources r JOIN modules m ON r.module_id = m.id WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleResourceDaoImpl", e);
            throw new DatabaseException("Database error in ModuleResourceDaoImpl", e);
        }
        return null;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM module_resources WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error occurred in ModuleResourceDaoImpl", e);
            return false;
        }
    }

    private ModuleResource mapResultSet(ResultSet rs) throws SQLException {
        ModuleResource r = new ModuleResource();
        r.setId(rs.getInt("id"));
        r.setModuleId(rs.getInt("module_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setFileName(rs.getString("file_name"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileSize(rs.getLong("file_size"));
        r.setUploadedBy(rs.getString("uploaded_by"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) r.setUploadedAt(ts.toLocalDateTime());
        r.setModuleName(rs.getString("module_name"));
        r.setModuleCode(rs.getString("module_code"));
        return r;
    }
}