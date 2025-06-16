package db;

import entidades.Marca;
import entidades.Modelo;
import entidades.Proprietario;
import entidades.Veiculo;
import relatorios.ContagemVeiculosPorMarca;
import utilitarios.PlacaUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    public boolean salvar(Veiculo veiculo) {
        String sql = "INSERT INTO veiculo (placa, idMarca, idModelo, ano, cor, proprietarioAtualCpf, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, veiculo.getPlaca());
            pstmt.setInt(2, veiculo.getMarca().getId());
            pstmt.setInt(3, veiculo.getModelo().getId());
            pstmt.setInt(4, veiculo.getAno());
            pstmt.setString(5, veiculo.getCor());
            pstmt.setString(6, veiculo.getProprietarioAtual().getCpf());
            pstmt.setString(7, "ATIVO");

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.salvar] Falha ao salvar veículo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Veiculo buscarPorPlaca(String placaParametro) {
        Veiculo veiculo = null;
        // A placaParametro já vem normalizada (maiúscula, sem hífen) do Gerenciador

        String sql =
                "SELECT v.placa, v.ano, v.cor, v.status, " +
                "       p.cpf AS prop_cpf, p.nome AS prop_nome, " +
                "       m.idMarca AS marca_id, m.nomeMarca AS marca_nome, " +
                "       md.idModelo AS modelo_id, md.nomeModelo AS modelo_nome " +
                "FROM veiculo v " +
                "LEFT JOIN proprietario p ON v.proprietarioAtualCpf = p.cpf " +
                "LEFT JOIN marca m ON v.IdMarca = m.idMarca " +
                "LEFT JOIN modelo md ON v.IdModelo = md.idModelo " +
                "WHERE UPPER(REPLACE(v.placa, '-', '')) = ?";

        System.out.println("[VeiculoDAO] Buscando com placa normalizada: " + placaParametro); // Para depuração

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaParametro); // placaParametro já está em maiúsculas e sem hífen

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[VeiculoDAO] Veículo encontrado no ResultSet para placa: " + placaParametro); //Para depuração

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
                        modelo = new Modelo(modeloId, rs.getString("modelo_nome"), marca);
                    }

                    // Cria ProprietarioAtual
                    Proprietario proprietarioAtual = null;
                    String propCpf = rs.getString("prop_cpf"); // Usa o alias
                    if (propCpf != null) {
                        proprietarioAtual = new Proprietario(rs.getString("prop_nome"), propCpf);
                    }

                    // Cria Veiculo
                    veiculo = new Veiculo(
                            rs.getString("placa"),
                            marca,
                            modelo,
                            rs.getInt("ano"),
                            rs.getString("cor"),
                            rs.getString("status"), // NOVO: Passando o status
                            proprietarioAtual
                    );
                } else {
                    System.out.println("[VeiculoDAO] Nenhum veículo encontrado no ResultSet para placa normalizada: " + placaParametro); // Para depuração
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.buscarPorPlaca] Falha ao buscar veículo com placa '" + placaParametro + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return veiculo;
    }

    public boolean atualizarVeiculoParaTransferencia(String placaOriginalParam, String placaNova, String cpfNovoProprietario){
        String sql = "UPDATE veiculo SET placa = ?, proprietarioAtualCpf = ? WHERE UPPER(REPLACE(placa, '-', '')) = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaNova);
            pstmt.setString(2, cpfNovoProprietario);
            pstmt.setString(3, placaOriginalParam);

            int linhasAfetadas = pstmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("[VeiculoDAO] Veículo atualizado com sucesso. Nova placa: " + placaNova + ", Novo CPF: " + cpfNovoProprietario);
                return true;
            } else {
                System.out.println("[VeiculoDAO] Nenhuma linha atualizada para placa original (normalizada): " + placaOriginalParam +
                        ". Veículo não encontrado ou dados já eram os mesmos.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.atualizarVeiculoParaTransferencia] Falha ao atualizar veículo (placa original normalizada: '" +
                    placaOriginalParam + "'): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Veiculo> buscarVeiculosPorCpf(String cpfInput){
        List<Veiculo> veiculosDoProprietario = new ArrayList<>();

        String sql =
                "SELECT v.placa, v.ano, v.cor, v.status, " +
                "       p.cpf AS prop_cpf, p.nome AS prop_nome, " +
                "       m.idMarca AS marca_id, m.nomeMarca AS marca_nome, " +
                "       md.idModelo AS modelo_id, md.nomeModelo AS modelo_nome " +
                "FROM veiculo v " +
                "INNER JOIN proprietario p ON v.proprietarioAtualCpf = p.cpf " +
                "LEFT JOIN marca m ON v.IdMarca = m.idMarca " +
                "LEFT JOIN modelo md ON v.IdModelo = md.idModelo " +
                "WHERE v.proprietarioAtualCpf = ? AND v.status = 'ATIVO'";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpfInput);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Marca marca = new Marca(rs.getInt("marca_id"), rs.getString("marca_nome"));
                    Modelo modelo = new Modelo(rs.getInt("modelo_id"), rs.getString("modelo_nome"), marca);

                    Proprietario proprietario = new Proprietario(rs.getString("prop_nome"), rs.getString("prop_cpf"));

                    Veiculo veiculo = new Veiculo(
                            rs.getString("placa"),
                            marca,
                            modelo,
                            rs.getInt("ano"),
                            rs.getString("cor"),
                            rs.getString("status"),
                            proprietario
                    );
                    veiculosDoProprietario.add(veiculo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar veículos por CPF do proprietário (" + cpfInput + "): " + e.getMessage());
            e.printStackTrace();
        }
        return veiculosDoProprietario;
    }

    public List<ContagemVeiculosPorMarca> contarVeiculosPorMarca(){
        List<ContagemVeiculosPorMarca> contagemPorMarca = new ArrayList<>();

        String sql = "SELECT m.nomeMarca, COUNT(v.placa) AS quantidade " +
                "FROM marca m " +
                "LEFT JOIN veiculo v ON m.idMarca = v.IdMarca AND v.status = 'ATIVO' " +
                "GROUP BY m.nomeMarca " +
                "ORDER BY m.nomeMarca ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String nomeMarca = rs.getString("nomeMarca");
                int quantidade = rs.getInt("quantidade");
                contagemPorMarca.add(new ContagemVeiculosPorMarca(nomeMarca, quantidade));
            }

        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.contarVeiculosPorMarca] Falha ao contar veículos por marca: " + e.getMessage());
            e.printStackTrace();
        }
        return contagemPorMarca;
    }

    public List<Veiculo> buscarVeiculosComPlacaAntiga(){
        List<Veiculo> todosOsVeiculos = new ArrayList<>();
        List<Veiculo> veiculosComPlacaAntiga = new ArrayList<>();

        String sql =
                "SELECT v.placa, v.ano, v.cor, v.status, " +
                "       p.cpf AS prop_cpf, p.nome AS prop_nome, " +
                "       m.idMarca AS marca_id, m.nomeMarca AS marca_nome, " +
                "       md.idModelo AS modelo_id, md.nomeModelo AS modelo_nome " +
                "FROM veiculo v " +
                "LEFT JOIN proprietario p ON v.proprietarioAtualCpf = p.cpf " +
                "LEFT JOIN marca m ON v.IdMarca = m.idMarca " +
                "LEFT JOIN modelo md ON v.IdModelo = md.idModelo " +
                "ORDER BY v.placa ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Marca marca = new Marca(rs.getInt("marca_id"), rs.getString("marca_nome"));
                Modelo modelo = new Modelo(rs.getInt("modelo_id"), rs.getString("modelo_nome"), marca);

                Proprietario proprietarioAtual = null;
                String propCpf = rs.getString("prop_cpf");
                if (propCpf != null) {
                    proprietarioAtual = new Proprietario(rs.getString("prop_nome"), propCpf);
                }

                Veiculo veiculo = new Veiculo(
                        rs.getString("placa"),
                        marca,
                        modelo,
                        rs.getInt("ano"),
                        rs.getString("cor"),
                        rs.getString("status"),
                        proprietarioAtual
                );
                todosOsVeiculos.add(veiculo);
            }

            for (Veiculo v : todosOsVeiculos) {
                if (PlacaUtil.ehPlacaAntiga(v.getPlaca()) && "ATIVO".equalsIgnoreCase(v.getStatus())) {
                    veiculosComPlacaAntiga.add(v);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.buscarVeiculosComPlacaAntiga] Falha ao buscar veículos: " + e.getMessage());
            e.printStackTrace();
        }
        return veiculosComPlacaAntiga;
    }

    public boolean baixarVeiculo(String placaNormalizada) {
        String sql = "UPDATE veiculo SET status = 'INATIVO', proprietarioAtualCpf = NULL WHERE UPPER(REPLACE(placa, '-', '')) = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaNormalizada);
            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - VeiculoDAO.darBaixaVeiculo] Falha ao dar baixa no veículo com placa normalizada '" +
                    placaNormalizada + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
