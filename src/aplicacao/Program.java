package aplicacao;

import db.*;
import entidades.*;
import relatorios.ContagemVeiculosPorMarca;
import servicos.Gerenciador;
import utilitarios.DataUtil;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;


public class Program {

    private final static Scanner sc = new Scanner(System.in); // Scanner estático para ser usado nos métodos
    private final Gerenciador gerenciador; // Instância do Gerenciador

    // Construtor recebe a instância do Gerenciador
    public Program(Gerenciador gerenciador) {
        this.gerenciador = gerenciador;
    }

    public void executarMenu() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerOpcao();
            processarOpcao(opcao);
        } while (opcao != 6);
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1 - Cadastro de veículos");
        System.out.println("2 - Transferência de propriedade");
        System.out.println("3 - Consulta de informações");
        System.out.println("4 - Relatórios");
        System.out.println("5 - Baixa de veículos");
        System.out.println("6 - Sair");
        System.out.print("Escolha uma opção: ");
    }

    private int lerOpcao() {
        int opcao = -1;
        while (opcao == -1) { // Loop até obter uma opção válida
            try {
                opcao = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, digite um número.");
                sc.nextLine();
                opcao = -1; // Garante que o loop continue
            }
        }
        return opcao;
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                executarCadastroVeiculo();
                break;
            case 2:
                executarTransferenciaVeiculo();
                break;
            case 3:
                executarMenuConsultaInformacoes();
                break;
            case 4:
                executarMenuRelatorios();
                break;
            case 5:
                executarBaixaVeiculo();
                break;
            case 6:
                System.out.println("Saindo...");
                break;
            default:
                if (opcao != -1) { // Evita mensagem de erro se a leitura falhou
                    System.out.println("OPÇÃO INVÁLIDA! Tente novamente.");
                }
                break;
        }
    }

    private void executarCadastroVeiculo() {
        System.out.println("\n--- INICIANDO CADASTRO DE VEÍCULO ---");
        System.out.println("1 - Novo Emplacamento (Gerar Placa Automática)");
        System.out.println("2 - Cadastrar Veículo com Placa Existente");
        System.out.println("3 - Voltar ao Menu Principal");
        System.out.print("Escolha uma opção: ");

        int opcao = lerOpcao();


        switch (opcao) {
            case 1:
                executarNovoEmplacamento();
                break;
            case 2:
                executarCadastroPlacaExistente();
                break;
            case 3:
                System.out.println("Retornando ao Menu Principal...");
                return; // Sai do método e volta ao menu principal
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    private void executarCadastroPlacaExistente(){
        System.out.println("\n--- CADASTRO DE VEÍCULO COM PLACA EXISTENTE ---");

        String placa = obterPlacaValida();

        // Reutiliza sua verificação de placa existente
        if (gerenciador.verificarPlacaExistente(placa)) {
            System.out.println("[ERRO] A placa '" + placa.toUpperCase() + "' já está cadastrada no sistema. Cadastro cancelado.");
            return;
        }

        // Coleta de dados: marca, modelo, ano, cor, proprietário...
        Marca marcaSelecionada = selecionarMarca();
        if (marcaSelecionada == null) return;

        Modelo modeloSelecionado = selecionarModelo(marcaSelecionada);
        if (modeloSelecionado == null) return;

        int ano = obterAnoValido();
        String cor = obterCorValida();
        String cpf = obterCPFValido();
        String nome = obterNomeProprietarioSeNecessario(cpf); // Este método já busca ou pede o nome
        if (nome == null) return;

        // Chama o método original do Gerenciador
        boolean sucesso = gerenciador.cadastrarVeiculo(placa, marcaSelecionada, modeloSelecionado, ano, cor, cpf, nome);

        if (sucesso) {
            System.out.println("\n--- CADASTRO REALIZADO COM SUCESSO! ---");
        } else {
            System.out.println("\n--- FALHA NO CADASTRO. Verifique os erros e tente novamente. ---");
        }
    }

    private void executarNovoEmplacamento(){
        System.out.println("\n--- NOVO EMPLACAMENTO ---");

        // Coleta todos os dados, EXCETO a placa
        Marca marcaSelecionada = selecionarMarca();
        if (marcaSelecionada == null) return;

        Modelo modeloSelecionado = selecionarModelo(marcaSelecionada);
        if (modeloSelecionado == null) return;

        int ano = obterAnoValido();
        String cor = obterCorValida();
        String cpf = obterCPFValido();
        String nome = obterNomeProprietarioSeNecessario(cpf);
        if (nome == null) return;

        // Chama o NOVO método sobrecarregado do Gerenciador (sem a placa)
        boolean sucesso = gerenciador.cadastrarVeiculo(marcaSelecionada, modeloSelecionado, ano, cor, cpf, nome);

        if (sucesso) {
            System.out.println("\n--- NOVO EMPLACAMENTO REALIZADO COM SUCESSO! ---");
            System.out.println("Uma nova placa foi gerada e associada ao veículo.");
        } else {
            System.out.println("\n--- FALHA NO NOVO EMPLACAMENTO. Verifique os erros e tente novamente. ---");
        }
    }

    private Marca selecionarMarca() {
        List<Marca> marcas = gerenciador.listarMarcasDisponiveis();
        if (marcas.isEmpty()) {
            System.out.println("ERRO: Nenhuma marca disponível no banco.");
            return null;
        }
        System.out.println("Selecione a Marca:");
        for (int i = 0; i < marcas.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, marcas.get(i));
        }
        while (true) {
            System.out.print("Opção Marca (número): ");
            try {
                int escolha = sc.nextInt(); // Lê número
                sc.nextLine(); // Consome \n
                if (escolha > 0 && escolha <= marcas.size()) { return marcas.get(escolha - 1); }
                else { System.out.println("Opção inválida."); }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Digite um número.");
                sc.nextLine(); // Consome entrada inválida
            }
        }
    }

    private Modelo selecionarModelo(Marca marca) {
        List<Modelo> modelos = gerenciador.listarModelosDisponiveis(marca);
        if (modelos.isEmpty()) {
            System.out.println("ERRO: Nenhum modelo para " + marca.getNome());
            return null;
        }
        System.out.println("Selecione o Modelo para " + marca.getNome() + ":");
        for (int i = 0; i < modelos.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, modelos.get(i));
        }
        while (true) {
            System.out.print("Opção Modelo (número): ");
            try {
                int escolha = sc.nextInt(); // 1. Lê APENAS o número
                sc.nextLine(); // 2. <<< ADICIONE ESTA LINHA para consumir o '\n' restante

                if (escolha > 0 && escolha <= modelos.size()) {
                    return modelos.get(escolha - 1);
                } else {
                    System.out.println("Opção inválida.");
                }
            } catch (java.util.InputMismatchException e) { // 3. Use InputMismatchException para nextInt()
                System.out.println("Entrada inválida. Digite um número.");
                sc.nextLine(); // 4. Consome a entrada inválida (que não era número)
            }
        }
    }

    private String obterPlacaValida() {
        while(true) {
            System.out.print("Digite a Placa (Ex: ABC-1234 ou BRA1A23): ");
            String placaInput = sc.nextLine();
            if (gerenciador.validarFormatoPlaca(placaInput)) {
                return placaInput;
            }
            else {
                System.out.println("Formato de placa inválido.");
            }
        }
    }

    private int obterAnoValido() {
        while (true) {
            System.out.print("Ano do veículo (4 dígitos): ");
            try {
                int ano = Integer.parseInt(sc.nextLine());
                if (ano > 1900 && ano < 2100) {
                    return ano;
                }
                else {
                    System.out.println("Ano inválido.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida.");
            }
        }
    }

    private String obterCorValida() {
        while(true) {
            System.out.print("Cor do veículo: ");
            String cor = sc.nextLine();
            if (cor != null && !cor.trim().isEmpty()) {
                return cor.trim();
            }
            else {
                System.out.println("Cor não pode ser vazia.");
            }
        }
    }

    private String obterCPFValido() {
        while (true) {
            System.out.print("CPF do proprietário (11 dígitos): ");
            String cpfInput = sc.nextLine();
            if (gerenciador.validarFormatoCPF(cpfInput)) {
                return cpfInput;
            }
            else {
                System.out.println("Formato de CPF inválido.");
            }
        }
    }

    private String obterNomeProprietarioSeNecessario(String cpf) {
        Proprietario existente = gerenciador.buscarProprietarioPorCPF(cpf);

        if (existente != null) {
            System.out.println("Proprietário encontrado: " + existente.getNome());
            return existente.getNome();
        } else {
            System.out.println("Proprietário não encontrado. Informe o nome:");
            while(true) {
                System.out.print("Nome completo do novo proprietário: ");
                String nome = sc.nextLine();
                if (nome != null && !nome.trim().isEmpty()) {
                    return nome.trim();
                }
                else {
                    System.out.println("Nome não pode ser vazio.");
                }
            }
        }
    }

    private void executarTransferenciaVeiculo() {
        System.out.println("\n--- INICIANDO TRANSFERÊNCIA DE PROPRIEDADE ---");

        System.out.print("Digite a placa do veículo a ser transferido: ");
        String placaVeiculoInput = sc.nextLine().trim();

        // === VERIFICAÇÃO INICIAL DO VEÍCULO ===
        Veiculo veiculoParaTransferir = gerenciador.buscarVeiculoPorPlacaMenu(placaVeiculoInput);

        if (veiculoParaTransferir == null) {
            System.out.println("Veículo com placa '" + placaVeiculoInput.toUpperCase() + "' não encontrado no sistema. Transferência cancelada.");
            return; // Interrompe a operação
        } else {
            System.out.println("Veículo encontrado: " +
                    (veiculoParaTransferir.getMarca() != null ? veiculoParaTransferir.getMarca().getNome() : "Marca Desconhecida") + " " +
                    (veiculoParaTransferir.getModelo() != null ? veiculoParaTransferir.getModelo().getNome() : "Modelo Desconhecido") +
                    ", Placa: " + veiculoParaTransferir.getPlaca() +
                    ", Proprietário Atual: " + (veiculoParaTransferir.getProprietarioAtual() != null ? veiculoParaTransferir.getProprietarioAtual().getNome() : "N/A"));
        }
        // === FIM DA VERIFICAÇÃO INICIAL DO VEÍCULO ===

        System.out.print("Digite o CPF do NOVO proprietário (11 dígitos): ");
        String cpfNovoProprietario = sc.nextLine().trim();

        String nomeNovoProprietario = null;
        Proprietario proprietarioExistente = gerenciador.buscarProprietarioPorCPF(cpfNovoProprietario);

        if (proprietarioExistente == null) {
            System.out.println("Proprietário com CPF " + cpfNovoProprietario + " não encontrado.");
            System.out.print("Digite o nome completo do NOVO proprietário para cadastro: ");
            nomeNovoProprietario = sc.nextLine().trim();
            if (nomeNovoProprietario.isEmpty()) {
                System.out.println("Nome não pode ser vazio para um novo cadastro. Transferência cancelada.");
                return;
            }
        } else {
            System.out.println("Proprietário encontrado: " + proprietarioExistente.getNome() + " (CPF: " + proprietarioExistente.getCpf() + ")");
        }

        System.out.print("Digite a data da transferência (formato dd/MM/yyyy): ");
        String dataTransferenciaStr = sc.nextLine().trim();

        // Agora, ao chamar o método principal de transferência no Gerenciador,
        // ele ainda fará suas próprias validações e busca do veículo para garantir
        // a consistência dos dados no momento da transação.
        // Passaremos a placa original que o usuário digitou.
        boolean sucesso = gerenciador.transferirPropriedade(
                placaVeiculoInput, // Passa a placa que o usuário digitou
                cpfNovoProprietario,
                nomeNovoProprietario,
                dataTransferenciaStr
        );

        if (sucesso) {
            System.out.println("\n--- TRANSFERÊNCIA DE PROPRIEDADE REALIZADA COM SUCESSO! ---");
        } else {
            // A mensagem de erro específica já terá sido impressa pelo Gerenciador ou DAOs
            System.out.println("\n--- FALHA NA TRANSFERÊNCIA DE PROPRIEDADE. ---");
        }
    }

    private void executarMenuConsultaInformacoes(){
        System.out.println("\n--- INICIANDO CONSULTA DE INFORMAÇÕES ---");
        System.out.println("1 - Consultar veículo por placa");
        System.out.println("2 - Consultar veículos por proprietário");
        System.out.println("3 - Consultar histórico de transferência de um veículo");
        System.out.println("4 - Retornar ao menu principal");
        System.out.print("Digite a opção desejada: ");
        int n = sc.nextInt();
        sc.nextLine();

        switch (n){
            case 1:
                System.out.print("Digite a placa do veículo: ");
                String placaParametro = sc.nextLine();

                Veiculo veiculoConsulta = gerenciador.buscarVeiculoPorPlacaMenu(placaParametro);

                if (veiculoConsulta != null) {
                    System.out.println("Veículo encontrado: ");
                    System.out.println(veiculoConsulta);
                }
                break;

            case 2:
                System.out.print("Digite o CPF do proprietário: ");
                String cpfConsulta = sc.nextLine();

                Proprietario proprietario = gerenciador.buscarProprietarioPorCPF(cpfConsulta);

                if (proprietario == null){
                    System.out.println("Proprietário não encontrado.");
                } else {
                    System.out.println("Veiculos cadastrados no nome de " + proprietario.getNome() + ": ");
                    List<Veiculo> listaVeiculosEncontrados = gerenciador.consultarVeiculoPorCpf(cpfConsulta);
                    System.out.println();
                    if (listaVeiculosEncontrados.isEmpty()){
                        System.out.println("Nenhum veículo encontrado.");
                    } else {
                        for (Veiculo obj : listaVeiculosEncontrados){
                            System.out.println(obj);
                            System.out.println();
                        }
                    }
                }
                break;

            case 3:
                System.out.print("Digite a placa do veículo: ");
                String placaInput = sc.nextLine();

                Veiculo veiculo = gerenciador.buscarVeiculoPorPlacaMenu(placaInput);

                if (veiculo == null){
                    System.out.println("Veículo não encontrado.");
                } else {
                    System.out.println("Veículo encontrado: ");
                    System.out.println(veiculo);

                    List<Transferencia> historico = gerenciador.consultarHistorico(placaInput);

                    if (historico.isEmpty()){
                        System.out.println("Histórico de transferências vazio.");
                    } else {
                        for (Transferencia obj : historico){
                            System.out.println(obj);
                        }
                    }
                }
                break;

            case 4:
                System.out.println("Retornando ao menu principal...");
                break;
            default:
                System.out.println("Opção inválida!");
                break;
        }
    }

    private void executarMenuRelatorios(){
        System.out.println("\n--- INICIANDO RELATÓRIOS ---");
        System.out.println("1 - Quantidade de veículos por marca");
        System.out.println("2 - Veículos transferidos em determinado período");
        System.out.println("3 - Veículos com placa antiga ainda não transferidos");
        System.out.println("4 - Retornar ao menu principal");
        System.out.print("Digite uma opção: ");
        int opcaoRelatorio = lerOpcao();

        switch (opcaoRelatorio){
            case 1:
                exibirRelatorioVeiculosPorMarca();
                break;
            case 2:
                exibirRelatorioVeiculosTransferidosPeriodo();
                break;
            case 3:
                exibirRelatorioVeiculosPlacaAntiga();
                break;
            case 4:
                System.out.println("Retornando ao Menu Principal...");
                break;
            default:
                System.out.println("Opção de relatório inválida!");
                break;
        }
    }

    public void exibirRelatorioVeiculosPorMarca(){
        System.out.println("\n--- RELATÓRIO: QUANTIDADE DE VEÍCULOS POR MARCA ---");
        List<ContagemVeiculosPorMarca> relatorio = gerenciador.gerarRelatorioVeiculosPorMarca();

        if (relatorio == null || relatorio.isEmpty()) {
            System.out.println("Nenhum dado encontrado.");
        } else {
            System.out.println("-----------------------------------------");
            System.out.printf("| %-25s | %-10s |\n", "MARCA", "QUANTIDADE");
            System.out.println("-----------------------------------------");
            for (ContagemVeiculosPorMarca item : relatorio) {
                System.out.printf("| %-25s | %-10d |\n", item.getNomeMarca(), item.getQuantidade());
            }
            System.out.println("-----------------------------------------");
        }
    }

    public void exibirRelatorioVeiculosTransferidosPeriodo(){
        System.out.println("\n--- RELATÓRIO: VEÍCULOS TRANSFERIDOS POR PERÍODO ---");
        System.out.print("Digite a data de INÍCIO do período (dd/MM/yyyy): ");
        String dataInicioStr = sc.nextLine().trim();
        System.out.print("Digite a data de FIM do período (dd/MM/yyyy): ");
        String dataFimStr = sc.nextLine().trim();

        List<Transferencia> relatorio = gerenciador.gerarRelatorioVeiculosTransferidosPeriodo(dataInicioStr, dataFimStr);

        if (relatorio == null) {
            // Mensagem de erro de data inválida já foi mostrada pelo Gerenciador
            System.out.println("Não foi possível gerar o relatório devido a datas inválidas.");
        } else if (relatorio.isEmpty()) {
            System.out.println("Nenhuma transferência encontrada para o período de " + dataInicioStr + " a " + dataFimStr + ".");
        } else {
            System.out.println("\n--- Transferências de " + dataInicioStr + " a " + dataFimStr + " ---");
            for (Transferencia t : relatorio) {
                Veiculo v = t.getVeiculo();
                System.out.println("--------------------------------------------------");
                System.out.println("Data da Transferência: " + DataUtil.formatarData(t.getDataTransferencia()));
                System.out.println("Veículo: " + (v != null ? v.getPlaca() : "N/A") +
                        " (" + (v != null && v.getMarca() != null ? v.getMarca().getNome() : "") +
                        " " + (v != null && v.getModelo() != null ? v.getModelo().getNome() : "") +
                        ", Ano: " + (v != null ? v.getAno() : "") +
                        ", Cor: " + (v != null ? v.getCor() : "") + ")");
                System.out.println("Proprietário Anterior: " +
                        (t.getAntigoProprietario() != null ? t.getAntigoProprietario().getNome() + " (CPF: " + t.getAntigoProprietario().getCpf() + ")" : "N/A"));
                System.out.println("Novo Proprietário: " +
                        (t.getNovoProprietario() != null ? t.getNovoProprietario().getNome() + " (CPF: " + t.getNovoProprietario().getCpf() + ")" : "N/A"));
            }
            System.out.println("--------------------------------------------------");
            System.out.println("Total de transferências no período: " + relatorio.size());
        }
    }

    public void exibirRelatorioVeiculosPlacaAntiga(){
        System.out.println("\n--- RELATÓRIO: VEÍCULOS COM PLACA ANTIGA (AINDA NÃO TRANSFERIDOS/CONVERTIDOS) ---");
        List<Veiculo> relatorio = gerenciador.gerarRelatorioVeiculosPlacaAntiga();

        if (relatorio == null || relatorio.isEmpty()) {
            System.out.println("Nenhum veículo com placa antiga encontrado ou ocorreu um erro na busca.");
        } else {
            System.out.println("-------------------------------------------------------------------------------");
            System.out.printf("| %-8s | %-10s | %-10s | %-4s | %-10s | %-25s |\n",
                    "PLACA", "MARCA", "MODELO", "ANO", "COR", "PROPRIETÁRIO ATUAL (CPF)");
            System.out.println("-------------------------------------------------------------------------------");
            for (Veiculo v : relatorio) {
                String nomeMarca = (v.getMarca() != null ? v.getMarca().getNome() : "N/A");
                String nomeModelo = (v.getModelo() != null ? v.getModelo().getNome() : "N/A");
                String nomeProp = (v.getProprietarioAtual() != null ? v.getProprietarioAtual().getNome() + " ("+ v.getProprietarioAtual().getCpf() +")" : "N/A");

                System.out.printf("| %-8s | %-10s | %-10s | %-4d | %-10s | %-25s |\n",
                        v.getPlaca(), nomeMarca, nomeModelo, v.getAno(), v.getCor(), nomeProp);
            }
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Total de veículos com placa antiga: " + relatorio.size());
        }
    }

    public void executarBaixaVeiculo(){
        System.out.println("\n--- BAIXA DE VEÍCULO ---");

        System.out.print("Digite a placa do veículo que deseja dar baixa: ");
        String placaInput = sc.nextLine().trim();

        // Chama o método do Gerenciador que contém toda a lógica
        boolean sucesso = gerenciador.darBaixaVeiculo(placaInput);

        // Fornece um feedback claro ao usuário com base no resultado
        if (sucesso) {
            System.out.println("\n--- VEÍCULO BAIXADO COM SUCESSO! ---");
            System.out.println("O veículo com placa '" + placaInput.toUpperCase() + "' foi marcado como INATIVO e seu proprietário foi desvinculado.");
        } else {
            System.out.println("\n--- FALHA NA OPERAÇÃO DE BAIXA. ---");
            System.out.println("Verifique se a placa está correta ou consulte as mensagens de erro acima.");
        }
    }

    public static void main(String[] args) {
        // 1. Configurar os DAOs
        MarcaDAO marcaDAO = new MarcaDAO();
        ModeloDAO modeloDAO = new ModeloDAO();
        ProprietarioDAO proprietarioDAO = new ProprietarioDAO();
        VeiculoDAO veiculoDAO = new VeiculoDAO();
        TransferenciaDAO transferenciaDAO = new TransferenciaDAO();

        // 2. Criar o Gerenciador com os DAOs
        Gerenciador gerenciador = new Gerenciador(marcaDAO, modeloDAO, proprietarioDAO, veiculoDAO, transferenciaDAO);

        System.out.println("--- Iniciando Sistema de Gerenciamento de Veículos ---");
        gerenciador.carregarDadosIniciais();

        // 3. Criar a instância da Aplicação (Program)
        Program app = new Program(gerenciador);

        // 4. Executar o menu principal
        app.executarMenu();

        // Fechar o scanner ao final da aplicação
        sc.close();
        System.out.println("\nAplicação finalizada.");
    }

}
