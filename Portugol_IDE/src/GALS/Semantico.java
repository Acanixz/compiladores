package GALS;

import java.util.List;
import java.util.Objects;
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

    /// NOVO CÓDIGO DO BIP, CÓDIGO ACIMA MANTIDO APENAS POR GARANTIA, REMOVER NÃO UTILIZADOS APÓS CONCLUSÃO
    public String codigoBIP = ".data\n";
    Boolean lendoSecaoData = true;
    Boolean primeiroCode = true;
    String nome;
    String nome_id_atrib;
    String valor;
    Boolean flagOp = false;
    String oper;

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

    private void gera_cod(String nome, String valor){
        List<String> operadores = List.of("LD", "ADD", "SUB", "LDI", "ADDI", "SUBI");

        if (operadores.contains(nome)){
            if (primeiroCode){
                codigoBIP += "\n.text\n";
                primeiroCode = false;
            }
            codigoBIP += nome + " " + valor + "\n";
            lendoSecaoData = false;
        } else if (lendoSecaoData) {
            if (nome != null){
                codigoBIP += nome + ": " ;

                if (valor == null){
                    codigoBIP += "0\n";
                }
            }
            if (valor != null){
                codigoBIP += valor + "\n";
            }
        }
    }

    public void executeAction(int action, Token token)	throws SemanticError
    {
        System.out.println("Ação #" + action + ", Token: " + token);
        switch (action){
            case 1:
                nome = token.getLexeme();
                break;

            case 2:
                valor = token.getLexeme();
                break;

            case 3:
                gera_cod(nome, valor);
                valor = null;
                break;

            case 4:
                flagOp = true;
                oper = token.getLexeme();
                break;

            case 5:
                if (!flagOp){
                    gera_cod("LD", token.getLexeme());
                } else {
                    if (Objects.equals(oper, "+")){
                        gera_cod("ADD ", token.getLexeme());
                    }
                    if (Objects.equals(oper, "-")) {
                        gera_cod("SUB", token.getLexeme());
                    }
                }
                flagOp = false;
                break;
            case 6:
                if (!flagOp){
                    gera_cod("LDI", token.getLexeme());
                } else {
                    if (Objects.equals(oper, "+")){
                        gera_cod("ADDI ", token.getLexeme());
                    }
                    if (Objects.equals(oper, "-")) {
                        gera_cod("SUBI", token.getLexeme());
                    }
                }
                flagOp = false;
                break;

            case 21:
                nome_id_atrib = token.getLexeme();
                break;

            case 22:
                gera_cod("STO", nome_id_atrib);
                break;
        }
    }
}
