package entidades;

import java.util.ArrayList;
import java.util.List;

public class Veiculo {
    private String placa;
    private String marca;
    private String modelo;
    private String cor;
    private int ano;
    private String padraoPlaca;
    private Proprietario proprietarioAtual;
    List<Transferencia> historicoTransfer = new ArrayList<>();

    public Veiculo(String placa, String marca, String modelo, String cor, int ano, Proprietario proprietarioAtual) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.cor = cor;
        this.ano = ano;
        this.proprietarioAtual = proprietarioAtual;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
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
