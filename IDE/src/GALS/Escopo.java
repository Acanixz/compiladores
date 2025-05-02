package GALS;

public class Escopo {
    private String nome;
    private Escopo parent;

    public String getNome() {
        return nome;
    }

    public Escopo getParent() {
        return parent;
    }

    public Escopo() {
        this.nome = "Global";
        this.parent = null;
    }

    public Escopo(String nome, Escopo parent){
        this.nome = nome;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Escopo escopo = (Escopo) o;
        return nome.equals(escopo.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
