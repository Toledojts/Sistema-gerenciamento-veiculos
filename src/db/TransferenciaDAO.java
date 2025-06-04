package db;

import entidades.Transferencia;

import java.sql.*;

public class TransferenciaDAO {
    public boolean salvar(Transferencia transferencia) {
        // SQL CORRETO para as colunas da sua tabela 'transferencia'
        String sql = "INSERT INTO transferencia (dataTransferencia, veiculoPlaca, proprietarioAnteriorCpf, novoProprietarioCpf) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(transferencia.getDataTransferencia()));
            pstmt.setString(2, transferencia.getVeiculo().getPlaca()); // Placa do veículo (já pode ser a Mercosul)

            // CPF do Proprietário Anterior
            if (transferencia.getAntigoProprietario() != null) {
                pstmt.setString(3, transferencia.getAntigoProprietario().getCpf());
            } else {
                // Se o proprietário anterior puder ser nulo no seu modelo de negócio
                // (ex: primeiro emplacamento direto com transferência, raro, ou se o campo permite NULL)
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }
            // CPF do Novo Proprietário
            pstmt.setString(4, transferencia.getNovoProprietario().getCpf());

            int linhasAfetadas = pstmt.executeUpdate(); // Esta é a linha que provavelmente está dando erro (linha 23 no seu caso)
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            // Seria bom que esta mensagem de erro fosse específica para transferência
            System.err.println("Erro SQL ao salvar REGISTRO DE TRANSFERÊNCIA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
