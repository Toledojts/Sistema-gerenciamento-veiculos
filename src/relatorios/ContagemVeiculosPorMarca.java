package relatorios;

public class ContagemVeiculosPorMarca {
    private String nomeMarca;
    private int quantidade;

    public ContagemVeiculosPorMarca(String nomeMarca, int quantidade) {
        this.nomeMarca = nomeMarca;
        this.quantidade = quantidade;
    }

    public String getNomeMarca() {
        return nomeMarca;
    }

    public void setNomeMarca(String nomeMarca) {
        this.nomeMarca = nomeMarca;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString(){
        return "Marca: " + nomeMarca + " , Quantidade de veículos: " + quantidade;
    }
}
