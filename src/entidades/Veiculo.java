package entidades;



public class Veiculo {
    private String placa;
    private Marca marca;
    private Modelo modelo;
    private String cor;
    private int ano;
    private String status;
    private Proprietario proprietarioAtual;

    public Veiculo (String placa, Marca marca, Modelo modelo, int ano, String cor, String status, Proprietario proprietarioAtual) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.proprietarioAtual = proprietarioAtual;
        this.status = status;
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

    public Proprietario getProprietarioAtual() {
        return proprietarioAtual;
    }

    public void setProprietarioAtual(Proprietario proprietarioAtual) {
        this.proprietarioAtual = proprietarioAtual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString(){
        return marca + " " + modelo + " " + ano + ", Cor: " + cor + " - " + placa + "\n" + "Proprietário: " + proprietarioAtual;
    }
}
