package GALS;

import java.util.HashMap;

public class Semantico implements Constants {
    public Escopo escopoGlobal = new Escopo();
    public Escopo escopoAtual = escopoGlobal;

    private int tipoAtual;   // tipo da declaração atual
    private int tipoExpr;    // tipo da expressão sendo avaliada
    private int[] pilhaExpr;

    private int actionPosition = -1;

    private static final HashMap<String, Integer> mapaTipos = new HashMap<>();
    static {
        mapaTipos.put("int", SemanticTable.INT);
        mapaTipos.put("float", SemanticTable.FLO);
        mapaTipos.put("bool", SemanticTable.BOO);
        mapaTipos.put("char", SemanticTable.CHA);
        mapaTipos.put("string", SemanticTable.STR);
    }

    // Interpretação das tokens
    public void executeAction(int action, Token token, int position) throws SemanticError {
        actionPosition = position;
        System.out.println("Ação #" + action + ", Token: " + token);
        switch (action){
            // Define tipo
            case 1:
                Object tipoAtual_tmp = mapaTipos.get(token.getLexeme());
                tipoAtual = tipoAtual_tmp == null ? -1 : tipoAtual;
                if (tipoAtual == -1) {
                    throw new SemanticError("Tipo não reconhecido: " + token.getLexeme());
                }
                break;

            // Chamada de vetor
            case 11:
                usarVariavel(token.getLexeme());
                break;

            // Declaração de vetor
            case 12:
                declareVariavel(token.getLexeme(), tipoAtual);
                escopoAtual.buscarSimbolo(token.getLexeme()).isVetor = true;
                break;

            // Declaração de variavel
            case 15:
                declareVariavel(token.getLexeme(), tipoAtual);
                break;

            // Abre escopo (Vetor)
            case 16:
                enterEscopo();
                break;

            // Fecha escopo (Vetor)
            case 17:
                exitEscopo();
                break;

            // Abre escopo (Valor no vetor)
            case 18:
                enterEscopo();
                break;

            // Fecha escopo (Valor no vetor)
            case 19:
                exitEscopo();
                break;

            // Entrada de escopo (switch)
            case 53:
                enterEscopo();
                break;

            // Saida de escopo (switch)
            case 54:
                exitEscopo();
                break;

            // Entrada de escopo (geral)
            case 67:
                enterEscopo();
                break;

            // Saida de escopo (geral)
            case 68:
                exitEscopo();
                break;
        }
    }

    private void enterEscopo() {
        // gera nome aleatório para escopo
        int proximoInt = escopoAtual.children.size() + 1;
        String nomeEscopo = escopoAtual.getNome() + "_" + proximoInt;
        Escopo novoEscopo = new Escopo(nomeEscopo, escopoAtual);
        escopoAtual.children.add(novoEscopo);
        escopoAtual = novoEscopo;
    }

    private void exitEscopo() {
        escopoAtual = escopoAtual.getParent();
    }

    private void declareVariavel(String nome, Integer tipo) throws SemanticError {
        if (escopoAtual.getSimbolos().containsKey(nome)) {
            throw new SemanticError("Variável já declarada no mesmo escopo: " + nome, actionPosition);
        }
        Simbolo s = new Simbolo(nome, tipo, escopoAtual);
        s.inicializada = false;
        escopoAtual.getSimbolos().put(nome, s);
    }

    private void usarVariavel(String nome) throws SemanticError {
        Simbolo simbolo = escopoAtual.buscarSimbolo(nome);
        if (simbolo == null) {
            throw new SemanticError("Variável não declarada: " + nome, actionPosition);
        }
        simbolo.usada = true;
    }
}
