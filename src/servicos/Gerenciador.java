package servicos;

import db.*;
import entidades.*;
import relatorios.ContagemVeiculosPorMarca;
import utilitarios.CpfUtil;
import utilitarios.DataUtil;
import utilitarios.GeradorPlacaUtil;
import utilitarios.PlacaUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

public class Gerenciador {

    private final MarcaDAO marcaDAO;
    private final ModeloDAO modeloDAO;
    private final ProprietarioDAO proprietarioDAO;
    private final VeiculoDAO veiculoDAO;
    private final TransferenciaDAO transferenciaDAO;

    public Gerenciador(MarcaDAO marcaDAO, ModeloDAO modeloDAO, ProprietarioDAO proprietarioDAO, VeiculoDAO veiculoDAO, TransferenciaDAO transferenciaDAO) {
        this.marcaDAO = marcaDAO;
        this.modeloDAO = modeloDAO;
        this.proprietarioDAO = proprietarioDAO;
        this.veiculoDAO = veiculoDAO;
        this.transferenciaDAO = transferenciaDAO;
    }

    public List<Marca> listarMarcasDisponiveis() {
        return marcaDAO.listarTodas();
    }

    public List<Modelo> listarModelosDisponiveis(Marca marca) {
        return modeloDAO.listarPorMarca(marca);
    }

    public void carregarDadosIniciais(){
        if (marcaDAO.contar() == 0) {
            System.out.println("[Gerenciador] Banco de dados parece estar vazio. Iniciando carga de dados iniciais.");
            try (Connection conn = Conexao.getConnection()) {
                CargaInicialDados.popularBanco(conn);
            } catch (SQLException e) {
                System.err.println("[Gerenciador] Falha ao obter conexão para carga inicial de dados: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[Gerenciador] Banco de dados já populado. Nenhuma carga inicial necessária.");
        }
    }

    //metodo para cadastro com placa
    public boolean cadastrarVeiculo(String placaInput, Marca marca, Modelo modelo, int ano, String cor, String cpfProprietarioInput, String nomeProprietarioInput) {
        System.out.println("\n[Gerenciador] Iniciando processo de cadastro...");

        // Valida Placa (Formato antigo ou Mercosul)
        if (!validarFormatoPlaca(placaInput)) {
            System.err.println("[Gerenciador] Erro: Formato da placa inválido.");
            return false;
        }

        if (verificarPlacaExistente(placaInput)){
            System.out.println("[Gerenciador] Erro: Placa já cadastrada!");
            return false;
        }

        // Normaliza placa para maiúsculas, por exemplo
        String placa = placaInput.toUpperCase();
        System.out.println("[Gerenciador] Placa validada: " + placa);

        // Valida CPF (Formato 11 dígitos)
        if (!validarFormatoCPF(cpfProprietarioInput)) {
            System.err.println("[Gerenciador] Erro: Formato do CPF inválido (deve ter 11 dígitos).");
            return false;
        }

        Proprietario proprietario = proprietarioDAO.buscarPorCPF(cpfProprietarioInput);
        if (proprietario == null) {
            System.out.println("[Gerenciador] Proprietário com CPF " + cpfProprietarioInput + " não encontrado. Cadastrando novo...");
            if (nomeProprietarioInput == null || nomeProprietarioInput.trim().isEmpty()) {
                System.err.println("[Gerenciador] Erro: Nome do novo proprietário não pode ser vazio.");
                return false;
            }
            proprietario = new Proprietario(nomeProprietarioInput.trim(), cpfProprietarioInput);

            boolean salvouProprietario = proprietarioDAO.salvar(proprietario);
            if (!salvouProprietario){
                System.err.println("[Gerenciador] Falha ao salvar o novo proprietário no banco de dados. Cadastro de veículo interrompido.");
                return false;
            }

        } else {
            System.out.println("[Gerenciador] Proprietário encontrado: " + proprietario.getNome() + " (CPF: " + proprietario.getCpf() + ")");
        }

        // Valida outros dados
        if (marca == null || modelo == null || ano <= 1900 || cor == null || cor.trim().isEmpty()) {
            System.err.println("[Gerenciador] Erro: Dados do veículo (marca, modelo, ano, cor) inválidos.");
            return false;
        }

        // Valida se modelo pertence à marca
        if (modelo.getMarca().getId() != marca.getId()) {
            System.err.println("[Gerenciador] Erro: Inconsistência - Modelo não pertence à Marca selecionada.");
            return false;
        }

        Veiculo novoVeiculo = new Veiculo(placa, marca, modelo, ano, cor.trim(), "ATIVO", proprietario);
        System.out.println("[Gerenciador] Objeto Veiculo pronto para salvar.");

        boolean salvouVeiculo = veiculoDAO.salvar(novoVeiculo);
        if (!salvouVeiculo){
            System.err.println("[Gerenciador] Falha ao salvar o veículo. Verifique os logs do DAO.");
            return false;
        }
        System.out.println("[Gerenciador] Cadastro concluído com sucesso para placa: " + placa);
        return true;
    }

    //metodo para cadastro sem placa
    public boolean cadastrarVeiculo(Marca marca, Modelo modelo, int ano, String cor, String cpfProprietarioInput, String nomeProprietarioInput){
        System.out.println("[Gerenciador] Iniciando processo de novo emplacamento...");

        String novaPlaca;
        do {
            novaPlaca = GeradorPlacaUtil.gerarPlacaMercosul();
        } while (verificarPlacaExistente(novaPlaca));

        System.out.println("[Gerenciador] Placa única '" + novaPlaca + "' gerada e validada.");

        // Metodo original
        return cadastrarVeiculo(novaPlaca, marca, modelo, ano, cor, cpfProprietarioInput, nomeProprietarioInput);
    }

    public boolean verificarPlacaExistente(String placaInput){
        if (placaInput == null || placaInput.trim().isEmpty()){
            return false;
        }

        String placaNormalizada = placaInput.toUpperCase().replace("-", "");

        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaNormalizada);

        return veiculo != null;
    }

    public boolean validarFormatoPlaca(String placa) {
        // Uma placa é válida se ela for do formato antigo OU do formato Mercosul.
        boolean ehValida = PlacaUtil.ehPlacaAntiga(placa) || PlacaUtil.ehPlacaMercosul(placa);
        System.out.printf("[Gerenciador] Validação da placa %s: %s%n", placa, (ehValida ? "Válido" : "Formato inválido"));

        return ehValida;
    }

    public boolean validarFormatoCPF(String cpf) {
        return CpfUtil.validar(cpf); // Apenas chama o utilitário
    }

    public Proprietario buscarProprietarioPorCPF(String cpf) {
        if (!validarFormatoCPF(cpf)) {
            return null;
        }
        return proprietarioDAO.buscarPorCPF(cpf);
    }

    public boolean transferirPropriedade(String placaVeiculoInput, String cpfNovoProprietarioInput,
                                         String nomeNovoProprietarioInput, String dataTransferenciaStr) {

        System.out.println("\n[Gerenciador] Iniciando processo de transferência de propriedade...");

        // Normaliza e Valida Placa do Veículo
        String placaNormalizada = placaVeiculoInput != null ? placaVeiculoInput.toUpperCase().replace("-", "") : null;
        if (placaNormalizada == null || !(PlacaUtil.ehPlacaAntiga(placaNormalizada) || PlacaUtil.ehPlacaMercosul(placaNormalizada))) {
            System.err.println("[Gerenciador] Formato de placa do veículo inválido ou não fornecida.");
            return false;
        }

        // Valida CPF do Novo Proprietário
        if (!validarFormatoCPF(cpfNovoProprietarioInput)) { // Reutiliza sua validação de CPF
            System.err.println("[Gerenciador] Formato do CPF do novo proprietário inválido.");
            return false;
        }

        // Valida e Converter Data da Transferência
        LocalDate dataTransferencia = DataUtil.parseData(dataTransferenciaStr); // Usando DataUtil
        if (dataTransferencia == null) {
            System.err.println("[Gerenciador] Data da transferência inválida.");
            return false;
        }

        //Verifica se a data é futura
        if (dataTransferencia.isAfter(ChronoLocalDate.from(LocalDateTime.now()))){
            System.out.println("[Gerenciador] ERRO: Data da transferência futura.");
            return false;
        }

        // Busca Veículo pelo DAO
        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaNormalizada);
        if (veiculo == null) {
            System.err.println("[Gerenciador] Veículo com placa " + placaNormalizada + " não encontrado.");
            return false;
        }

        // Verifica se o novo proprietário é diferente do atual
        Proprietario proprietarioAnterior = veiculo.getProprietarioAtual();
        if (proprietarioAnterior != null && proprietarioAnterior.getCpf().equals(cpfNovoProprietarioInput)) {
            System.err.println("[Gerenciador] O novo proprietário (CPF: " + cpfNovoProprietarioInput + ") deve ser diferente do proprietário atual.");
            return false;
        }

        // Busca ou Cadastra Novo Proprietário
        Proprietario novoProprietario = proprietarioDAO.buscarPorCPF(cpfNovoProprietarioInput);
        if (novoProprietario == null) {
            if (nomeNovoProprietarioInput == null || nomeNovoProprietarioInput.trim().isEmpty()) {
                System.err.println("[Gerenciador] Nome do novo proprietário é obrigatório para cadastro (CPF: " + cpfNovoProprietarioInput + ").");
                return false;
            }
            novoProprietario = new Proprietario(nomeNovoProprietarioInput.trim(), cpfNovoProprietarioInput);
            if (!proprietarioDAO.salvar(novoProprietario)) {
                System.err.println("[Gerenciador] Falha ao cadastrar o novo proprietário (CPF: " + cpfNovoProprietarioInput + ").");
                return false;
            }
            System.out.println("[Gerenciador] Novo proprietário (CPF: " + cpfNovoProprietarioInput + ", Nome: " + novoProprietario.getNome() + ") cadastrado com sucesso.");
        } else {
            System.out.println("[Gerenciador] Novo proprietário encontrado: " + novoProprietario.getNome() + " (CPF: " + novoProprietario.getCpf() + ").");
        }

        // Converte Placa para Mercosul, se necessário
        String placaOriginalVeiculo = veiculo.getPlaca().toUpperCase().replace("-", ""); // Placa atual do veículo, limpa
        String placaFinalVeiculo = placaOriginalVeiculo; // Por padrão, a placa não muda

        if (PlacaUtil.ehPlacaAntiga(placaOriginalVeiculo)) {
            placaFinalVeiculo = PlacaUtil.converterPlacaAntigaParaMercosul(placaOriginalVeiculo);
            System.out.println("[Gerenciador] Placa antiga " + placaOriginalVeiculo + " convertida para Mercosul: " + placaFinalVeiculo + ".");
        }

        boolean atualizouVeiculo = veiculoDAO.atualizarVeiculoParaTransferencia(placaOriginalVeiculo, placaFinalVeiculo, novoProprietario.getCpf());

        if (!atualizouVeiculo) {
            System.err.println("[Gerenciador] Falha ao atualizar os dados do veículo (proprietário/placa) no banco de dados.");
            return false;
        }
        System.out.println("[Gerenciador] Veículo (Placa antiga: " + placaOriginalVeiculo + " -> Placa nova: " + placaFinalVeiculo + ") atualizado com novo proprietário: " + novoProprietario.getNome() + ".");

        // Atualiza o objeto veículo em memória para refletir a mudança de placa (se houve) para o registro de transferência
        veiculo.setPlaca(placaFinalVeiculo);
        veiculo.setProprietarioAtual(novoProprietario);


        // Cria e Salva Registro de Transferência
        Transferencia novaTransferencia = new Transferencia(proprietarioAnterior, novoProprietario, dataTransferencia, veiculo);

        if (!transferenciaDAO.salvar(novaTransferencia)) {
            System.err.println("[Gerenciador] Falha crítica: Veículo foi atualizado, mas não foi possível registrar a transferência. Contate o suporte.");
            return false;
        }
        System.out.println("[Gerenciador] Registro de transferência salvo com sucesso.");

        System.out.println("[Gerenciador] Processo de Transferência de Propriedade para a placa " + placaFinalVeiculo + " concluído com sucesso!");
        return true;
    }

    public Veiculo buscarVeiculoPorPlacaMenu(String placaInput) {
        if (placaInput == null || placaInput.trim().isEmpty()) {
            System.err.println("[Gerenciador] Placa não fornecida para busca.");
            return null;
        }
        String placaNormalizada = placaInput.toUpperCase().replace("-", "");

        if (!(PlacaUtil.ehPlacaAntiga(placaNormalizada) || PlacaUtil.ehPlacaMercosul(placaNormalizada))) {
            System.err.println("[Gerenciador] Formato de placa inválido para busca: " + placaNormalizada);
            return null;
        }
        return veiculoDAO.buscarPorPlaca(placaNormalizada);
    }

    public List<Veiculo> consultarVeiculoPorCpf(String cpfConsulta){
        if (!validarFormatoCPF(cpfConsulta)) {
            System.err.println("[Gerenciador] Formato de CPF inválido para consulta.");
            return null;
        }

        return veiculoDAO.buscarVeiculosPorCpf(cpfConsulta);
    }

    public List<Transferencia> consultarHistorico(String placaInput){
        String placaNormalizada = placaInput != null ? placaInput.toUpperCase().replace("-", "") : null;

        if (placaNormalizada == null || !(PlacaUtil.ehPlacaAntiga(placaNormalizada) || PlacaUtil.ehPlacaMercosul(placaNormalizada))) {
            System.err.println("[Gerenciador] Formato de placa inválido para consulta de histórico.");
            return null; // Ou lista vazia
        }

        return transferenciaDAO.buscarTransferenciasPorPlaca(placaNormalizada);
    }

    public List<ContagemVeiculosPorMarca> gerarRelatorioVeiculosPorMarca(){
        System.out.println("[Gerenciador] Gerando relatório de veículos por marca...");
        return veiculoDAO.contarVeiculosPorMarca();
    }

    public List<Transferencia> gerarRelatorioVeiculosTransferidosPeriodo(String dataInicioStr, String dataFimStr){
        LocalDate dataInicio = DataUtil.parseData(dataInicioStr);
        LocalDate dataFim = DataUtil.parseData(dataFimStr);

        if (dataInicio == null) {
            System.err.println("[Gerenciador] Data de início inválida para o relatório.");
            return null;
        }
        if (dataFim == null) {
            System.err.println("[Gerenciador] Data de fim inválida para o relatório.");
            return null;
        }

        if (dataInicio.isAfter(dataFim)) {
            System.err.println("[Gerenciador] A data de início não pode ser posterior à data de fim.");
            return null;
        }

        System.out.println("[Gerenciador] Gerando relatório de veículos transferidos de " +
                DataUtil.formatarData(dataInicio) + " até " + DataUtil.formatarData(dataFim)); //
        return transferenciaDAO.buscarTransferenciasPorPeriodo(dataInicio, dataFim);
    }

    public List<Veiculo> gerarRelatorioVeiculosPlacaAntiga(){
        System.out.println("[Gerenciador] Gerando relatório de veículos com placa antiga...");
        return veiculoDAO.buscarVeiculosComPlacaAntiga();
    }

    public boolean darBaixaVeiculo(String placaInput){
        System.out.println("\n[Gerenciador] Iniciando processo de baixa para a placa: " + placaInput);

        // 1. Normaliza a placa para a busca no DAO
        String placaNormalizada = placaInput != null ? placaInput.toUpperCase().replace("-", "") : null;
        if (placaNormalizada == null || placaNormalizada.trim().isEmpty()) {
            System.err.println("[Gerenciador] Placa não fornecida.");
            return false;
        }

        // 2. Verifica se o veículo existe antes de tentar a baixa
        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaNormalizada);
        if (veiculo == null) {
            System.err.println("[Gerenciador] Veículo com placa '" + placaNormalizada + "' não encontrado.");
            return false;
        }

        // 3. Verifica se o veículo já não está inativo
        if ("INATIVO".equalsIgnoreCase(veiculo.getStatus())) {
            System.err.println("[Gerenciador] O veículo com placa '" + veiculo.getPlaca() + "' já está baixado (inativo). Nenhuma ação foi tomada.");
            return false; // Retorna false para indicar que nenhuma alteração foi feita
        }

        // 4. Se o veículo existe e está ativo, proceder com a baixa no DAO
        System.out.println("[Gerenciador] Veículo encontrado e ativo. Prosseguindo com a baixa...");
        boolean sucessoNaBaixa = veiculoDAO.baixarVeiculo(placaNormalizada);

        if (sucessoNaBaixa) {
            System.out.println("[Gerenciador] Baixa do veículo com placa '" + veiculo.getPlaca() + "' realizada com sucesso no banco de dados.");
        } else {
            System.err.println("[Gerenciador] Ocorreu uma falha no DAO ao tentar dar baixa no veículo.");
        }

        return sucessoNaBaixa;
    }

}
