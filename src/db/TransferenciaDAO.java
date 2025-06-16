package db;

import entidades.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransferenciaDAO {
    public boolean salvar(Transferencia transferencia) {
        String sql = "INSERT INTO transferencia (dataTransferencia, veiculoPlaca, proprietarioAnteriorCpf, novoProprietarioCpf) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(transferencia.getDataTransferencia()));
            pstmt.setString(2, transferencia.getVeiculo().getPlaca());

            if (transferencia.getAntigoProprietario() != null) {
                pstmt.setString(3, transferencia.getAntigoProprietario().getCpf());
            } else {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }
            pstmt.setString(4, transferencia.getNovoProprietario().getCpf());

            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - TransferenciaDAO.salvar] Falha ao salvar registro de transferência para o veículo placa");
            e.printStackTrace();
            return false;
        }
    }

    public List<Transferencia> buscarTransferenciasPorPlaca(String placaInput){
        List<Transferencia> historico = new ArrayList<>();
        String sql =
                "SELECT " + "t.dataTransferencia, " +
                "v.placa AS veiculo_placa, v.status AS veiculo_status, " +
                "marca_v.nomeMarca AS veiculo_marca_nome, " +
                "modelo_v.nomeModelo AS veiculo_modelo_nome, " +
                "pa.cpf AS ant_prop_cpf, pa.nome AS ant_prop_nome, " +
                "pn.cpf AS novo_prop_cpf, pn.nome AS novo_prop_nome " +
                "FROM transferencia t " +
                "INNER JOIN veiculo v ON t.veiculoPlaca = v.placa " +
                "LEFT JOIN marca marca_v ON v.IdMarca = marca_v.idMarca " +
                "LEFT JOIN modelo modelo_v ON v.IdModelo = modelo_v.idModelo " +
                "LEFT JOIN proprietario pa ON t.proprietarioAnteriorCpf = pa.cpf " +
                "LEFT JOIN proprietario pn ON t.novoProprietarioCpf = pn.cpf " +
                "WHERE t.veiculoPlaca = ? " +
                "ORDER BY t.dataTransferencia DESC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaInput);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Marca marcaDoVeiculo = new Marca(0, rs.getString("veiculo_marca_nome"));
                    Modelo modeloDoVeiculo = new Modelo(0, rs.getString("veiculo_modelo_nome"), marcaDoVeiculo);

                    Veiculo veiculoDaTransferencia = new Veiculo(
                            rs.getString("veiculo_placa"),
                            marcaDoVeiculo,
                            modeloDoVeiculo,
                            0,
                            null,
                            rs.getString("veiculo_status"),
                            null
                    );

                    Proprietario anterior = null;
                    String antCpf = rs.getString("ant_prop_cpf");
                    if (antCpf != null) {
                        anterior = new Proprietario(rs.getString("ant_prop_nome"), antCpf);
                    }
                    Proprietario novo = null;
                    String novoCpf = rs.getString("novo_prop_cpf");
                    if (novoCpf != null) {
                        novo = new Proprietario(rs.getString("novo_prop_nome"), novoCpf);
                    }

                    Transferencia transferencia = new Transferencia(
                            anterior,
                            novo,
                            rs.getDate("dataTransferencia").toLocalDate(),
                            veiculoDaTransferencia
                    );
                    historico.add(transferencia);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - TransferenciaDAO.buscarTransferenciasPorPlaca] Falha ao buscar histórico: " + e.getMessage());
            e.printStackTrace();
        }
        return historico;
    }

    public List<Transferencia> buscarTransferenciasPorPeriodo(LocalDate dataInicio, LocalDate dataFim){
        List<Transferencia> transferenciasNoPeriodo = new ArrayList<>();

        String sql = "SELECT " +
                "t.dataTransferencia, " +
                "v.placa AS veiculo_placa_transferida, v.status, " +
                "marca_v.nomeMarca AS veiculo_marca_nome, " +
                "modelo_v.nomeModelo AS veiculo_modelo_nome, " +
                "v.ano AS veiculo_ano, v.cor AS veiculo_cor, " +
                "pa.cpf AS ant_prop_cpf, pa.nome AS ant_prop_nome, " +
                "pn.cpf AS novo_prop_cpf, pn.nome AS novo_prop_nome " +
                "FROM transferencia t " +
                "INNER JOIN veiculo v ON t.veiculoPlaca = v.placa " +
                "LEFT JOIN marca marca_v ON v.IdMarca = marca_v.idMarca " +
                "LEFT JOIN modelo modelo_v ON v.IdModelo = modelo_v.idModelo " +
                "LEFT JOIN proprietario pa ON t.proprietarioAnteriorCpf = pa.cpf " +
                "INNER JOIN proprietario pn ON t.novoProprietarioCpf = pn.cpf " +
                "WHERE t.dataTransferencia >= ? AND t.dataTransferencia <= ? " +
                "ORDER BY t.dataTransferencia DESC, v.placa ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(dataInicio));
            pstmt.setDate(2, Date.valueOf(dataFim));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Marca marcaDoVeiculo = new Marca(0, rs.getString("veiculo_marca_nome"));
                    Modelo modeloDoVeiculo = new Modelo(0, rs.getString("veiculo_modelo_nome"), marcaDoVeiculo);
                    Veiculo veiculoDaTransferencia = new Veiculo(
                            rs.getString("veiculo_placa_transferida"),
                            marcaDoVeiculo,
                            modeloDoVeiculo,
                            rs.getInt("veiculo_ano"),
                            rs.getString("veiculo_cor"),
                            rs.getString("status"),
                            null
                    );
                    Proprietario proprietarioAnterior = null;
                    String antCpf = rs.getString("ant_prop_cpf");
                    if (antCpf != null) {
                        proprietarioAnterior = new Proprietario(rs.getString("ant_prop_nome"), antCpf);
                    }
                    Proprietario novoProprietario = new Proprietario(rs.getString("novo_prop_nome"), rs.getString("novo_prop_cpf"));
                    Transferencia transferencia = new Transferencia(
                            proprietarioAnterior,
                            novoProprietario,
                            rs.getDate("dataTransferencia").toLocalDate(),
                            veiculoDaTransferencia
                    );
                    transferenciasNoPeriodo.add(transferencia);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO NO DAO - TransferenciaDAO.buscarTransferenciasPorPeriodo] Falha ao buscar transferências: " + e.getMessage());
            e.printStackTrace();
        }
        return transferenciasNoPeriodo;
    }
}
