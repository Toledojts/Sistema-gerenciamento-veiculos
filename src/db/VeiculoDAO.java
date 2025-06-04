package db;

import entidades.Marca;
import entidades.Modelo;
import entidades.Proprietario;
import entidades.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public Veiculo buscarPorPlaca(String placaParametro) {
        Veiculo veiculo = null;
        // A placaParametro já vem normalizada (maiúscula, sem hífen) do Gerenciador

        String sql = "SELECT v.placa, v.ano, v.cor, " +
                "       p.cpf AS prop_cpf, p.nome AS prop_nome, " +
                "       m.idMarca AS marca_id, m.nomeMarca AS marca_nome, " + // Usando alias para clareza no getInt/getString
                "       md.idModelo AS modelo_id, md.nomeModelo AS modelo_nome " +
                "FROM veiculo v " +
                "LEFT JOIN proprietario p ON v.proprietarioAtualCpf = p.cpf " +
                "LEFT JOIN marca m ON v.IdMarca = m.idMarca " +
                "LEFT JOIN modelo md ON v.IdModelo = md.idModelo " +
                "WHERE UPPER(REPLACE(v.placa, '-', '')) = ?"; // MODIFICAÇÃO AQUI

        System.out.println("[VeiculoDAO] Buscando com placa normalizada: " + placaParametro); // Para depuração

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaParametro); // placaParametro já está em maiúsculas e sem hífen

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[VeiculoDAO] Veículo encontrado no ResultSet para placa: " + placaParametro); // Para depuração

                    // Criar Marca
                    Marca marca = null;
                    int marcaId = rs.getInt("marca_id"); // Usa o alias definido no SELECT
                    if (!rs.wasNull()) {
                        marca = new Marca(marcaId, rs.getString("marca_nome"));
                    }

                    // Criar Modelo
                    Modelo modelo = null;
                    int modeloId = rs.getInt("modelo_id"); // Usa o alias
                    if (!rs.wasNull()) {
                        // Assumindo que a marca do modelo é a mesma marca associada diretamente ao veículo
                        // Se a entidade Modelo espera um objeto Marca que deve ser construído com base
                        // em um md.idMarca diferente (se existisse), a lógica seria mais complexa aqui.
                        // Mas com a estrutura atual, a marca já obtida deve ser a correta para o modelo.
                        modelo = new Modelo(modeloId, rs.getString("modelo_nome"), marca);
                    }

                    // Criar ProprietarioAtual
                    Proprietario proprietarioAtual = null;
                    String propCpf = rs.getString("prop_cpf"); // Usa o alias
                    if (propCpf != null) {
                        proprietarioAtual = new Proprietario(rs.getString("prop_nome"), propCpf);
                    }

                    // Criar Veiculo
                    veiculo = new Veiculo(
                            rs.getString("placa"), // Pega a placa original do banco (com hífen, se tiver)
                            marca,
                            modelo,
                            rs.getInt("ano"),
                            rs.getString("cor"),
                            proprietarioAtual
                    );
                } else {
                    System.out.println("[VeiculoDAO] Nenhum veículo encontrado no ResultSet para placa normalizada: " + placaParametro); // Para depuração
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar veículo por placa (" + placaParametro + "): " + e.getMessage());
            e.printStackTrace();
        }
        return veiculo;
    }

    public boolean atualizarVeiculoParaTransferencia(String placaOriginalParam, String placaNova, String cpfNovoProprietario){
        String sql = "UPDATE veiculo SET placa = ?, proprietarioAtualCpf = ? WHERE UPPER(REPLACE(placa, '-', '')) = ?"; // MODIFICAÇÃO AQUI

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaNova);           // A nova placa (ex: "CBA5C78")
            pstmt.setString(2, cpfNovoProprietario); // O CPF do novo proprietário
            pstmt.setString(3, placaOriginalParam);  // A placa original normalizada para encontrar no WHERE (ex: "CBA5678")

            int linhasAfetadas = pstmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("[VeiculoDAO] Veículo atualizado com sucesso. Nova placa: " + placaNova + ", Novo CPF: " + cpfNovoProprietario);
                return true;
            } else {
                System.out.println("[VeiculoDAO] Nenhuma linha atualizada para placa original (normalizada): " + placaOriginalParam + ". Veículo não encontrado ou dados já eram os mesmos.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar veículo para transferência (placa original normalizada: " + placaOriginalParam + "): " + e.getMessage());
            e.printStackTrace(); // IMPORTANTE: Verifique o console para erros detalhados do SQL aqui!
            return false;
        }
    }

}
