package aplicacao;

import db.MarcaDAO;
import db.ModeloDAO;
import db.ProprietarioDAO;
import db.VeiculoDAO;
import entidades.Marca;
import entidades.Modelo;
import entidades.Proprietario;
import servicos.Gerenciador;

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

    // Método principal que executa o menu
    public void executarMenu() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerOpcao();
            processarOpcao(opcao);
        } while (opcao != 5);
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1 - Cadastro de veículos");
        System.out.println("2 - Transferência de propriedade");
        System.out.println("3 - Consulta de informações");
        System.out.println("4 - Relatórios");
        System.out.println("5 - Sair");
        System.out.print("Escolha uma opção: ");
    }

    private int lerOpcao() {
        int opcao = -1;
        while (opcao == -1) { // Loop até obter uma opção válida
            try {
                opcao = sc.nextInt(); // Lê apenas o número
                sc.nextLine(); // Consome a nova linha restante
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, digite um número.");
                sc.nextLine(); // Consome a entrada inválida (que não era número)
                opcao = -1; // Garante que o loop continue
            }
        }
        return opcao;
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                // Chama o método que encapsula o fluxo de cadastro
                executarCadastroVeiculo();
                break;
            case 2:
                System.out.println("Funcionalidade de Transferência ainda não implementada.");
                // Chamar método executarTransferenciaVeiculo();
                break;
            case 3:
                System.out.println("Funcionalidade de Consulta ainda não implementada.");
                // Chamar método executarConsultaVeiculo();
                break;
            case 4:
                System.out.println("Funcionalidade de Relatórios ainda não implementada.");
                // Chamar método executarRelatorios();
                break;
            case 5:
                System.out.println("Saindo do sistema...");
                break;
            default:
                if (opcao != -1) { // Evita mensagem de erro se a leitura falhou
                    System.out.println("OPÇÃO INVÁLIDA! Tente novamente.");
                }
                break;
        }
        // Pausa rápida para o usuário ler a saída antes do menu reaparecer (opcional)
        if (opcao != 5) {
            System.out.println("\nPressione Enter para continuar...");
        }
    }

    // --- Lógica de Cadastro (agora chamada pelo menu) ---
    private void executarCadastroVeiculo() {
        System.out.println("\n--- INICIANDO CADASTRO DE VEÍCULO ---");
        Marca marcaSelecionada = selecionarMarca();
        if (marcaSelecionada == null) return; //Pequisar o que é
        Modelo modeloSelecionado = selecionarModelo(marcaSelecionada);
        if (modeloSelecionado == null) return;
        String placa = obterPlacaValida();
        int ano = obterAnoValido();
        String cor = obterCorValida();
        String cpf = obterCPFValido();
        String nome = obterNomeProprietarioSeNecessario(cpf);
        if (nome == null) return;

        boolean sucesso = gerenciador.cadastrarVeiculo(placa, marcaSelecionada, modeloSelecionado, ano, cor, cpf, nome);
        if (sucesso) {
            System.out.println("\n--- CADASTRO REALIZADO COM SUCESSO! ---");
        } else {
            System.out.println("\n--- FALHA NO CADASTRO. Verifique os erros e tente novamente. ---");
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
        ProprietarioDAO propDAO = new ProprietarioDAO(); // Idealmente injetado no construtor do Program
        Proprietario existente = propDAO.buscarPorCPF(cpf);
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

    public static void main(String[] args) {
        // 1. Configurar os DAOs
        MarcaDAO marcaDAO = new MarcaDAO();
        ModeloDAO modeloDAO = new ModeloDAO();
        ProprietarioDAO proprietarioDAO = new ProprietarioDAO();
        VeiculoDAO veiculoDAO = new VeiculoDAO();

        // 2. Criar o Gerenciador com os DAOs
        Gerenciador gerenciador = new Gerenciador(marcaDAO, modeloDAO, proprietarioDAO, veiculoDAO);

        // 3. Criar a instância da Aplicação (Program)
        Program app = new Program(gerenciador);

        // 4. Executar o menu principal
        app.executarMenu();

        // Fechar o scanner ao final da aplicação
        sc.close();
        System.out.println("\nAplicação finalizada.");
    }

}
