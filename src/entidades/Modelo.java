package entidades;

public class Modelo {
    private long id;
    private String nome;
    private Marca marca;

    public Modelo(long id, String nome, Marca marca) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Marca getMarca() {
        return marca;
    }

    public String toString() {
        return nome;
    }
}
