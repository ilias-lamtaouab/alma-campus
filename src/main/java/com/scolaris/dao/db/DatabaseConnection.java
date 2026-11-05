package com.scolaris.dao.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.scolaris.exception.DatabaseException;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String DB_PROPERTIES_FILE = "db.properties";

    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        Properties props = loadDbProperties();
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
            
            // Optimizations for MySQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
        } catch (NumberFormatException ex) {
            logger.error("Invalid db.pool.size in " + DB_PROPERTIES_FILE, ex);
            throw new DatabaseException("Invalid db.pool.size in " + DB_PROPERTIES_FILE, ex);
        }
    }

    /**
     * Loads database settings from {@code db.properties}.
     * Lookup order: path given by the {@code db.config} system property,
     * then {@code ./db.properties} in the working directory, then the classpath.
     * This file is git-ignored; see {@code db.properties.example} for the format.
     */
    private Properties loadDbProperties() {
        Properties props = new Properties();
        Path external = Paths.get(System.getProperty("db.config", DB_PROPERTIES_FILE));
        try {
            if (Files.isRegularFile(external)) {
                try (InputStream input = Files.newInputStream(external)) {
                    props.load(input);
                }
                logger.info("Loaded database configuration from {}", external.toAbsolutePath());
            } else {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {
                    if (input == null) {
                        String msg = "Database configuration not found. Copy db.properties.example to "
                                + DB_PROPERTIES_FILE + " in the project root and set your credentials.";
                        logger.error(msg);
                        throw new DatabaseException(msg);
                    }
                    props.load(input);
                }
                logger.info("Loaded database configuration from classpath");
            }
        } catch (IOException ex) {
            logger.error("Error loading database configuration", ex);
            throw new DatabaseException("Error loading database configuration", ex);
        }
        if (props.getProperty("db.url") == null) {
            throw new DatabaseException("db.url is missing in " + DB_PROPERTIES_FILE);
        }
        return props;
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
