package db;

import entidades.Marca;
import entidades.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModeloDAO {
    // Metodo para buscar modelos de uma marca específica
    public List<Modelo> listarPorMarca(Marca marca) {
        List<Modelo> modelos = new ArrayList<>();
        String sql = "SELECT idModelo, nomeModelo FROM modelo WHERE idMarca = ? ORDER BY idModelo ASC";

        if (marca == null) {
            System.err.println("[ModeloDAO] Erro: Marca não pode ser nula para listar modelos.");
            return modelos;
        }

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, marca.getId());
            System.out.println("[ModeloDAO] Executando consulta: " + pstmt.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("idModelo");
                    String nome = rs.getString("nomeModelo");

                    modelos.add(new Modelo(id, nome, marca));
                }
                System.out.println("[ModeloDAO] Modelos encontrados para " + marca.getNome() + ": " + modelos.size());
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar modelos para a marca " + marca.getNome() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return modelos;
    }
}
