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
    private String asmDataSection = ".data\n";
    private String asmTextSection = ".text\n";
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

    // Junta as seções de declaração e código em uma string ASM legivel pelo Bipide 3.0
    // Download: https://sourceforge.net/projects/bipide/
    public String compilar_ASM(){
        return asmDataSection + "\n" + asmTextSection;
    }

    private void gera_cod(String nome, String valor){
        List<String> operadores = List.of("LD", "ADD", "SUB", "LDI", "ADDI", "SUBI", "STO");

        if (operadores.contains(nome)){
            /*if (primeiroCode){
                codigoBIP += "\n.text\n";
                primeiroCode = false;
            }*/
            asmTextSection += nome + " " + valor + "\n";
            lendoSecaoData = false;
        } else if (lendoSecaoData) {
            if (nome != null){
                asmDataSection += nome + ": " ;

                if (valor == null){
                    asmDataSection += "0\n";
                }
            }
            if (valor != null){
                asmDataSection += valor + "\n";
            }
        }
    }

    public void executeAction(int action, Token token)	throws SemanticError
    {
        System.out.println("Ação #" + action + ", Token: " + token);
        switch (action){
            // Nome p/ declaração de variaveis
            case 1:
                nome = token.getLexeme();
                break;

            // Valor p/ declaração de variaveis
            case 2:
                valor = token.getLexeme();
                break;

            // Geração de código (declaração de variaveis)
            case 3:
                gera_cod(nome, valor);
                valor = null;
                break;

            // Obtenção do operador na expressão + aciona flag p/ segundo ou outro operando
            case 4:
                flagOp = true;
                oper = token.getLexeme();
                break;

            // Geração de código dos operandos em uma expressão (origem: identificador)
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

            // Geração de código dos operandos em uma expressão (origem: immediate)
            case 6:
                if (!flagOp){
                    gera_cod("LDI", token.getLexeme());
                } else {
                    if (Objects.equals(oper, "+")){
                        gera_cod("ADDI", token.getLexeme());
                    }
                    if (Objects.equals(oper, "-")) {
                        gera_cod("SUBI", token.getLexeme());
                    }
                }
                flagOp = false;
                break;

            // Entrada de dados
            case 7:
                gera_cod("LD", "$in_port");
                gera_cod("STO", token.getLexeme());
                break;

            // Saida de dados (origem: variavel)
            // NOTA: NÃO IMPLEMENTADO NO MOMENTO
            // compare pg.14 de "Geração de Código 1" e expressão em Portugol.gals
            // para entender
            case 8:
                gera_cod("LD", token.getLexeme());
                gera_cod("STO", "$out_port");
                break;

            // Saida de dados (origem: immediate)
            case 9:
                gera_cod("LDI", token.getLexeme());
                gera_cod("STO", "$out_port");
                break;

            // Nome p/ atribuição em variavel
            case 21:
                nome_id_atrib = token.getLexeme();
                break;

            // Geração de código (atribuição de variavel)
            case 22:
                gera_cod("STO", nome_id_atrib);
                break;
        }
    }
}
