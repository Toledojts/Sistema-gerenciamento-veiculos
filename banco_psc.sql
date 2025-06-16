SELECT * FROM veiculo;
SELECT * FROM marca;
SELECT * FROM modelo;
SELECT * FROM proprietario;
SELECT * FROM transferencia;
SHOW TABLES;
USE banco_psc;
DELETE FROM veiculo WHERE TRUE;
DELETE FROM proprietario WHERE TRUE;
DELETE FROM transferencia WHERE TRUE;
DELETE FROM modelo WHERE TRUE;
DELETE FROM marca WHERE TRUE;
SET SQL_SAFE_UPDATES = 0; -- Desativar modo de segurança
SET SQL_SAFE_UPDATES = 1; -- Ativar modo de segurança
DELETE FROM proprietario;
DELETE FROM veiculo WHERE placa = "EFS9C09";
DELETE FROM proprietario WHERE cpf = "33424349020";