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


    // CAMPOS PARA VETORE
    public boolean isVetor;
    public int tamanhoVetor;

    public boolean isMatriz;
    public boolean isFuncao;

    /// NOVO CÓDIGO DO BIP, CÓDIGO ACIMA MANTIDO APENAS POR GARANTIA, REMOVER NÃO UTILIZADOS APÓS CONCLUSÃO
    public boolean isTemp;
    public boolean isLivre;


    public Simbolo(String nome, Integer tipo, Escopo escopo) {
        this.nome = nome;
        this.tipo = tipo;
        this.inicializada = false;
        this.usada = false;
        this.escopo = escopo;
        this.isParametro = false;
        this.parametroPosicao = -1;
        this.isRef = false;
        this.isMatriz = false;
        this.isFuncao = false;
        this.isTemp = false;
        this.isLivre = false;

        // --- INICIALIZAÇÃO  VETORES
        this.isVetor =false;
        this.tamanhoVetor=0;
    }

    @Override
    public String toString() {
        String base = nome + " : " + getTipoComoString();
        if (isVetor) {
            base += " (vetor[" + tamanhoVetor + "])"; // Adiciona a informação de vetor
        }
        if (isFuncao) {
            base += " (função)";
        }
        if (isTemp) {
            base += " (temp, livre=" + isLivre + ")";
        }
        //  adicionar mais informações se isMatriz ou isParametro for true
        return base;
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
