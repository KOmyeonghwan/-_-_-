-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: multi
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `board_developer`
--

DROP TABLE IF EXISTS `board_developer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_developer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_developer_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_developer`
--

LOCK TABLES `board_developer` WRITE;
/*!40000 ALTER TABLE `board_developer` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_developer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_employment`
--

DROP TABLE IF EXISTS `board_employment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_employment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_employment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_employment`
--

LOCK TABLES `board_employment` WRITE;
/*!40000 ALTER TABLE `board_employment` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_employment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_finance`
--

DROP TABLE IF EXISTS `board_finance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_finance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_finance_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_finance`
--

LOCK TABLES `board_finance` WRITE;
/*!40000 ALTER TABLE `board_finance` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_finance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_free`
--

DROP TABLE IF EXISTS `board_free`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_free` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_free_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_free`
--

LOCK TABLES `board_free` WRITE;
/*!40000 ALTER TABLE `board_free` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_free` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_manager`
--

DROP TABLE IF EXISTS `board_manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_manager` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_code` varchar(255) NOT NULL,
  `board_name` varchar(255) NOT NULL,
  `dept_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5u2evv3m8u2oilgcvdiwpua3y` (`board_code`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_manager`
--

LOCK TABLES `board_manager` WRITE;
/*!40000 ALTER TABLE `board_manager` DISABLE KEYS */;
INSERT INTO `board_manager` VALUES (36,'developer','개발부','developer'),(37,'notice','공지사항',NULL),(38,'free','자유게시판',NULL),(39,'suggest','건의사항',NULL),(40,'employment','채용공고',NULL),(42,'finance','재정부','finance');
/*!40000 ALTER TABLE `board_manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_notice`
--

DROP TABLE IF EXISTS `board_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_notice_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_notice`
--

LOCK TABLES `board_notice` WRITE;
/*!40000 ALTER TABLE `board_notice` DISABLE KEYS */;
INSERT INTO `board_notice` VALUES (1,'금일 정오 단체 회의 위치 변경  안내','<p>금일 단체 회의 A룸에서 C룸으로 변동 되었습니다.</p>\r\n','관리자','2025-05-26 17:19:36','admin',1),(2,'채용공고','<p>전 부서별로 채용공고 시작합니다.</p>\r\n\r\n<p>기간:2025.03.21~2025.04.21</p>\r\n\r\n<p>자세한 사항은 채용공고 페이지 확인바랍니다.</p>\r\n','관리자','2025-05-26 17:20:43','admin',2),(3,'이번주 일정 및 사내 여행 투표 실시','<p>이번주는 영양사분께서 휴가로 인해 사내 급식은 없습니다.</p>\r\n\r\n<p>사내 여행지 투표 진행중이오니 많은 참여 부탁드립니다.</p>\r\n','관리자','2025-05-27 09:27:01','admin',1),(4,'금일 주차장 긴급 공지','<p>현재 우천으로 인해 주차장에 물이 차기 시작했으므로 주차하신 분들은 이동 부탁드립니다.</p>\r\n','관리자','2025-05-27 11:25:26','admin',1),(5,'차후 일정 공지','<p>내일 태풍이 근접 상태이기 떄문에 재택 근무로 변경하곘습니다.</p>\r\n','관리자','2025-05-27 11:34:10','admin',5);
/*!40000 ALTER TABLE `board_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_suggest`
--

DROP TABLE IF EXISTS `board_suggest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_suggest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `user_name` varchar(30) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(50) DEFAULT NULL,
  `views` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `board_suggest_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_suggest`
--

LOCK TABLES `board_suggest` WRITE;
/*!40000 ALTER TABLE `board_suggest` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_suggest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `message` text NOT NULL,
  `receiver_id` int NOT NULL,
  `sender_id` int NOT NULL,
  PRIMARY KEY (`message_id`),
  KEY `FKand7mh9iu4kt3n1tn2w9i9of0` (`receiver_id`),
  KEY `FKgiqeap8ays4lf684x7m0r2729` (`sender_id`),
  CONSTRAINT `FKand7mh9iu4kt3n1tn2w9i9of0` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`useridx`) ON DELETE CASCADE,
  CONSTRAINT `FKgiqeap8ays4lf684x7m0r2729` FOREIGN KEY (`sender_id`) REFERENCES `users` (`useridx`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_developer`
--

DROP TABLE IF EXISTS `comment_developer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_developer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_developer_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_developer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_developer`
--

LOCK TABLES `comment_developer` WRITE;
/*!40000 ALTER TABLE `comment_developer` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_developer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_employment`
--

DROP TABLE IF EXISTS `comment_employment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_employment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_employment_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_employment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_employment`
--

LOCK TABLES `comment_employment` WRITE;
/*!40000 ALTER TABLE `comment_employment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_employment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_finance`
--

DROP TABLE IF EXISTS `comment_finance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_finance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_finance_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_finance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_finance`
--

LOCK TABLES `comment_finance` WRITE;
/*!40000 ALTER TABLE `comment_finance` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_finance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_free`
--

DROP TABLE IF EXISTS `comment_free`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_free` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_free_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_free` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_free`
--

LOCK TABLES `comment_free` WRITE;
/*!40000 ALTER TABLE `comment_free` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_free` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_notice`
--

DROP TABLE IF EXISTS `comment_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_notice_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_notice` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_notice`
--

LOCK TABLES `comment_notice` WRITE;
/*!40000 ALTER TABLE `comment_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_suggest`
--

DROP TABLE IF EXISTS `comment_suggest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_suggest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `comment_suggest_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `board_suggest` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_suggest`
--

LOCK TABLES `comment_suggest` WRITE;
/*!40000 ALTER TABLE `comment_suggest` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_suggest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `dept_id` int NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) NOT NULL,
  `member_count` int DEFAULT '0',
  `created_at` date NOT NULL,
  `dept_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (27,'개발부',0,'2025-05-26','developer'),(29,'재정부',1,'2025-05-26','finance');
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notes`
--

DROP TABLE IF EXISTS `notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notes` (
  `note_id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(255) NOT NULL,
  `receiver_id` varchar(50) NOT NULL,
  `sender_id` varchar(50) NOT NULL,
  PRIMARY KEY (`note_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notes`
--

LOCK TABLES `notes` WRITE;
/*!40000 ALTER TABLE `notes` DISABLE KEYS */;
INSERT INTO `notes` VALUES (1,'zczzzcz','malkum354','admin'),(2,'sdsdsd','honk3578','malkum354');
/*!40000 ALTER TABLE `notes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `useridx` int NOT NULL AUTO_INCREMENT,
  `userid` varchar(50) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `user_email` varchar(150) DEFAULT NULL,
  `zip_code` varchar(200) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `reg_date` date DEFAULT NULL,
  `admin` int DEFAULT '0',
  `dept_id` int DEFAULT NULL,
  `reset_token` varchar(100) DEFAULT NULL,
  `reset_token_expiry` datetime DEFAULT NULL,
  PRIMARY KEY (`useridx`),
  KEY `dept_id` (`dept_id`),
  KEY `idx_userid` (`userid`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','1234','관리자','admin@example.com','12345','010-0000-0000','2025-04-25',1,NULL,NULL,NULL),(11,'honk3578','12345678','홍길동','honk3578@naver.com','경기 평택시 청북읍 판교길 9','01000000000','2025-05-26',0,29,NULL,NULL);
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

-- Dump completed on 2025-06-11  9:22:23
