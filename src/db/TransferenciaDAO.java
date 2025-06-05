package db;

import entidades.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
            System.err.println("[ERRO NO DAO - TransferenciaDAO.salvar] Falha ao salvar registro de transferência para o veículo placa");
            e.printStackTrace();
            return false;
        }
    }

    public List<Transferencia> buscarTransferenciasPorPlaca(String placaInput){
        List<Transferencia> historico = new ArrayList<>();

        String sql = "SELECT " +
                "t.dataTransferencia, " +
                "v.placa AS veiculo_placa, " + // Placa do veículo na transferência
                "marca_v.nomeMarca AS veiculo_marca_nome, " + // Nome da marca do veículo
                "modelo_v.nomeModelo AS veiculo_modelo_nome, " + // Nome do modelo do veículo
                "pa.cpf AS ant_prop_cpf, pa.nome AS ant_prop_nome, " + // Proprietário Anterior
                "pn.cpf AS novo_prop_cpf, pn.nome AS novo_prop_nome " +  // Novo Proprietário
                "FROM transferencia t " +
                "INNER JOIN veiculo v ON t.veiculoPlaca = v.placa " + // Garante que o veículo da transferência exista
                "LEFT JOIN marca marca_v ON v.IdMarca = marca_v.idMarca " + // Para obter nome da marca do veículo
                "LEFT JOIN modelo modelo_v ON v.IdModelo = modelo_v.idModelo " + // Para obter nome do modelo do veículo
                "LEFT JOIN proprietario pa ON t.proprietarioAnteriorCpf = pa.cpf " +
                "LEFT JOIN proprietario pn ON t.novoProprietarioCpf = pn.cpf " +
                "WHERE t.veiculoPlaca = ? " +
                "ORDER BY t.dataTransferencia DESC"; // Mais recente primeiro

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placaInput);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Criar Veiculo (simplificado, apenas com o necessário para a transferência ou completo)
                    // Para popular o Veiculo dentro da Transferencia, precisamos da Marca e Modelo do Veiculo
                    Marca marcaDoVeiculo = new Marca(0, rs.getString("veiculo_marca_nome")); // ID da marca não é pego aqui, só nome
                    Modelo modeloDoVeiculo = new Modelo(0, rs.getString("veiculo_modelo_nome"), marcaDoVeiculo); // ID do modelo não é pego
                    Veiculo veiculoDaTransferencia = new Veiculo(rs.getString("veiculo_placa"), marcaDoVeiculo, modeloDoVeiculo, 0, null, null); // Ano, cor, prop. atual não são o foco aqui

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
                            rs.getDate("dataTransferencia").toLocalDate(), // Conversão de java.sql.Date para LocalDate
                            veiculoDaTransferencia // Objeto Veiculo associado à transferência
                    );
                    historico.add(transferencia);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar histórico de transferências para a placa (" + placaInput + "): " + e.getMessage());
            e.printStackTrace();
        }
        return historico;
    }

    public List<Transferencia> buscarTransferenciasPorPeriodo(LocalDate dataInicio, LocalDate dataFim){
        List<Transferencia> transferenciasNoPeriodo = new ArrayList<>();

        String sql = "SELECT " +
                "t.dataTransferencia, " +
                "v.placa AS veiculo_placa_transferida, " + // Usar um alias diferente se 'veiculo_placa' já é usado em outro método
                "marca_v.nomeMarca AS veiculo_marca_nome, " +
                "modelo_v.nomeModelo AS veiculo_modelo_nome, " +
                "v.ano AS veiculo_ano, v.cor AS veiculo_cor, " + // Detalhes adicionais do veículo
                "pa.cpf AS ant_prop_cpf, pa.nome AS ant_prop_nome, " +
                "pn.cpf AS novo_prop_cpf, pn.nome AS novo_prop_nome " +
                "FROM transferencia t " +
                "INNER JOIN veiculo v ON t.veiculoPlaca = v.placa " +
                "LEFT JOIN marca marca_v ON v.IdMarca = marca_v.idMarca " +
                "LEFT JOIN modelo modelo_v ON v.IdModelo = modelo_v.idModelo " +
                "LEFT JOIN proprietario pa ON t.proprietarioAnteriorCpf = pa.cpf " + // Proprietário anterior pode ser NULL
                "INNER JOIN proprietario pn ON t.novoProprietarioCpf = pn.cpf " + // Novo proprietário deve existir
                "WHERE t.dataTransferencia >= ? AND t.dataTransferencia <= ? " +
                "ORDER BY t.dataTransferencia DESC, v.placa ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(dataInicio)); // Converte LocalDate para java.sql.Date
            pstmt.setDate(2, Date.valueOf(dataFim));   // Converte LocalDate para java.sql.Date

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Criar Marca para o Veículo da Transferência
                    Marca marcaDoVeiculo = new Marca(0, rs.getString("veiculo_marca_nome")); // ID da marca não é crucial para este relatório específico

                    // Criar Modelo para o Veículo da Transferência
                    Modelo modeloDoVeiculo = new Modelo(0, rs.getString("veiculo_modelo_nome"), marcaDoVeiculo); // ID do modelo não é crucial

                    // Criar objeto Veiculo simplificado para a Transferência
                    Veiculo veiculoDaTransferencia = new Veiculo(
                            rs.getString("veiculo_placa_transferida"),
                            marcaDoVeiculo,
                            modeloDoVeiculo,
                            rs.getInt("veiculo_ano"), // Adicionado ano
                            rs.getString("veiculo_cor"), // Adicionada cor
                            null // O proprietário atual do veículo não é o foco do histórico de transferência em si
                    );

                    // Criar Proprietario Anterior
                    Proprietario proprietarioAnterior = null;
                    String antCpf = rs.getString("ant_prop_cpf");
                    if (antCpf != null) {
                        proprietarioAnterior = new Proprietario(rs.getString("ant_prop_nome"), antCpf);
                    }

                    // Criar Novo Proprietario
                    Proprietario novoProprietario = new Proprietario(rs.getString("novo_prop_nome"), rs.getString("novo_prop_cpf"));

                    // Criar Transferencia
                    Transferencia transferencia = new Transferencia(
                            proprietarioAnterior,
                            novoProprietario,
                            rs.getDate("dataTransferencia").toLocalDate(), // Converte java.sql.Date para LocalDate
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
