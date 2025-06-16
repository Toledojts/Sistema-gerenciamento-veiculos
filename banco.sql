-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: banco_psc
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `marca`
--

DROP TABLE IF EXISTS `marca`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marca` (
  `idMarca` int NOT NULL AUTO_INCREMENT,
  `nomeMarca` varchar(25) NOT NULL,
  PRIMARY KEY (`idMarca`),
  UNIQUE KEY `nomeMarca` (`nomeMarca`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `modelo`
--

DROP TABLE IF EXISTS `modelo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modelo` (
  `idModelo` int NOT NULL AUTO_INCREMENT,
  `nomeModelo` varchar(100) NOT NULL,
  `idMarca` int DEFAULT NULL,
  PRIMARY KEY (`idModelo`),
  KEY `fk_Modelo_Marca` (`idMarca`),
  CONSTRAINT `fk_Modelo_Marca` FOREIGN KEY (`idMarca`) REFERENCES `marca` (`idMarca`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proprietario`
--

DROP TABLE IF EXISTS `proprietario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proprietario` (
  `cpf` varchar(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  PRIMARY KEY (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transferencia`
--

DROP TABLE IF EXISTS `transferencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transferencia` (
  `idTransferencia` int NOT NULL AUTO_INCREMENT,
  `dataTransferencia` date NOT NULL,
  `veiculoPlaca` varchar(8) NOT NULL,
  `proprietarioAnteriorCpf` varchar(11) DEFAULT NULL,
  `novoProprietarioCpf` varchar(11) NOT NULL,
  PRIMARY KEY (`idTransferencia`),
  KEY `fk_Transferencia_Veiculo` (`veiculoPlaca`),
  KEY `fk_Transferencia_ProprietarioAnterior` (`proprietarioAnteriorCpf`),
  KEY `fk_Transferencia_NovoProprietario` (`novoProprietarioCpf`),
  CONSTRAINT `fk_Transferencia_NovoProprietario` FOREIGN KEY (`novoProprietarioCpf`) REFERENCES `proprietario` (`cpf`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_Transferencia_ProprietarioAnterior` FOREIGN KEY (`proprietarioAnteriorCpf`) REFERENCES `proprietario` (`cpf`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_Transferencia_Veiculo` FOREIGN KEY (`veiculoPlaca`) REFERENCES `veiculo` (`placa`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `veiculo`
--

DROP TABLE IF EXISTS `veiculo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `veiculo` (
  `placa` varchar(8) NOT NULL,
  `ano` year DEFAULT NULL,
  `cor` varchar(25) DEFAULT NULL,
  `proprietarioAtualCpf` varchar(11) DEFAULT NULL,
  `idMarca` int DEFAULT NULL,
  `idModelo` int DEFAULT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'ATIVO',
  PRIMARY KEY (`placa`),
  KEY `fk_Veiculo_ProprietarioAtual` (`proprietarioAtualCpf`),
  KEY `fk_Veiculo_Marca` (`idMarca`),
  KEY `fk_Veiculo_Modelo` (`idModelo`),
  CONSTRAINT `fk_Veiculo_Marca` FOREIGN KEY (`idMarca`) REFERENCES `marca` (`idMarca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_Veiculo_Modelo` FOREIGN KEY (`idModelo`) REFERENCES `modelo` (`idModelo`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_Veiculo_ProprietarioAtual` FOREIGN KEY (`proprietarioAtualCpf`) REFERENCES `proprietario` (`cpf`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-15 22:51:37
