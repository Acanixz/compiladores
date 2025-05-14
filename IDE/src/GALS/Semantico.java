package GALS;

import java.util.HashMap;
import java.util.Stack;

public class Semantico implements Constants {
    IDE_Warnings logger = IDE_Warnings.getInstance();
    public Escopo escopoGlobal = new Escopo();
    public Escopo escopoAtual = escopoGlobal;

    Simbolo simboloAtual = null; // Simbolo sendo utilizado p/ atribuição
    private int tipoAtual = -1;   // tipo da declaração atual
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

        tipoAtual = -1;
        pilhaExpr.clear();

        actionPosition = -1;
        parameterPosition = -1;
        isFunctionHeader = false;
        ignoreAssign = false;
    }

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
                tipoAtual = tipoAtual_tmp == null ? -1 : (int) tipoAtual_tmp;
                if (tipoAtual == -1) {
                    logger.addError("Tipo não reconhecido: " + token.getLexeme(), actionPosition, token.getLexeme());
                    return;
                }
                break;

            case 2:
                String lexema = token.getLexeme();

                if (lexema.equals("true") || lexema.equals("false")) {
                    System.out.println("Tipo: BOOL_LITERAL");
                    pilhaExpr.push(SemanticTable.BOO);
                } else if (lexema.matches("-?\\d+")) {
                    System.out.println("Tipo: INT_LITERAL");
                    pilhaExpr.push(SemanticTable.INT);
                } else if (lexema.matches("-?\\d+\\.\\d+([fFdD]?)")) {
                    System.out.println("Tipo: FLOAT_LITERAL");
                    pilhaExpr.push(SemanticTable.FLO);
                } else if (lexema.matches("'(\\\\[btnfr\"'\\\\]|[^\\\\])'")) {
                    System.out.println("Tipo: CHAR_LITERAL");
                    pilhaExpr.push(SemanticTable.CHA);
                } else if (lexema.matches("\"(\\\\[btnfr\"'\\\\]|[^\"\\\\])*\"")) {
                    System.out.println("Tipo: STRING_LITERAL");
                    pilhaExpr.push(SemanticTable.STR);
                } else {
                    System.out.println("Literal não reconhecido na ação #2: " + lexema);
                    pilhaExpr.push(SemanticTable.ERR); // Empilha erro, se quiser tratar depois
                }
                break;

            case 3:
                if (token.getLexeme().equals("+")) {
                    pilhaExpr.push(SemanticTable.SUM);
                } else { // "-"
                    pilhaExpr.push(SemanticTable.SUB);
                }
                break;

            // TIMES
            case 4:
                pilhaExpr.push(SemanticTable.MUL);
                break;

            // DIVIDE
            case 5:
                pilhaExpr.push(SemanticTable.DIV);
                break;

            // MODULO
            case 6:
                pilhaExpr.push(SemanticTable.MOD);
                break;

            // Relacionais, bitwise e lógicos — todos caem em REL (igual, !=, >, <, >=, <=, &&, ||, |, ^, &)
            case 7:
                pilhaExpr.push(SemanticTable.REL);
                break;

            // LEFT_SHIFT / RIGHT_SHIFT — não há ID na SemanticTable
            case 10:
                if (token.getLexeme().equals("<<")) {
                    pilhaExpr.push(SemanticTable.SHL);
                } else {
                    pilhaExpr.push(SemanticTable.SHR);
                }
                break;

            // Chamada de vetor
            case 11:
                usarVariavel(token.getLexeme());
                break;

            // Declaração de vetor
            case 12:
                simboloAtual = criarVariavel(token.getLexeme(), tipoAtual);
                escopoAtual.buscarSimbolo(token.getLexeme()).isVetor = true;
                break;

            // Declaração de variavel
            case 15:
                simboloAtual = criarVariavel(token.getLexeme(), tipoAtual);
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
                if (simboloAtual != null)
                    simboloAtual.inicializada = true;
                simboloAtual = null;
                break;

            // Declaração de função
            case 21:
                simboloAtual = criarVariavel(token.getLexeme(), tipoAtual);
                simboloAtual.isFuncao = true;
                if (simboloAtual != null)
                    simboloAtual.inicializada = true;
                break;

            // Abre escopo (Parametros da função)
            case 22:
                enterEscopo();
                isFunctionHeader = true;
                break;

            // Fim da declaração de parametros (função)
            case 23:
                parameterPosition = -1;
                break;

            // Declaração de parametro
            case 24:
                criarVariavel(token.getLexeme(), tipoAtual);
                escopoAtual.buscarSimbolo(token.getLexeme()).isParametro = true;

                parameterPosition += 1;
                escopoAtual.buscarSimbolo(token.getLexeme()).parametroPosicao = parameterPosition;
                break;

            // Declaração de parametro (vetor)
            case 25:
                criarVariavel(token.getLexeme(), tipoAtual);
                escopoAtual.buscarSimbolo(token.getLexeme()).isParametro = true;
                escopoAtual.buscarSimbolo(token.getLexeme()).isVetor = true;

                parameterPosition += 1;
                escopoAtual.buscarSimbolo(token.getLexeme()).parametroPosicao = parameterPosition;
                break;

            case 26:
                usarFuncao(token.getLexeme());
                break;

            // Entrada de escopo (switch)
            case 53:
                enterEscopo();
                break;

            // Saida de escopo (switch)
            case 54:
                exitEscopo();
                break;

            case 62:
                ignoreAssign = true;
                simboloAtual = usarVariavel(token.getLexeme());
                break;

            case 64:
                int tipo = obterTipoVariavel(token.getLexeme());
                pilhaExpr.push(tipo);
                break;

            // Entrada de escopo (geral)
            case 67:
                // Funções inicializam o escopo mais cedo devido aos parametros
                if (isFunctionHeader) {
                    isFunctionHeader = false;
                    return;
                }
                enterEscopo();
                break;

            // Saida de escopo (geral)
            case 68:
                exitEscopo();
                break;

            // Atribuição de variaveis
            case 69:
                int tipoRecebido;

                if (!pilhaExpr.isEmpty()) {
                    tipoRecebido = pilhaExpr.pop();
                } else {
                    // Mesma lógica de inferência do case 2
                    String lex = token.getLexeme();
                    if (lex.equals("true") || lex.equals("false")) {
                        tipoRecebido = SemanticTable.BOO;
                    } else if (lex.matches("-?\\d+")) {
                        tipoRecebido = SemanticTable.INT;
                    } else if (lex.matches("-?\\d+\\.\\d+([fFdD]?)")) {
                        tipoRecebido = SemanticTable.FLO;
                    } else if (lex.matches("'(\\\\[btnfr\"'\\\\]|[^\\\\])'")) {
                        tipoRecebido = SemanticTable.CHA;
                    } else if (lex.matches("\"(\\\\[btnfr\"'\\\\]|[^\"\\\\])*\"")) {
                        tipoRecebido = SemanticTable.STR;
                    } else {
                        logger.addError("Não foi possível inferir o tipo do valor: " + lex, actionPosition, token.getLexeme());
                        return;
                    }
                }

                // Usa a tabela de atribuição: destino = tipoAtual, origem = tipoRecebido
                int comp = SemanticTable.atribType(tipoAtual, tipoRecebido);
                if (comp == SemanticTable.ERR) {
                    logger.addError("Atribuição inválida: não é possível atribuir "
                            + tipoToString(tipoRecebido)
                            + " a variável do tipo "
                            + tipoToString(tipoAtual), actionPosition, token.getLexeme());
                    return;
                } else if (comp == SemanticTable.WAR) {
                    logger.addWarning("Warning semântico: atribuição de "
                            + tipoToString(tipoRecebido)
                            + " a "
                            + tipoToString(tipoAtual)
                            + " pode resultar em perda de informação.", actionPosition, token.getLexeme());
                }

                if (simboloAtual != null)
                    simboloAtual.inicializada = true;
                simboloAtual = null;
                break;


            case 70:
                // desempilha na ordem: tipoDireita, operador, tipoEsquerda
                int tipoDireita = pilhaExpr.pop();
                int operador    = pilhaExpr.pop();
                int tipoEsquerda= pilhaExpr.pop();

                // obtém resultado: resultType(tipoEsquerda, tipoDireita, operador)
                int resultado = SemanticTable.resultType(tipoEsquerda, tipoDireita, operador);
                if (resultado == SemanticTable.ERR) {
                    logger.addError("Operação inválida entre tipos "
                            + tipoToString(tipoEsquerda)
                            + " e "
                            + tipoToString(tipoDireita), actionPosition, token.getLexeme());
                    return;
                }
                pilhaExpr.push(resultado);
                break;

            // —— Case 71: negação unária ——
            case 71:
                int tipoUn;
                if (!pilhaExpr.isEmpty()) {
                    tipoUn = pilhaExpr.pop();
                } else {
                    // infere como no case 2
                    String lex = token.getLexeme();
                    if (lex.equals("true") || lex.equals("false")) {
                        tipoUn = SemanticTable.BOO;
                    } else if (lex.matches("-?\\d+")) {
                        tipoUn = SemanticTable.INT;
                    } else if (lex.matches("-?\\d+\\.\\d+([fFdD]?)")) {
                        tipoUn = SemanticTable.FLO;
                    } else {
                        logger.addError("Não foi possível inferir tipo para negação de: " + lex, actionPosition, token.getLexeme());
                        return;
                    }
                }
                // só int, float ou bool
                if (tipoUn == SemanticTable.INT
                        || tipoUn == SemanticTable.FLO
                        || tipoUn == SemanticTable.BOO) {
                    // tipo pós-negação é o mesmo
                    pilhaExpr.push(tipoUn);
                } else {
                    logger.addError("Negação aplicada em tipo inválido: " + tipoToString(tipoUn), actionPosition, token.getLexeme());
                    return;
                }
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

    private Simbolo criarVariavel(String nome, Integer tipo) throws SemanticError {
        if (escopoAtual.getSimbolos().containsKey(nome)) {
            logger.addError("Variável já declarada no mesmo escopo: " + nome, actionPosition, nome);
            return null;
        }
        Simbolo s = new Simbolo(nome, tipo, escopoAtual);
        s.inicializada = false;
        escopoAtual.getSimbolos().put(nome, s);
        return s;
    }

    private Simbolo usarVariavel(String nome) {
        if (ignoreAssign) return null;
        Simbolo simbolo = escopoAtual.buscarSimbolo(nome);
        if (simbolo == null) {
            logger.addError("Variável não declarada: " + nome, actionPosition, nome);
            return null;
        }
        simbolo.usada = true;

        if (!simbolo.inicializada) {
            logger.addWarning("Uso de variável não inicializada: " + nome, actionPosition, nome);
        }
        return simbolo;
    }

    private int obterTipoVariavel(String nome){
        Simbolo simbolo = usarVariavel(nome);
        if (simbolo != null){
            return simbolo.tipo;
        }
        return -1;
    }

    private void usarFuncao(String nome) {
        Simbolo simbolo = escopoAtual.buscarSimbolo(nome);
        if (simbolo == null) {
            logger.addError("Função não declarada: " + nome, actionPosition, nome);
            return;
        }

        if (!simbolo.isFuncao){
            logger.addError("Variável não é uma função: " + nome, actionPosition, nome);
            return;
        }
        simbolo.usada = true;
    }

    private String tipoToString(int tipo) {
        switch (tipo) {
            case SemanticTable.INT: return "int";
            case SemanticTable.FLO: return "float";
            case SemanticTable.CHA: return "char";
            case SemanticTable.STR: return "string";
            case SemanticTable.BOO: return "bool";
            default: return "desconhecido";
        }
    }

}
