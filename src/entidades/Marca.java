package entidades;

public class Marca {
    private long id;
    private String nome;

    public Marca(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String toString(){
        return nome;
    }
}
