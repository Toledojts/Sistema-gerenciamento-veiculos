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
import java.util.List;
import java.util.regex.Pattern;

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

        // 1. Validar Placa (Formato antigo ou Mercosul)
        if (!validarFormatoPlaca(placaInput)) {
            System.err.println("[Gerenciador] Erro: Formato da placa inválido.");
            return false;
        }

        if (verificarPlacaExistente(placaInput)){
            System.out.println("[Gerenciador] Erro: Placa já cadastrada!");
            return false;
        }

        // Normalizar placa para maiúsculas, por exemplo
        String placa = placaInput.toUpperCase();
        System.out.println("[Gerenciador] Placa validada: " + placa);

        // 2. Validar CPF (Formato 11 dígitos)
        if (!validarFormatoCPF(cpfProprietarioInput)) {
            System.err.println("[Gerenciador] Erro: Formato do CPF inválido (deve ter 11 dígitos).");
            return false;
        }
        // Já validado

        // 3. Tratar Proprietário
        Proprietario proprietario = proprietarioDAO.buscarPorCPF(cpfProprietarioInput);
        if (proprietario == null) {
            System.out.println("[Gerenciador] Proprietário com CPF " + cpfProprietarioInput + " não encontrado. Cadastrando novo...");
            // Validar nome (não pode ser vazio)
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

        // 4. Validar outros dados (básico)
        if (marca == null || modelo == null || ano <= 1900 || cor == null || cor.trim().isEmpty()) {
            System.err.println("[Gerenciador] Erro: Dados do veículo (marca, modelo, ano, cor) inválidos.");
            return false;
        }
        // Validar se modelo pertence à marca (extra)
        if (modelo.getMarca().getId() != marca.getId()) {
            System.err.println("[Gerenciador] Erro: Inconsistência - Modelo não pertence à Marca selecionada.");
            return false;
        }

        // 5. Criar Objeto Veiculo
        Veiculo novoVeiculo = new Veiculo(placa, marca, modelo, ano, cor.trim(), "ATIVO", proprietario);
        System.out.println("[Gerenciador] Objeto Veiculo pronto para salvar.");

        // 6. Salvar Veículo no Banco
        // Adicionar verificação se placa já existe seria ideal aqui antes de salvar
        boolean salvouVeiculo = veiculoDAO.salvar(novoVeiculo);
        if (!salvouVeiculo){
            System.err.println("[Gerenciador] Falha ao salvar o veículo. Verifique os logs do DAO.");
            return false;
        }
        System.out.println("[Gerenciador] Cadastro concluído com sucesso para placa: " + placa);
        return true; // Indica sucesso
    }

    //metodo para cadastro sem placa
    public boolean cadastrarVeiculo(Marca marca, Modelo modelo, int ano, String cor, String cpfProprietarioInput, String nomeProprietarioInput){
        System.out.println("[Gerenciador] Iniciando processo de novo emplacamento...");

        String novaPlaca;
        do {
            // CHAMANDO O NOVO GERADOR
            novaPlaca = GeradorPlacaUtil.gerarPlacaMercosul();
            System.out.println("[Gerenciador] Tentativa de placa gerada: " + novaPlaca);
        } while (verificarPlacaExistente(novaPlaca));

        System.out.println("[Gerenciador] Placa única '" + novaPlaca + "' gerada e validada.");

        // Chama o método de cadastro original com a placa gerada
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
        if (placa == null) return false;
        // Padrão Antigo: LLL-NNNN (L=Letra, N=Número)
        Pattern padraoAntigo = Pattern.compile("^[A-Z]{3}-\\d{4}$", Pattern.CASE_INSENSITIVE);
        // Padrão Mercosul: LLLNLNN
        Pattern padraoMercosul = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$", Pattern.CASE_INSENSITIVE);

        boolean valido = padraoAntigo.matcher(placa).matches() || padraoMercosul.matcher(placa).matches();
        return valido;
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

        // 1. Normalizar e Validar Placa do Veículo
        String placaNormalizada = placaVeiculoInput != null ? placaVeiculoInput.toUpperCase().replace("-", "") : null;
        if (placaNormalizada == null || !(PlacaUtil.ehPlacaAntiga(placaNormalizada) || PlacaUtil.ehPlacaMercosul(placaNormalizada))) {
            System.err.println("[Gerenciador] Formato de placa do veículo inválido ou não fornecida.");
            return false;
        }

        // 2. Validar CPF do Novo Proprietário
        if (!validarFormatoCPF(cpfNovoProprietarioInput)) { // Reutiliza sua validação de CPF
            System.err.println("[Gerenciador] Formato do CPF do novo proprietário inválido.");
            return false;
        }

        // 3. Validar e Converter Data da Transferência
        LocalDate dataTransferencia = DataUtil.parseData(dataTransferenciaStr); // Usando DataUtil
        if (dataTransferencia == null) {
            System.err.println("[Gerenciador] Data da transferência inválida.");
            return false;
        }
        // Opcional: Adicionar validação se a dataTransferencia é futura, etc.

        // 4. Buscar Veículo pelo DAO
        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaNormalizada);
        if (veiculo == null) {
            System.err.println("[Gerenciador] Veículo com placa " + placaNormalizada + " não encontrado.");
            return false;
        }

        // 5. Verificar se o novo proprietário é diferente do atual
        Proprietario proprietarioAnterior = veiculo.getProprietarioAtual();
        if (proprietarioAnterior != null && proprietarioAnterior.getCpf().equals(cpfNovoProprietarioInput)) {
            System.err.println("[Gerenciador] O novo proprietário (CPF: " + cpfNovoProprietarioInput + ") deve ser diferente do proprietário atual.");
            return false;
        }

        // 6. Buscar ou Cadastrar Novo Proprietário
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

        // 7. Converter Placa para Mercosul, se necessário
        String placaOriginalVeiculo = veiculo.getPlaca().toUpperCase().replace("-", ""); // Placa atual do veículo, limpa
        String placaFinalVeiculo = placaOriginalVeiculo; // Por padrão, a placa não muda

        if (PlacaUtil.ehPlacaAntiga(placaOriginalVeiculo)) {
            placaFinalVeiculo = PlacaUtil.converterPlacaAntigaParaMercosul(placaOriginalVeiculo);
            System.out.println("[Gerenciador] Placa antiga " + placaOriginalVeiculo + " convertida para Mercosul: " + placaFinalVeiculo + ".");
        }

        // 8. Atualizar Veículo no Banco de Dados (Proprietário e possivelmente a Placa)
        // Este método no DAO precisa lidar com a mudança da PK se a placa for alterada.
        // É crucial que a tabela 'veiculo' tenha 'ON UPDATE CASCADE' para a FK 'placa'
        // se outras tabelas (como 'transferencia' antiga) dependerem dela e a placa mudar.
        // Como estamos criando um *novo* registro de transferência, ele usará a 'placaFinalVeiculo'.
        boolean atualizouVeiculo = veiculoDAO.atualizarVeiculoParaTransferencia(placaOriginalVeiculo, placaFinalVeiculo, novoProprietario.getCpf());

        if (!atualizouVeiculo) {
            System.err.println("[Gerenciador] Falha ao atualizar os dados do veículo (proprietário/placa) no banco de dados.");
            // Considerar reverter o cadastro do novo proprietário se ele foi criado nesta transação? (Mais complexo, para um estudo pode ser opcional)
            return false;
        }
        System.out.println("[Gerenciador] Veículo (Placa antiga: " + placaOriginalVeiculo + " -> Placa nova: " + placaFinalVeiculo + ") atualizado com novo proprietário: " + novoProprietario.getNome() + ".");

        // Atualiza o objeto veículo em memória para refletir a mudança de placa (se houve) para o registro de transferência
        veiculo.setPlaca(placaFinalVeiculo);
        veiculo.setProprietarioAtual(novoProprietario);


        // 9. Criar e Salvar Registro de Transferência
        // O construtor de Transferencia foi atualizado para receber o Veiculo
        Transferencia novaTransferencia = new Transferencia(proprietarioAnterior, novoProprietario, dataTransferencia, veiculo);

        if (!transferenciaDAO.salvar(novaTransferencia)) {
            System.err.println("[Gerenciador] Falha crítica: Veículo foi atualizado, mas não foi possível registrar a transferência. Contate o suporte.");
            // Aqui você teria uma situação que pode exigir compensação manual ou uma transação mais robusta.
            // Para um projeto de estudo, registrar o erro é o mínimo.
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
        // A normalização deve ser consistente com a usada na busca principal
        String placaNormalizada = placaInput.toUpperCase().replace("-", "");

        // Validação de formato ANTES de ir ao DAO (opcional, mas bom)
        if (!(PlacaUtil.ehPlacaAntiga(placaNormalizada) || PlacaUtil.ehPlacaMercosul(placaNormalizada))) {
            System.err.println("[Gerenciador] Formato de placa inválido para busca: " + placaNormalizada);
            return null;
        }
        return veiculoDAO.buscarPorPlaca(placaNormalizada);
    }

    public List<Veiculo> consultarVeiculoPorCpf(String cpfConsulta){
        if (!validarFormatoCPF(cpfConsulta)) {
            System.err.println("[Gerenciador] Formato de CPF inválido para consulta.");
            return null; // Ou lançar uma exceção, ou retornar lista vazia controlada
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
            return null; // Ou new ArrayList<>() para indicar "nada encontrado devido a erro de input"
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

        // 1. Normalizar a placa para a busca no DAO
        String placaNormalizada = placaInput != null ? placaInput.toUpperCase().replace("-", "") : null;
        if (placaNormalizada == null || placaNormalizada.trim().isEmpty()) {
            System.err.println("[Gerenciador] Placa não fornecida.");
            return false;
        }

        // 2. Verificar se o veículo existe antes de tentar a baixa
        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaNormalizada);
        if (veiculo == null) {
            System.err.println("[Gerenciador] Veículo com placa '" + placaNormalizada + "' não encontrado.");
            return false;
        }

        // 3. Verificar se o veículo já está inativo
        if ("INATIVO".equalsIgnoreCase(veiculo.getStatus())) {
            System.err.println("[Gerenciador] O veículo com placa '" + veiculo.getPlaca() + "' já está baixado (inativo). Nenhuma ação foi tomada.");
            return false; // Retorna false para indicar que nenhuma alteração foi feita
        }

        // 4. Se o veículo existe e está ativo, proceder com a baixa no DAO
        System.out.println("[Gerenciador] Veículo encontrado e ativo. Prosseguindo com a baixa...");
        boolean sucessoNaBaixa = veiculoDAO.darBaixaVeiculo(placaNormalizada);

        if (sucessoNaBaixa) {
            System.out.println("[Gerenciador] Baixa do veículo com placa '" + veiculo.getPlaca() + "' realizada com sucesso no banco de dados.");
        } else {
            // A mensagem de erro específica do SQL já terá sido impressa pelo DAO
            System.err.println("[Gerenciador] Ocorreu uma falha no DAO ao tentar dar baixa no veículo.");
        }

        return sucessoNaBaixa;
    }

}
