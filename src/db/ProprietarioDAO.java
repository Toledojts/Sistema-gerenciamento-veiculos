package db;

import entidades.Proprietario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProprietarioDAO {
    public Proprietario buscarPorCPF(String cpf) {
        Proprietario proprietario = null;
        String sql = "SELECT cpf, nome FROM proprietario WHERE cpf = ?"; // Assumindo tabela proprietarios
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    proprietario = new Proprietario(rs.getString("nome"), rs.getString("cpf"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar proprietário por CPF: " + e.getMessage());
            e.printStackTrace();
        }
        return proprietario;
    }

    public boolean salvar(Proprietario proprietario) {
        String sql = "INSERT INTO proprietario (cpf, nome) VALUES (?, ?)"; // SQL está correto (cpf primeiro, nome depois)
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // CORREÇÃO AQUI: O índice 1 deve ser o CPF, o índice 2 deve ser o Nome
            pstmt.setString(1, proprietario.getCpf());   // Índice 1 recebe o CPF
            pstmt.setString(2, proprietario.getNome());  // Índice 2 recebe o Nome
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar proprietário: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
