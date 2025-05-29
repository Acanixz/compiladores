package GALS;

import java.util.ArrayList;
import java.util.HashMap;

public class Escopo {
    private String nome;
    private Escopo parent;
    private HashMap<String, Simbolo> simbolos = new HashMap<String, Simbolo>();

    public ArrayList<Escopo> children = new ArrayList<Escopo>();
    public String getNome() {
        return nome;
    }

    public Escopo getParent() {
        return parent;
    }

    public HashMap<String, Simbolo> getSimbolos() {
        return simbolos;
    }

    public Simbolo buscarSimbolo(String nome) {
        Escopo escopo = this;
        while (escopo != null) {
            if (escopo.simbolos.containsKey(nome)) {
                return escopo.simbolos.get(nome);
            }
            escopo = escopo.parent;
        }
        return null; // não encontrou
    }

    /// Busca por um simbolo temporario livre
    public Simbolo buscarTempLivre() {
        Escopo escopo = this;
        while (escopo != null) {
            for (Simbolo simb : escopo.simbolos.values()){
                if (simb.isTemp && simb.isLivre){
                    return simb;
                }
            }
            escopo = escopo.parent;
        }
        return null; // não encontrou
    }

    /// Retorna contagem de vars temporarias no escopo
    public int getTempCount(){
        int count = 0;
        Escopo escopo = this;
        for (Simbolo simb : escopo.simbolos.values()){
            if (simb.isTemp){
                count += 1;
            }
        }
        return count;
    }

    public Escopo() {
        this.nome = "Global";
        this.parent = null;
        this.simbolos = new HashMap<>();
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
