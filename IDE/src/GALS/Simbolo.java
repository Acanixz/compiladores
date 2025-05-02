// Implementação do simbolo, utilizado para registros na tabela de simbolos
package GALS;

public class Simbolo {
    String nome;
    Tipo tipo;
    Escopo escopo;
    boolean inicializada = false;
    boolean usada = false;

    boolean isParametro;
    int parametroPosicao;
    boolean isRef; // É parametro por referência? (&)

    boolean isVetor;
    boolean isMatriz;
    boolean isFuncao;

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
