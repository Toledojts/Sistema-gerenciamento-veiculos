package entidades;

import java.util.ArrayList;
import java.util.List;

public class Veiculo {
    private String placa;
    private Marca marca;
    private Modelo modelo;
    private String cor;
    private int ano;
    private String padraoPlaca;
    private Proprietario proprietarioAtual;
    List<Transferencia> historicoTransfer = new ArrayList<>();

    public Veiculo (String placa, Marca marca, Modelo modelo, int ano, String cor, Proprietario proprietarioAtual) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.proprietarioAtual = proprietarioAtual;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Marca getMarca() {
        return marca;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getPadraoPlaca() {
        return padraoPlaca;
    }

    public void setPadraoPlaca(String padraoPlaca) {
        this.padraoPlaca = padraoPlaca;
    }

    public Proprietario getProprietarioAtual() {
        return proprietarioAtual;
    }

    public void setProprietarioAtual(Proprietario proprietarioAtual) {
        this.proprietarioAtual = proprietarioAtual;
    }

    public List<Transferencia> getHistoricoTransfer() {
        return historicoTransfer;
    }

    public void setHistoricoTransfer(List<Transferencia> historicoTransfer) {
        this.historicoTransfer = historicoTransfer;
    }

    public void transferirPropriedade(Proprietario novoProprietario){
        proprietarioAtual = novoProprietario;
    }

    public void adicionarTransferencia(Transferencia transferencia){
        historicoTransfer.add(transferencia);
    }


    public String toString(){
        return marca + " " + modelo + " " + ano + ", cor " + cor + " - " + placa + "\n" + "Proprietário: " + proprietarioAtual;
    }
}
