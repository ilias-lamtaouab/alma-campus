package com.scolaris.dao.impl;

import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.db.DatabaseConnection;
import com.scolaris.model.Module;

import com.scolaris.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleDaoImpl implements ModuleDao {

    private static final Logger logger = LoggerFactory.getLogger(ModuleDaoImpl.class);


    @Override
    public Module save(Module module) {
        String sql = "INSERT INTO modules (code, name, semester) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, module.getCode());
            stmt.setString(2, module.getName());
            stmt.setInt(3, module.getSemester());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        module.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
        return module;
    }

    @Override
    public Optional<Module> findById(Integer id) {
        String sql = "SELECT * FROM modules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractModuleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Module> findAll() {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM modules";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modules.add(extractModuleFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
        return modules;
    }

    @Override
    public Module update(Module module) {
        String sql = "UPDATE modules SET code = ?, name = ?, semester = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, module.getCode());
            stmt.setString(2, module.getName());
            stmt.setInt(3, module.getSemester());
            stmt.setInt(4, module.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
        return module;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM modules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
    }

    @Override
    public Optional<Module> findByCode(String code) {
        String sql = "SELECT * FROM modules WHERE code = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractModuleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in ModuleDaoImpl", e);
            throw new DatabaseException("Database error in ModuleDaoImpl", e);
        }
        return Optional.empty();
    }

    private Module extractModuleFromResultSet(ResultSet rs) throws SQLException {
        Module module = new Module();
        module.setId(rs.getInt("id"));
        module.setCode(rs.getString("code"));
        module.setName(rs.getString("name"));
        module.setSemester(rs.getInt("semester"));
        return module;
    }
}
