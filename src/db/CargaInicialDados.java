package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CargaInicialDados {

    public static void popularBanco(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("[Carga Inicial] Populando o banco de dados com dados de exemplo...");

            // Desabilitar auto-commit para tratar como uma única transação
            conn.setAutoCommit(false);

            //  INSERIR MARCAS (sem dependências)
            stmt.addBatch("INSERT INTO marca (idMarca, nomeMarca) VALUES (1, 'Chevrolet'), (2, 'Volkswagen'), (3, 'Fiat');");
            System.out.println("[Carga Inicial] Inserindo marcas...");

            // INSERIR MODELOS (dependem de Marca)
            stmt.addBatch(
                    "INSERT INTO modelo (idModelo, nomeModelo, idMarca) " +
                            "VALUES (1, 'Onix', 1), (2, 'Celta', 1), (3, 'Corsa', 1), " +
                            "(4, 'Gol', 2), (5, 'Saveiro', 2), (6, 'Polo', 2), " +
                            "(7, 'Uno', 3), (8, 'Mobi', 3), (9, 'Argo', 3);");

            System.out.println("[Carga Inicial] Inserindo modelos...");

            // INSERIR PROPRIETÁRIOS (sem dependências)
            stmt.addBatch("INSERT INTO proprietario (cpf, nome) VALUES ('27665173080', 'Ana Silva'), ('12453567047', 'Carlos Souza');");
            System.out.println("[Carga Inicial] Inserindo proprietários...");

            // INSERIR VEÍCULOS (dependem de Marca, Modelo, Proprietario)
            //Coluna status tem ATIVO como padrao - não é necessário inserir status
            stmt.addBatch(
                    "INSERT INTO veiculo (placa, ano, cor, proprietarioAtualCpf, IdMarca, IdModelo) " +
                            "VALUES " + "('ABC-1234', 2020, 'Branco', '27665173080', 1, 2);");
            stmt.addBatch(
                    "INSERT INTO veiculo (placa, ano, cor, proprietarioAtualCpf, IdMarca, IdModelo) " +
                            "VALUES " + "('XYZ-5678', 2021, 'Preto', '12453567047', 2, 3);");

            stmt.addBatch("INSERT INTO veiculo (placa, ano, cor, proprietarioAtualCpf, IdMarca, IdModelo) " +
                    "VALUES ('QWE-9101', 2022, 'Prata', '27665173080', 3, 5);");

            System.out.println("[Carga Inicial] Inserindo veículos...");

            // Executa todos os comandos em lote
            stmt.executeBatch();

            // Se tudo deu certo, comitar a transação
            conn.commit();
            System.out.println("[Carga Inicial] Dados inseridos com sucesso!");

        } catch (SQLException e) {
            System.err.println("[Carga Inicial] ERRO ao popular o banco de dados: " + e.getMessage());
            e.printStackTrace();
            try {
                // Em caso de erro, reverter a transação
                System.err.println("[Carga Inicial] Revertendo transação...");
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("[Carga Inicial] ERRO ao reverter a transação: " + ex.getMessage());
            }
        } finally {
            try {
                // Reabilitar o auto-commit para o resto da aplicação
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
