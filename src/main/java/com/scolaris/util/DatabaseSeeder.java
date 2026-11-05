package com.scolaris.util;

import com.scolaris.dao.*;
import com.scolaris.dao.impl.*;
import com.scolaris.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.List;

public class DatabaseSeeder {

    public static void migrateDatabase() {
        try (java.sql.Connection conn = com.scolaris.dao.db.DatabaseConnection.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // Add status column to users if not exists
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN status ENUM('ACTIVE', 'BLOCKED') DEFAULT 'ACTIVE'");
                System.out.println("[Migration] Added 'status' column to 'users' table.");
            } catch (java.sql.SQLException e) {
                // Column probably already exists, ignore
            }

            // Modify role column in users to support STUDENT
            try {
                stmt.executeUpdate("ALTER TABLE users MODIFY COLUMN role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL");
                System.out.println("[Migration] Updated 'role' column in 'users' table to support STUDENT.");
            } catch (java.sql.SQLException e) {
                System.err.println("[Migration] Failed to modify role column: " + e.getMessage());
            }

            // Create certificate_requests table if not exists
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS certificate_requests (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "student_id INT NOT NULL, " +
                        "request_date DATE NOT NULL, " +
                        "status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING', " +
                        "document_type VARCHAR(100) DEFAULT 'Attestation de scolarité', " +
                        "approved_date DATE DEFAULT NULL, " +
                        "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE" +
                        ")");
                System.out.println("[Migration] Created 'certificate_requests' table.");
            } catch (java.sql.SQLException e) {
                System.err.println("[Migration] Failed to create 'certificate_requests' table: " + e.getMessage());
            }

            // Add document_type column to certificate_requests if not exists (for existing tables)
            try {
                stmt.executeUpdate("ALTER TABLE certificate_requests ADD COLUMN document_type VARCHAR(100) DEFAULT 'Attestation de scolarité'");
                System.out.println("[Migration] Added 'document_type' column to 'certificate_requests' table.");
            } catch (java.sql.SQLException e) {
                // Column probably already exists, ignore
            }

            // Create system_notifications table if not exists
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS system_notifications (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "sender VARCHAR(50) DEFAULT 'ADMIN', " +
                        "target_type VARCHAR(50) NOT NULL, " +
                        "target_username VARCHAR(50) DEFAULT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");
                System.out.println("[Migration] Created 'system_notifications' table.");
            } catch (java.sql.SQLException e) {
                System.err.println("[Migration] Failed to create 'system_notifications' table: " + e.getMessage());
            }
            
            // Create module_resources table if not exists
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS module_resources (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "module_id INT NOT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "file_name VARCHAR(255) DEFAULT '', " +
                        "file_path VARCHAR(500) DEFAULT '', " +
                        "file_size BIGINT DEFAULT 0, " +
                        "uploaded_by VARCHAR(50), " +
                        "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE" +
                        ")");
                System.out.println("[Migration] Created 'module_resources' table.");
            } catch (java.sql.SQLException e) {
                System.err.println("[Migration] Failed to create 'module_resources' table: " + e.getMessage());
            }

            // Curriculum refresh: if subjects count != 11, rebuild everything with 1-module-per-subject
            try {
                java.sql.Statement countStmt = conn.createStatement();
                java.sql.ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM subjects");
                if (rs.next()) {
                    int subjectCount = rs.getInt(1);
                    if (subjectCount != 0 && subjectCount != 11) {
                        stmt.executeUpdate("DELETE FROM grades");
                        stmt.executeUpdate("DELETE FROM module_resources");
                        stmt.executeUpdate("DELETE FROM subjects");
                        stmt.executeUpdate("DELETE FROM modules");
                        System.out.println("[Migration] Cleared old data for curriculum refresh (11 modules).");
                    }
                }
                rs.close();
                countStmt.close();
            } catch (java.sql.SQLException e) {
                System.err.println("[Migration] Curriculum refresh failed: " + e.getMessage());
            }

            // Update all existing modules to semester 3
            try {
                stmt.executeUpdate("UPDATE modules SET semester = 3");
            } catch (java.sql.SQLException e) {
                // Ignore
            }

        } catch (java.sql.SQLException e) {
            System.err.println("[Migration] Database connection failed for migration: " + e.getMessage());
        }
    }

    public static void seedIfEmpty() {
        // Run database migration first to ensure schema has new columns
        migrateDatabase();

        StudentDao studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        UserDao userDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.UserDao.class);
        TeacherDao teacherDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.TeacherDao.class);
        ModuleDao moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        SubjectDao subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        GradeDao gradeDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.GradeDao.class);

        System.out.println("[Seeder] Checking database for missing sample data...");

        // 1. Seed Users (ensure admin and teachers and student users exist)
        if (userDao.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt(10)));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userDao.save(admin);
            System.out.println("[Seeder] Seeded default 'admin' user.");
        }

        // 2. Seed Teachers
        if (teacherDao.findAll().isEmpty()) {
            Teacher t1 = new Teacher(0, "AB1234", "Ahmed", "Alami", "alami@scolaris.ma", "0612345678", "Java & Spring");
            Teacher t2 = new Teacher(0, "CD5678", "Salma", "Tazi", "tazi@scolaris.ma", "0687654321", "Web & Mobile");
            Teacher t3 = new Teacher(0, "EF9012", "Khalid", "Naciri", "naciri@scolaris.ma", "0655554433", "Maths & Algèbre");
            teacherDao.save(t1);
            teacherDao.save(t2);
            teacherDao.save(t3);
            System.out.println("[Seeder] Seeded default teachers.");
        }

        // Ensure teacher user accounts exist (password = teacher123)
        String[][] teacherAccounts = {{"AB1234", "teacher123"}, {"CD5678", "teacher123"}, {"EF9012", "teacher123"}};
        for (String[] acc : teacherAccounts) {
            if (userDao.findByUsername(acc[0]).isEmpty()) {
                userDao.save(new User(0, acc[0], BCrypt.hashpw(acc[1], BCrypt.gensalt(10)), "TEACHER", "ACTIVE", null));
                System.out.println("[Seeder] Created teacher account: " + acc[0]);
            }
        }

        // 3. Seed Modules (11 modules, one per subject)
        if (moduleDao.findAll().isEmpty()) {
            moduleDao.save(new com.scolaris.model.Module(0, "M1", "JAVA", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M2", "PHP", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M3", "Programmation des Cartes Intelligentes", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M4", "Programmation Réseaux sous Python", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M5", "Intelligence Artificielle", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M6", "Langage SQL - Req Avancées", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M7", "SGBDR Oracle Ini", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M8", "Statistiques", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M9", "Admin Avancée & Sécurité Linux", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M10", "ERP et Logistique Français", 1));
            moduleDao.save(new com.scolaris.model.Module(0, "M11", "Innovation Mgt et Tec SI", 1));
            System.out.println("[Seeder] Seeded 11 modules (M1-M11, one per subject).");
        }

        // 4. Seed Subjects (11 subjects, one per module)
        if (subjectDao.findAll().isEmpty()) {
            List<com.scolaris.model.Module> mods = moduleDao.findAll();
            List<Teacher> teachers = teacherDao.findAll();
            if (mods.size() >= 11 && teachers.size() >= 3) {
                // M1 → JAVA → teacher AB1234 (teachers.get(0))
                subjectDao.save(new Subject(0, "JAVA", 3.0, mods.get(0).getId(), teachers.get(0).getId()));
                // M2 → PHP → teacher CD5678 (teachers.get(1))
                subjectDao.save(new Subject(0, "PHP", 2.0, mods.get(1).getId(), teachers.get(1).getId()));
                // M3 → Prog Cartes Intelligentes → teacher AB1234
                subjectDao.save(new Subject(0, "Programmation des Cartes Intelligentes", 2.0, mods.get(2).getId(), teachers.get(0).getId()));
                // M4 → Prog Réseaux Python → teacher CD5678
                subjectDao.save(new Subject(0, "Programmation Réseaux sous Python", 2.0, mods.get(3).getId(), teachers.get(1).getId()));
                // M5 → IA → teacher EF9012 (teachers.get(2))
                subjectDao.save(new Subject(0, "Intelligence Artificielle", 2.0, mods.get(4).getId(), teachers.get(2).getId()));
                // M6 → SQL → teacher EF9012
                subjectDao.save(new Subject(0, "Langage SQL - Req Avancées", 2.0, mods.get(5).getId(), teachers.get(2).getId()));
                // M7 → SGBDR Oracle → teacher CD5678
                subjectDao.save(new Subject(0, "SGBDR Oracle Ini", 2.0, mods.get(6).getId(), teachers.get(1).getId()));
                // M8 → Statistiques → teacher EF9012
                subjectDao.save(new Subject(0, "Statistiques", 1.5, mods.get(7).getId(), teachers.get(2).getId()));
                // M9 → Admin Linux → teacher AB1234
                subjectDao.save(new Subject(0, "Admin Avancée & Sécurité Linux", 2.0, mods.get(8).getId(), teachers.get(0).getId()));
                // M10 → ERP → teacher CD5678
                subjectDao.save(new Subject(0, "ERP et Logistique Français", 2.0, mods.get(9).getId(), teachers.get(1).getId()));
                // M11 → Innovation → teacher EF9012
                subjectDao.save(new Subject(0, "Innovation Mgt et Tec SI", 1.5, mods.get(10).getId(), teachers.get(2).getId()));
                System.out.println("[Seeder] Seeded 11 subjects (one per module).");
            } else {
                System.err.println("[Seeder] Cannot seed subjects: need 11 modules, got " + mods.size());
            }
        }

        // 5. Seed Students
        // If there's 1 or less students, seed the default students
        if (studentDao.findAll().size() <= 1) {
            Student st1 = new Student(0, "AA8748", "hamza", "ouala", LocalDate.of(2004, 5, 15), "hamza@scolaris.ma", "0622223333", LocalDate.now(), "ACTIVE");
            Student st2 = new Student(0, "EE1234", "esma", "rami", LocalDate.of(2005, 1, 20), "esma@scolaris.ma", "0633334444", LocalDate.now(), "ACTIVE");
            Student st3 = new Student(0, "SS5678", "sofia", "benani", LocalDate.of(2004, 11, 10), "sofia@scolaris.ma", "0644445555", LocalDate.now(), "ACTIVE");
            
            if (studentDao.findByCne("AA8748").isEmpty()) {
                studentDao.save(st1);
            }
            if (studentDao.findByCne("EE1234").isEmpty()) {
                studentDao.save(st2);
            }
            if (studentDao.findByCne("SS5678").isEmpty()) {
                studentDao.save(st3);
            }
            System.out.println("[Seeder] Seeded default students.");
        }

        // Ensure student user accounts exist (password = student123)
        String[][] studentAccounts = {{"AA8748", "student123"}, {"EE1234", "student123"}, {"SS5678", "student123"}};
        for (String[] acc : studentAccounts) {
            if (userDao.findByUsername(acc[0]).isEmpty()) {
                userDao.save(new User(0, acc[0], BCrypt.hashpw(acc[1], BCrypt.gensalt(10)), "STUDENT", "ACTIVE", null));
                System.out.println("[Seeder] Created student account: " + acc[0]);
            }
        }

        // 6. Seed Grades (across all 11 subjects)
        if (gradeDao.findAll().isEmpty()) {
            List<Student> allSt = studentDao.findAll();
            List<Subject> allSub = subjectDao.findAll();
            if (allSt.size() >= 2 && allSub.size() >= 11) {
                Student hamza = null;
                Student esma = null;
                for (Student s : allSt) {
                    if ("AA8748".equals(s.getCne())) hamza = s;
                    if ("EE1234".equals(s.getCne())) esma = s;
                }
                
                if (hamza != null) {
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(0).getId(), 14.5, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(0).getId(), 16.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(1).getId(), 12.0, "CC", LocalDate.now().minusDays(4)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(1).getId(), 14.5, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(2).getId(), 8.5, "CC", LocalDate.now().minusDays(3)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(2).getId(), 11.0, "EXAM", LocalDate.now()));

                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(4).getId(), 13.0, "CC", LocalDate.now().minusDays(6)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(4).getId(), 15.0, "EXAM", LocalDate.now().minusDays(1)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(5).getId(), 10.0, "CC", LocalDate.now().minusDays(7)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(5).getId(), 12.5, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(7).getId(), 9.0, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(7).getId(), 12.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(8).getId(), 11.0, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(8).getId(), 13.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(9).getId(), 14.0, "CC", LocalDate.now().minusDays(3)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(9).getId(), 15.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(10).getId(), 12.0, "CC", LocalDate.now().minusDays(2)));
                    gradeDao.save(new Grade(0, hamza.getId(), allSub.get(10).getId(), 13.5, "EXAM", LocalDate.now()));
                }
                
                if (esma != null) {
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(0).getId(), 18.0, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(0).getId(), 19.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(1).getId(), 15.0, "CC", LocalDate.now().minusDays(4)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(1).getId(), 16.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(2).getId(), 14.0, "CC", LocalDate.now().minusDays(3)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(2).getId(), 15.5, "EXAM", LocalDate.now()));

                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(4).getId(), 16.0, "CC", LocalDate.now().minusDays(6)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(4).getId(), 18.5, "EXAM", LocalDate.now().minusDays(1)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(5).getId(), 14.0, "CC", LocalDate.now().minusDays(7)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(5).getId(), 15.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(7).getId(), 13.0, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(7).getId(), 15.0, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(8).getId(), 17.0, "CC", LocalDate.now().minusDays(5)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(8).getId(), 16.5, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(9).getId(), 16.0, "CC", LocalDate.now().minusDays(3)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(9).getId(), 17.5, "EXAM", LocalDate.now()));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(10).getId(), 15.0, "CC", LocalDate.now().minusDays(2)));
                    gradeDao.save(new Grade(0, esma.getId(), allSub.get(10).getId(), 16.0, "EXAM", LocalDate.now()));
                }
                System.out.println("[Seeder] Seeded grades across all 11 subjects.");
            }
        }
        System.out.println("[Seeder] Check complete. Database is populated.");
    }
}
