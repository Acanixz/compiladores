// Implementação do simbolo, utilizado para registros na tabela de simbolos
package GALS;

public class Simbolo {
    public String nome;
    public Integer tipo;
    public Escopo escopo;
    public boolean inicializada = false;
    public boolean usada = false;

    public boolean isParametro;
    public int parametroPosicao;
    boolean isRef; // É parametro por referência? (&)

    public boolean isVetor;
    public boolean isMatriz;
    public boolean isFuncao;

    public Simbolo(String nome, Integer tipo, Escopo escopo) {
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

    @Override
    public String toString() {
        return nome + " : " + getTipoComoString();
    }

    public String getTipoComoString() {
        if (tipo == null) return "N/A";
        switch (tipo) {
            case SemanticTable.INT:
                return "int";
            case SemanticTable.FLO:
                return "float";
            case SemanticTable.CHA:
                return "char";
            case SemanticTable.STR:
                return "string";
            case SemanticTable.BOO:
                return "bool";
            default:
                return "desconhecido(" + tipo + ")";
        }
    }
}
