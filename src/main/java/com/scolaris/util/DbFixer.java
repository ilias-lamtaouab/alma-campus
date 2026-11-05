package com.scolaris.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbFixer {

    private static final Logger logger = LoggerFactory.getLogger(DbFixer.class);

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Update semester to 3
            int updated = stmt.executeUpdate("UPDATE modules SET semester = 3");
            System.out.println("Updated " + updated + " modules to semester 3.");
            
            // 2. Check and fix module_resources
            ResultSet rs = stmt.executeQuery("SELECT id, file_path FROM module_resources");
            while (rs.next()) {
                int id = rs.getInt("id");
                String path = rs.getString("file_path");
                System.out.println("Resource " + id + " path: " + path);
                
                if (path != null && path.contains("app java esma")) {
                    // Try to fix path if it was moved
                    String currentDir = System.getProperty("user.dir");
                    // Assuming the old path had 'resources/modules'
                    int idx = path.indexOf("resources" + java.io.File.separator + "modules");
                    if (idx == -1) idx = path.indexOf("resources/modules");
                    if (idx == -1) idx = path.indexOf("resources\\modules");
                    
                    if (idx != -1) {
                        String relative = path.substring(idx);
                        String newPath = new java.io.File(currentDir, relative).getAbsolutePath();
                        if (!path.equals(newPath)) {
                            System.out.println("Updating to: " + newPath);
                            Statement updateStmt = conn.createStatement();
                            updateStmt.executeUpdate("UPDATE module_resources SET file_path = '" + newPath.replace("\\", "\\\\") + "' WHERE id = " + id);
                            updateStmt.close();
                        }
                    }
                }
            }
            rs.close();
            System.out.println("Database fix completed.");
        } catch (Exception e) {
            logger.error("Error occurred in DbFixer", e);
        }
    }
}
