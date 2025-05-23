package GALS;

import java.util.Stack;

public class Semantico implements Constants
{
    IDE_Warnings logger = IDE_Warnings.getInstance();
    public Escopo escopoGlobal = new Escopo();
    public Escopo escopoAtual = escopoGlobal;

    Simbolo simboloAtual = null; // Simbolo sendo utilizado p/ atribuição
    private Stack<Integer> pilhaExpr = new Stack<>();

    private int actionPosition = -1;
    private int parameterPosition = -1;
    private boolean isFunctionHeader = false;
    private boolean ignoreAssign = false; // Atribuições não contam como variavel utilizada sem inicializar

    // NÃO ESQUECER DE ADICIONAR NOVAS VARIAVEIS AQUI
    public void reset() {
        logger.clearWarnsAndErrors();

        escopoGlobal = new Escopo();
        escopoAtual = escopoGlobal;

        pilhaExpr.clear();

        actionPosition = -1;
        parameterPosition = -1;
        isFunctionHeader = false;
        ignoreAssign = false;
    }

    public void executeAction(int action, Token token)	throws SemanticError
    {

    }	
}
