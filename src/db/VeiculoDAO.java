package db;

import entidades.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VeiculoDAO {

    public boolean salvar(Veiculo veiculo) {
        // Assumindo tabela veiculos com colunas: placa, id_marca, id_modelo, ano, cor, cpf_proprietario
        String sql = "INSERT INTO veiculo (placa, idMarca, idModelo, ano, cor, proprietarioAtualCpf) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, veiculo.getPlaca());
            pstmt.setInt(2, veiculo.getMarca().getId());
            pstmt.setInt(3, veiculo.getModelo().getId());
            pstmt.setInt(4, veiculo.getAno());
            pstmt.setString(5, veiculo.getCor());
            pstmt.setString(6, veiculo.getProprietarioAtual().getCpf());

            pstmt.executeUpdate();
            System.out.println("[VeiculoDAO] Veículo salvo com placa: " + veiculo.getPlaca());
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao salvar veículo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
