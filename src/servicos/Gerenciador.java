package servicos;

import db.*;
import entidades.*;
import utilitarios.DataUtil;
import utilitarios.PlacaUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class Gerenciador {

    public Gerenciador() {
    }

    private MarcaDAO marcaDAO;
    private ModeloDAO modeloDAO;
    private ProprietarioDAO proprietarioDAO;
    private VeiculoDAO veiculoDAO;
    private TransferenciaDAO transferenciaDAO;

    // Construtor para injeção dos DAOs
    public Gerenciador(MarcaDAO marcaDAO, ModeloDAO modeloDAO, ProprietarioDAO proprietarioDAO, VeiculoDAO veiculoDAO, TransferenciaDAO transferenciaDAO) {
        this.marcaDAO = marcaDAO;
        this.modeloDAO = modeloDAO;
        this.proprietarioDAO = proprietarioDAO;
        this.veiculoDAO = veiculoDAO;
        this.transferenciaDAO = transferenciaDAO;
    }

    // Métodos para obter listas para a camada de aplicação
    public List<Marca> listarMarcasDisponiveis() {
        return marcaDAO.listarTodas();
    }

    public List<Modelo> listarModelosDisponiveis(Marca marca) {
        return modeloDAO.listarPorMarca(marca);
    }

    // Método principal de cadastro
    public boolean cadastrarVeiculo(String placaInput, Marca marca, Modelo modelo, int ano, String cor, String cpfProprietarioInput, String nomeProprietarioInput) {
        System.out.println("\n[Gerenciador] Iniciando processo de cadastro...");

        // 1. Validar Placa (Formato antigo ou Mercosul)
        if (!validarFormatoPlaca(placaInput)) {
            System.err.println("[Gerenciador] Erro: Formato da placa inválido.");
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
        Veiculo novoVeiculo = new Veiculo(placa, marca, modelo, ano, cor.trim(), proprietario);
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

    // --- Métodos de Validação Internos ---

    public boolean validarFormatoPlaca(String placa) {
        if (placa == null) return false;
        // Padrão Antigo: LLL-NNNN (L=Letra, N=Número)
        Pattern padraoAntigo = Pattern.compile("^[A-Z]{3}-\\d{4}$", Pattern.CASE_INSENSITIVE);
        // Padrão Mercosul: LLLNLNN
        Pattern padraoMercosul = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$", Pattern.CASE_INSENSITIVE);

        boolean valido = padraoAntigo.matcher(placa).matches() || padraoMercosul.matcher(placa).matches();
        System.out.printf("[Gerenciador] Validando placa %s: %s%n", placa, (valido ? "Válido" : "Inválido"));
        return valido;
    }

    public boolean validarFormatoCPF(String cpf) {
        if (cpf == null) return false;
        // Verifica se tem exatamente 11 dígitos numéricos
        boolean valido = cpf.matches("^\\d{11}$");
        System.out.println("[Gerenciador] Validando CPF " + cpf + ": " + (valido ? "Válido (11 dígitos)" : "Inválido"));
        return valido;
    }

    public Proprietario buscarProprietarioPorCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty() || !validarFormatoCPF(cpf)) { // Validação básica
            // System.err.println("[Gerenciador] Tentativa de busca por CPF com formato inválido ou vazio."); // Log opcional
            return null;
        }
        return proprietarioDAO.buscarPorCPF(cpf); // Reutiliza o método do DAO
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

    // Outros métodos do Gerenciador (transferir, baixar, consultar, etc.) viriam aqui
}
