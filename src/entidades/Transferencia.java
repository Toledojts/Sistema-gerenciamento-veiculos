package entidades;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transferencia {
    private Proprietario antigoProprietario;
    private Proprietario novoProprietario;
    private LocalDate dataTransferencia;
    private Veiculo veiculo;

    public Transferencia(Proprietario antigoProprietario, Proprietario novoProprietario, LocalDate dataTransferencia, Veiculo veiculo) {
        this.antigoProprietario = antigoProprietario;
        this.novoProprietario = novoProprietario;
        this.dataTransferencia = dataTransferencia;
        this.veiculo = veiculo;
    }

    public Proprietario getAntigoProprietario() {
        return antigoProprietario;
    }

    public void setAntigoProprietario(Proprietario antigoProprietario) {
        this.antigoProprietario = antigoProprietario;
    }

    public Proprietario getNovoProprietario() {
        return novoProprietario;
    }

    public void setNovoProprietario(Proprietario novoProprietario) {
        this.novoProprietario = novoProprietario;
    }

    public LocalDate getDataTransferencia() {
        return dataTransferencia;
    }

    public void setDataTransferencia(LocalDate dataTransferencia) {
        this.dataTransferencia = dataTransferencia;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public String toString(){
        return "Veículo: " + veiculo.getPlaca() +" (" + veiculo.getMarca() + " " + veiculo.getModelo() +
                ", Ano: " + veiculo.getAno()+ ", Cor: " + veiculo.getCor() + ")\n" +
                "Antigo proprietário: " + antigoProprietario + "\n" + "Novo proprietário: " + novoProprietario
                + "\n" + "Data da transferência: " + dataTransferencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

}
