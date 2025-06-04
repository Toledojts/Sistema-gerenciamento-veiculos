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
    // Método para buscar modelos de uma marca específica
    public List<Modelo> listarPorMarca(Marca marca) {
        List<Modelo> modelos = new ArrayList<>();
        // Use nomes exatos das suas colunas (ex: id_modelo, nome_modelo, id_marca)
        String sql = "SELECT idModelo, nomeModelo FROM modelo WHERE idMarca = ? ORDER BY idModelo ASC";

        if (marca == null) {
            System.err.println("[ModeloDAO] Erro: Marca não pode ser nula para listar modelos.");
            return modelos; // Retorna lista vazia
        }

        // try-with-resources
        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Define o parâmetro da query (?)
            pstmt.setLong(1, marca.getId());
            System.out.println("[ModeloDAO] Executando consulta: " + pstmt.toString()); // Mostra a query com o parâmetro

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("idModelo"); // Nome exato da coluna
                    String nome = rs.getString("nomeModelo"); // Nome exato da coluna

                    // Cria o objeto Modelo, passando a Marca que já temos
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

    // Outros métodos do DAO (buscarPorId, salvar, etc.) podem ser adicionados aqui
}
