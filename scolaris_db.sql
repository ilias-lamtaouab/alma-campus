-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: scolaris_db
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `certificate_requests`
--

DROP TABLE IF EXISTS `certificate_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `certificate_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `request_date` date NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  `approved_date` date DEFAULT NULL,
  `document_type` varchar(100) DEFAULT 'Attestation de scolarité',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `certificate_requests_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `certificate_requests`
--

LOCK TABLES `certificate_requests` WRITE;
/*!40000 ALTER TABLE `certificate_requests` DISABLE KEYS */;
INSERT INTO `certificate_requests` VALUES (1,4,'2026-06-21','APPROVED','2026-06-21','Attestation de scolarité'),(2,4,'2026-06-21','APPROVED','2026-06-21','Attestation de scolarité'),(3,4,'2026-06-21','REJECTED',NULL,'Attestation de scolarité');
/*!40000 ALTER TABLE `certificate_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grades` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `subject_id` int(11) NOT NULL,
  `grade_value` decimal(5,2) NOT NULL CHECK (`grade_value` >= 0 and `grade_value` <= 20),
  `evaluation_type` enum('CC','EXAM','PROJECT') NOT NULL,
  `evaluation_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_student_subject_eval` (`student_id`,`subject_id`,`evaluation_type`),
  KEY `subject_id` (`subject_id`),
  CONSTRAINT `grades_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE,
  CONSTRAINT `grades_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (21,1,4,14.50,'CC','2026-06-16'),(22,1,4,16.00,'EXAM','2026-06-21'),(23,1,5,12.00,'CC','2026-06-17'),(24,1,5,14.50,'EXAM','2026-06-21'),(25,1,6,8.50,'CC','2026-06-18'),(26,1,6,11.00,'EXAM','2026-06-21'),(27,1,8,13.00,'CC','2026-06-15'),(28,1,8,15.00,'EXAM','2026-06-20'),(29,1,9,10.00,'CC','2026-06-14'),(30,1,9,12.50,'EXAM','2026-06-21'),(31,1,11,9.00,'CC','2026-06-16'),(32,1,11,12.00,'EXAM','2026-06-21'),(33,1,12,11.00,'CC','2026-06-16'),(34,1,12,13.00,'EXAM','2026-06-21'),(35,1,13,14.00,'CC','2026-06-18'),(36,1,13,15.00,'EXAM','2026-06-21'),(37,1,14,12.00,'CC','2026-06-19'),(38,1,14,13.50,'EXAM','2026-06-21'),(39,2,4,18.00,'CC','2026-06-16'),(40,2,4,19.00,'EXAM','2026-06-21'),(41,2,5,15.00,'CC','2026-06-17'),(42,2,5,16.00,'EXAM','2026-06-21'),(43,2,6,14.00,'CC','2026-06-18'),(44,2,6,15.50,'EXAM','2026-06-21'),(45,2,8,16.00,'CC','2026-06-15'),(46,2,8,18.50,'EXAM','2026-06-20'),(47,2,9,14.00,'CC','2026-06-14'),(48,2,9,15.00,'EXAM','2026-06-21'),(49,2,11,13.00,'CC','2026-06-16'),(50,2,11,15.00,'EXAM','2026-06-21'),(51,2,12,17.00,'CC','2026-06-16'),(52,2,12,16.50,'EXAM','2026-06-21'),(53,2,13,16.00,'CC','2026-06-18'),(54,2,13,17.50,'EXAM','2026-06-21'),(55,2,14,15.00,'CC','2026-06-19'),(56,2,14,16.00,'EXAM','2026-06-21');
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module_resources`
--

DROP TABLE IF EXISTS `module_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `file_name` varchar(255) DEFAULT '',
  `file_path` varchar(500) DEFAULT '',
  `file_size` bigint(20) DEFAULT 0,
  `uploaded_by` varchar(50) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `module_id` (`module_id`),
  CONSTRAINT `module_resources_ibfk_1` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module_resources`
--

LOCK TABLES `module_resources` WRITE;
/*!40000 ALTER TABLE `module_resources` DISABLE KEYS */;
INSERT INTO `module_resources` VALUES (2,4,'EXAMIN PHP','9RAW MZN','Bulletin_rami_esma.pdf','C:\\Users\\HAMZA\\Desktop\\app java esma\\resources\\modules\\4\\1782005685235_Bulletin_rami_esma.pdf',984792,'teacher2','2026-06-21 00:34:45');
/*!40000 ALTER TABLE `module_resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modules` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `name` varchar(150) NOT NULL,
  `semester` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modules`
--

LOCK TABLES `modules` WRITE;
/*!40000 ALTER TABLE `modules` DISABLE KEYS */;
INSERT INTO `modules` VALUES (3,'M1','JAVA',3),(4,'M2','PHP',3),(5,'M3','Programmation des Cartes Intelligentes',3),(6,'M4','Programmation Réseaux sous Python',3),(7,'M5','Intelligence Artificielle',3),(8,'M6','Langage SQL - Req Avancées',3),(9,'M7','SGBDR Oracle Ini',3),(10,'M8','Statistiques',3),(11,'M9','Admin Avancée & Sécurité Linux',3),(12,'M10','ERP et Logistique Français',3),(13,'M11','Innovation Mgt et Tec SI',3);
/*!40000 ALTER TABLE `modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `students` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cne` varchar(20) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `date_of_birth` date NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `enrollment_date` date NOT NULL,
  `status` enum('ACTIVE','SUSPENDED','GRADUATED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cne` (`cne`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (1,'AA8748','hamza','ouala','2026-07-15','hamza.ouala@example.com','0600000001','2026-06-20','ACTIVE'),(2,'EE1234','esma','rami','2005-01-20','esma@scolaris.ma','0633334444','2026-06-20','ACTIVE'),(3,'hamzaolix444','hamido','naim','2004-11-02','hamido@scolaris.ma','0644445558','2026-06-20','ACTIVE'),(4,'student1','student1','Étudiant','2004-05-12','student1@scolaris.ma','0611223344','2026-06-21','ACTIVE'),(5,'AAFGJ','oualid','modo','2000-06-17','oualid@example.com','0600000002','2026-06-28','ACTIVE');
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subjects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `coefficient` decimal(4,2) NOT NULL DEFAULT 1.00,
  `module_id` int(11) DEFAULT NULL,
  `teacher_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `module_id` (`module_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `subjects_ibfk_1` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `subjects_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
INSERT INTO `subjects` VALUES (4,'JAVA',3.00,3,1),(5,'PHP',2.00,4,4),(6,'Programmation des Cartes Intelligentes',2.00,5,1),(7,'Programmation Réseaux sous Python',2.00,6,2),(8,'Intelligence Artificielle',2.00,7,3),(9,'Langage SQL - Req Avancées',2.00,8,3),(10,'SGBDR Oracle Ini',2.00,9,2),(11,'Statistiques',1.50,10,5),(12,'Admin Avancée & Sécurité Linux',2.00,11,1),(13,'ERP et Logistique Français',2.00,12,2),(14,'Innovation Mgt et Tec SI',1.50,13,3);
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_notifications`
--

DROP TABLE IF EXISTS `system_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `system_notifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` varchar(50) DEFAULT 'ADMIN',
  `target_type` varchar(50) NOT NULL,
  `target_username` varchar(50) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_notifications`
--

LOCK TABLES `system_notifications` WRITE;
/*!40000 ALTER TABLE `system_notifications` DISABLE KEYS */;
INSERT INTO `system_notifications` VALUES (1,'ADMIN','SPECIFIC_STUDENT','student1','Demande de certificat approuvée','Votre demande de certificat de scolarité a été approuvée. Vous pouvez maintenant télécharger votre certificat.','2026-06-20 23:16:12'),(2,'ADMIN','SPECIFIC_STUDENT','student1','Demande de document refusée','Votre demande de document a été refusée. Veuillez contacter l\'administration pour plus d\'informations.','2026-06-28 13:50:42');
/*!40000 ALTER TABLE `system_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teachers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cin` varchar(20) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `specialty` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cin` (`cin`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teachers`
--

LOCK TABLES `teachers` WRITE;
/*!40000 ALTER TABLE `teachers` DISABLE KEYS */;
INSERT INTO `teachers` VALUES (1,'AB1234','Ahmed','Alami','alami@scolaris.ma','0612345678','Java & Spring'),(2,'CD5678','Salma','Tazi','tazi@scolaris.ma','0687654321','Web & Mobile'),(3,'EF9012','Khalid','Naciri','naciri@scolaris.ma','0655554433','Maths & Algèbre'),(4,'teacher2','teacher2','teacher2','teacher2@scolaris.ma','','PHP'),(5,'teacher3','teacher3','teacher3','teacher3@scolaris.ma','','Statistiques');
/*!40000 ALTER TABLE `teachers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','TEACHER','STUDENT') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('ACTIVE','BLOCKED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'admin','$2a$10$OKRiICrOPFDW8yFdbZ.2kemWj.WpXWAfu41sjhFw1LTmylA7ZA8du','ADMIN','2026-06-20 01:31:57','ACTIVE'),(3,'hamzaolix44','$2a$10$OKRiICrOPFDW8yFdbZ.2kemWj.WpXWAfu41sjhFw1LTmylA7ZA8du','ADMIN','2026-06-20 02:26:53','ACTIVE'),(4,'AB1234','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-20 21:01:17','ACTIVE'),(5,'CD5678','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-20 21:01:17','ACTIVE'),(6,'EE1234','$2a$10$tdRX7FOjlj8Yz0Ycdqfpd.U1REh.Ye4t3.fgHnVpn7qf.UETeuWfm','STUDENT','2026-06-20 21:01:17','ACTIVE'),(7,'SS5678','$2a$10$tdRX7FOjlj8Yz0Ycdqfpd.U1REh.Ye4t3.fgHnVpn7qf.UETeuWfm','STUDENT','2026-06-20 21:01:17','ACTIVE'),(8,'student1','$2a$10$tdRX7FOjlj8Yz0Ycdqfpd.U1REh.Ye4t3.fgHnVpn7qf.UETeuWfm','STUDENT','2026-06-20 23:21:40','ACTIVE'),(9,'teacher1','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-21 00:28:43','ACTIVE'),(10,'EF9012','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-21 00:32:33','ACTIVE'),(11,'AA8748','$2a$10$tdRX7FOjlj8Yz0Ycdqfpd.U1REh.Ye4t3.fgHnVpn7qf.UETeuWfm','STUDENT','2026-06-21 00:32:33','ACTIVE'),(12,'teacher2','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-21 01:33:48','ACTIVE'),(13,'teacher3','$2a$10$FsWlEsDjWXLDXkB6g.Id0.DkjxhLenzxvoY3IoZ0Iqqgz637daoPm','TEACHER','2026-06-28 21:56:06','ACTIVE');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-02  0:24:44
