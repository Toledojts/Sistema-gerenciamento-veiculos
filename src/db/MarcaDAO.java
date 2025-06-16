package db;

import entidades.Marca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {
    public List<Marca> listarTodas() {
        List<Marca> marcas = new ArrayList<>();
        String sql = "SELECT idMarca, nomeMarca FROM marca ORDER BY idMarca ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("[MarcaDAO] Executando consulta: " + sql);
            while (rs.next()) {
                int id = rs.getInt("idMarca");
                String nome = rs.getString("nomeMarca");

                marcas.add(new Marca(id, nome));
            }
            System.out.println("[MarcaDAO] Marcas encontradas: " + marcas.size());

        } catch (SQLException e) {
            System.err.println("Erro ao listar marcas: " + e.getMessage());
            e.printStackTrace();
        }
        return marcas;
    }

    public long contar(){
        long count = 0;
        String sql = "SELECT COUNT(*) FROM marca";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - MarcaDAO.contar] Falha ao contar marcas: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
}
