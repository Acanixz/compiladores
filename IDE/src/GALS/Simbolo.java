// Implementação do simbolo, utilizado para registros na tabela de simbolos
package GALS;

public class Simbolo {
    public String nome;
    public Tipo tipo;
    public Escopo escopo;
    public boolean inicializada = false;
    public boolean usada = false;

    public boolean isParametro;
    public int parametroPosicao;
    boolean isRef; // É parametro por referência? (&)

    public boolean isVetor;
    public boolean isMatriz;
    public boolean isFuncao;

    public Simbolo(String nome, Tipo tipo, Escopo escopo) {
        this.nome = nome;
        this.tipo = tipo;
        this.inicializada = false;
        this.usada = false;
        this.escopo = escopo;
        this.isParametro = false;
        this.parametroPosicao = -1;
        this.isRef = false;
        this.isVetor = false;
        this.isMatriz = false;
        this.isFuncao = false;
    }
}
