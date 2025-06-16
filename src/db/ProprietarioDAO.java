package db;

import entidades.Proprietario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProprietarioDAO {
    public Proprietario buscarPorCPF(String cpf) {
        Proprietario proprietario = null;
        String sql = "SELECT cpf, nome FROM proprietario WHERE cpf = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    proprietario = new Proprietario(rs.getString("nome"), rs.getString("cpf"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - ProprietarioDAO.buscarPorCPF] Falha ao buscar proprietário com CPF '" + cpf + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return proprietario;
    }

    public boolean salvar(Proprietario proprietario) {
        String sql = "INSERT INTO proprietario (cpf, nome) VALUES (?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proprietario.getCpf());
            pstmt.setString(2, proprietario.getNome());
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - ProprietarioDAO.salvar] Falha ao salvar proprietário com CPF '" + (proprietario != null ? proprietario.getCpf() : "N/A") + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
