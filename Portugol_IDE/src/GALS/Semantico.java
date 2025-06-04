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
    private String asmTempDataSection = "";
    private String asmTextSection = ".text\n";
    Boolean lendoSecaoData = true;
    Boolean primeiroCode = true;
    String nome;
    String nome_id_atrib;
    String valor;
    Boolean flagOp = false;
    String oper;
    Boolean variavelVetor = false;
    String indiceVetor;

    // --- NOVA FLAG ---
    private boolean isDeclarandoVetor = false; // Novo campo
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
        return asmDataSection + "\n" + asmTempDataSection + "\n" + asmTextSection;
    }

    private void gera_cod(String nome, String valor){
        List<String> operadores = List.of("LD", "ADD", "SUB", "AND", "XOR", "OR", "LDI", "ADDI", "SUBI", "ANDI", "XORI", "ORI", "STO", "STOV");

        if (operadores.contains(nome)){
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
                // OBS: BIP usa apenas integers, tipo sempre 0
                /*criarVariavel(nome, 0);
                gera_cod(nome, valor);
                valor = null;
                break;*/
                // Verifica se o 'nome' (ID da declaração) já foi processado como vetor pelo case 30
                if(nome != null){ // Garante que há um nome para processar

                    if(!isDeclarandoVetor){
                        // É uma declaração de variável simples (não foi tratada pelo case 30)
                        Simbolo s =criarVariavel(nome, 0);// OBS: BIP usa apenas integers, tipo sempre 0
                        if(s != null && valor != null){ // Se houver valor inicial (ID <- NUM_INT)
                            gera_cod("LDI", valor);
                            gera_cod("STO", nome); // Ou s.nome, para usar o nome do símbolo VER COM O HERIQUE PARA VER COMO PODEMOS IMPLEMTNTAR
                        }else {
                            // Se não houver valor inicial (ex: INT var;), inicializa com 0 no .data
                            gera_cod(s.nome, null); // `gera_cod` já trata `null` para gerar "0\n"
                        }
                    }
                    // Se isDeclarandoVetor for true, o case 30 já tratou, e este case 3 não faz nada
                }
                // Limpa as variáveis de estado para a próxima declaração/comando
                nome = null;
                valor = null;
                flagOp = false; // Essas parecem ser flags de expressão, talvez não pertençam aqui /// VER COMO O HERCK SE NESSESARIO
                oper = null;
                isDeclarandoVetor = false; // <--- LIMPA A FLAG AQUI!
                break;

            // Obtenção do operador na expressão + aciona flag p/ segundo ou outro operando
            case 4:
                flagOp = true;
                oper = token.getLexeme();
                break;

            // Geração de código dos operandos em uma expressão (origem: identificador)
            case 5:
                usarVariavel(token.getLexeme());
                if (!flagOp){
                    gera_cod("LD", token.getLexeme());
                } else {
                    if (Objects.equals(oper, "+")){
                        gera_cod("ADD", token.getLexeme());
                    }
                    if (Objects.equals(oper, "-")) {
                        gera_cod("SUB", token.getLexeme());
                    }
                    if (Objects.equals(oper, "&")) {
                        gera_cod("AND", token.getLexeme());
                    }
                    if (Objects.equals(oper, "^")) {
                        gera_cod("XOR", token.getLexeme());
                    }
                    if (Objects.equals(oper, "|")) {
                        gera_cod("OR", token.getLexeme());
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
                    if (Objects.equals(oper, "&")) {
                        gera_cod("ANDI", token.getLexeme());
                    }
                    if (Objects.equals(oper, "^")) {
                        gera_cod("XORI", token.getLexeme());
                    }
                    if (Objects.equals(oper, "|")) {
                        gera_cod("ORI", token.getLexeme());
                    }
                }
                flagOp = false;
                break;

            // Entrada de dados
            case 7:
                ignoreAssign = true;
                usarVariavel(token.getLexeme());
                gera_cod("LD", "$in_port");
                gera_cod("STO", token.getLexeme());
                break;

            // Saida de dados (origem: variavel)
            // NOTA: NÃO IMPLEMENTADO NO MOMENTO
            // compare pg.14 de "Geração de Código 1" e expressão em Portugol.gals
            // para entender
            case 8:
                usarVariavel(token.getLexeme());
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
                usarVariavel(token.getLexeme());
                nome_id_atrib = token.getLexeme();
                break;

            // Geração de código (atribuição de variavel)
            case 22:
                if (variavelVetor) {
                    gera_cod("LDI", indiceVetor);
                    Simbolo temp0 = GetTemp();
                    gera_cod("STO",temp0.nome);
                    gera_cod("LDI", indiceVetor);
                    Simbolo temp1 = GetTemp();
                    gera_cod("STO",temp1.nome);
                    gera_cod("LD", temp0.nome);
                    usarVariavel(temp0.nome);
//                    gera_cod("STO",temp1.nome);
                    gera_cod("LD", temp1.nome);
                    usarVariavel(temp1.nome);
                    gera_cod("STOV", nome_id_atrib);
                } else {
                    gera_cod("STO", nome_id_atrib);
                }
                variavelVetor = false;
                break;

            case 30: // Declaração de Vetores
                int tamanhoVetor;
                try {
                    tamanhoVetor = Integer.parseInt(token.getLexeme());
                    if (tamanhoVetor <= 0) {
                        logger.addError("O tamanho do vetor deve ser um número inteiro positivo: " + token.getLexeme(), actionPosition, token.getLexeme());
                        return;
                    }
                } catch (NumberFormatException e) {
                    logger.addError("Valor inválido para o tamanho do vetor: " + token.getLexeme(), actionPosition, token.getLexeme());
                    return;
                }

                // Verifica se o nome foi previamente atribuído
                if (nome == null || nome.isEmpty()) {
                    logger.addError("Nenhum nome de vetor foi especificado antes da declaração de tamanho.", actionPosition, token.getLexeme());
                    return;
                }

                // Criar e registrar o símbolo do vetor
                Simbolo simboloVetor = criarVetor(nome, 0, tamanhoVetor);
                if (simboloVetor == null) {
                    // Erro já tratado dentro de criarVetor
                    return;
                }

                // Gerar código Assembly BIP para vetor
                StringBuilder vetorCode = new StringBuilder(nome + ": ");
                for (int i = 0; i < tamanhoVetor; i++) {
                    vetorCode.append("0");
                    if (i < tamanhoVetor - 1) {
                        vetorCode.append(",");
                    }
                }
                vetorCode.append("\n");
                asmDataSection += vetorCode.toString();

                // Limpar variáveis de estado
                nome = null;
                valor = null;
                isDeclarandoVetor = true; // <--- SETA A FLAG AQUI!
                break;

            case 31:
                indiceVetor = token.getLexeme();
                variavelVetor = true;
                break;

            case 32:
                break;

            case 33:
                break;
        }
    }

    /*
        Objetivo: Retornar um temporário livre-Busca na lista
         →Se encontra um livre retorna-o e seta livre
        para False
         →Se não encontra cria um novo temp na lista
        retorna-o e seta livre para False
    */
    private Simbolo GetTemp(){
        Simbolo simbolo = escopoAtual.buscarTempLivre();
        if (simbolo == null) {
            String novoNome = "temp" + (escopoAtual.getTempCount());
            simbolo = new Simbolo(novoNome, 0, escopoAtual);
            simbolo.inicializada = true;
            simbolo.isTemp = true;
            escopoAtual.getSimbolos().put(novoNome, simbolo);
            asmTempDataSection += simbolo.nome + ": 0\n";
        }
        simbolo.isLivre = false;
        return simbolo;
    }

    /*
    *  Objetivo: Liberar um temporário-Busca temp na lista e seta livre para True
    */
    private void FreeTemp(String nome){
        Simbolo temporario = usarVariavel(nome);

        if (temporario != null){
            temporario.isLivre = true;
        }
    }

    private Simbolo criarVariavel(String nome, Integer tipo) throws SemanticError {
        if (escopoAtual.getSimbolos().containsKey(nome)) {
            logger.addError("Variável já declarada no mesmo escopo: " + nome, actionPosition, nome);
            return null;
        }
        Simbolo s = new Simbolo(nome, tipo, escopoAtual);
        s.inicializada = true;
        escopoAtual.getSimbolos().put(nome, s);
        return s;
    }
    private Simbolo criarVetor(String nome, Integer tipo, int tamanho) throws SemanticError {
        if (escopoAtual.getSimbolos().containsKey(nome)) {
            logger.addError("Variável ou vetor já declarado no mesmo escopo: " + nome, actionPosition, nome);
            return null;
        }
        Simbolo s = new Simbolo(nome, tipo, escopoAtual);
        s.inicializada = true;   // Vetores são considerados inicializados (com zeros)
        s.isVetor = true;        // Marca como vetor
        s.tamanhoVetor = tamanho; // Define o tamanho
        escopoAtual.getSimbolos().put(nome, s);
        return s;
    }

    private Simbolo usarVariavel(String nome) {
        System.out.println("Tentando ler variavel " + nome + " em " + escopoAtual.getNome() + " e acima");
        Simbolo simbolo = escopoAtual.buscarSimbolo(nome);
        if (simbolo == null) {
            logger.addError("Variável não declarada: " + nome, actionPosition, nome);
            return null;
        }

        if (!ignoreAssign){
            simbolo.usada = true;
        }
        ignoreAssign = false;

        if (!simbolo.inicializada) {
            logger.addWarning("Uso de variável não inicializada: " + nome, actionPosition, nome);
        }
        return simbolo;
    }
}
