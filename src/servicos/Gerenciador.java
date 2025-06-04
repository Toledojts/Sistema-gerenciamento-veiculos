package servicos;

import db.MarcaDAO;
import db.ModeloDAO;
import db.ProprietarioDAO;
import db.VeiculoDAO;
import entidades.Marca;
import entidades.Modelo;
import entidades.Proprietario;
import entidades.Veiculo;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Gerenciador {
    Scanner sc = new Scanner(System.in);

    public Gerenciador() {
    }

    private MarcaDAO marcaDAO;
    private ModeloDAO modeloDAO;
    private ProprietarioDAO proprietarioDAO;
    private VeiculoDAO veiculoDAO;

    // Construtor para injeção dos DAOs
    public Gerenciador(MarcaDAO marcaDAO, ModeloDAO modeloDAO, ProprietarioDAO proprietarioDAO, VeiculoDAO veiculoDAO) {
        this.marcaDAO = marcaDAO;
        this.modeloDAO = modeloDAO;
        this.proprietarioDAO = proprietarioDAO;
        this.veiculoDAO = veiculoDAO;
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
            proprietario = new Proprietario(nomeProprietarioInput.trim(), cpfProprietarioInput); // Ordem corrigida!

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
        veiculoDAO.salvar(novoVeiculo);
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

    // Outros métodos do Gerenciador (transferir, baixar, consultar, etc.) viriam aqui
}
