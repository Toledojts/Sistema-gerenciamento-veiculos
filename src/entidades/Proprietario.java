package entidades;

import java.util.ArrayList;
import java.util.List;

public class Proprietario {
    private String nome;
    private final String cpf;
    private List<Veiculo> posseVeiculos = new ArrayList<>();

    public Proprietario(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void removerVeiculoPropietario(Veiculo veiculoARemover){
        posseVeiculos.remove(veiculoARemover);
    }

    public void adicionarVeiculoProprietario(Veiculo veiculoAAdicionar){
        posseVeiculos.add(veiculoAAdicionar);
    }

    public String toString(){
        return nome;
    }
}
