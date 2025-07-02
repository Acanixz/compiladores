package GALS;

import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.*;

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
    private String asmTextSection = "";
    Boolean lendoSecaoData = true;
    Boolean primeiroCode = true;
    String nome;
    String nome_id_atrib;
    String valor;
    Boolean flagOp = false;
    String oper;
    String oprel;
    Boolean variavelVetor = false;
    String indiceVetor;
    Simbolo TEMP_ESQ;
    Simbolo TEMP_DIR;
    private Stack<String> pilhaRotulo = new Stack<>();
    String rotIf = "";
    String rotFim = "";
    String rotIni = "";
    String rotFor = "";
    String rotAposFor = "";
    Simbolo tempFor1;
    Simbolo tempFor2;
    private Stack<Integer> pilhaOperandosFor = new Stack<>();
    String ASMForIncrementBuffer = "";
    int rotCount = 0;
    String nome_call = "";
    int contpar = 0;
    int paramsValidados = 0;
    int maiorParam = 0;

    private boolean isDeclarandoVetor = false; // Novo campo
    private String idParaAcessoVetor = null; // <-- ADICIONADO AQUI
    private String nomeTempIndice = null; // Armazenará o nome da temp para o índice do vetor
    private String nomeTempValorAtribuicao = null; // Armazenará o nome da temp para o valor a ser atribuído
    private String idParaLeituraVetor = null; // Guarda o nome do vetor para 'leia' com índice
    private boolean lendoVetorIndexado = false; // Flag para indicar que estamos lendo um vetor com índice


    public void reset() {
        logger.clearWarnsAndErrors();

        escopoGlobal = new Escopo();
        escopoAtual = escopoGlobal;

        pilhaExpr.clear();

        actionPosition = -1;
        parameterPosition = -1;
        isFunctionHeader = false;
        ignoreAssign = false;

        idParaAcessoVetor = null; // <-- RESETAR TAMBÉM
        nomeTempIndice = null;
        nomeTempValorAtribuicao = null;
        idParaLeituraVetor = null;
        lendoVetorIndexado = false;
        oprel = null;
        TEMP_ESQ = null;
        TEMP_DIR = null;
        pilhaRotulo = new Stack<>();
        rotIf = "";
        rotFim = "";
        rotIni = "";
        rotFor = "";
        rotAposFor = "";
        tempFor1 = null;
        tempFor2 = null;
        pilhaOperandosFor = new Stack<>();
        ASMForIncrementBuffer = "";
        rotCount = 0;
        nome_call = "";
        contpar = 0;
        paramsValidados = 0;
        maiorParam = 0;
    }

    // Junta as seções de declaração e código em uma string ASM legivel pelo Bipide 3.0
    // Download: https://sourceforge.net/projects/bipide/
    public String compilar_ASM(){
        for (Escopo escopoFilho : escopoGlobal.children){
            if (escopoFilho.getNome().equals("principal")){
                asmTextSection = "\tJMP _principal\n" + asmTextSection;
            }
        }

        asmTextSection = ".text\n" + asmTextSection;
        return asmDataSection + "\n" + asmTempDataSection + "\n" + asmTextSection;
    }

    private void gera_cod(String nome, String valor){
        List<String> operadores = List.of("LD", "ADD", "SUB", "AND", "XOR", "OR", "LDI", "ADDI", "SUBI", "ANDI", "XORI", "ORI", "STO", "STOV", "JMP", "BLE", "BGE", "BNE", "BEQ", "BGT", "BLT", "ROT", "RETURN", "HLT", "CALL");

        if (operadores.contains(nome)){
            if (nome.equals("ROT")){
                asmTextSection += valor + ":" + "\n";
            } else {
                asmTextSection += "\t" + nome + "\t" + valor + "\n";
            }
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
        } else {
            System.out.println("[ALERTA] Comando não reconhecido pelo gerador ASM: " + nome);
            asmDataSection += nome + ": 0\n";
        }
    }

    public void executeAction(int action, Token token)	throws SemanticError
    {
        System.out.println("Ação #" + action + ", Token: " + token);
        String lexeme = token.getLexeme();
        switch (action){
            /// Casos da Geração de Código 1 - Instruções Sequenciais com Vetores

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
                if(nome != null){ // Garante que há um nome para processar
                    // Se isDeclarandoVetor for true, o case 30 já tratou, e este case 3 não faz nada
                    if(!isDeclarandoVetor){
                        criarVariavel(nome, 0);
                        gera_cod(nome, valor);
                    }
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
                Simbolo s = escopoAtual.buscarSimbolo(token.getLexeme()); // Busca o símbolo na tabela
                if (s != null && s.isVetor) { // SE o ID é um vetor
                    if (s.escopo != escopoGlobal){
                        lexeme = s.escopo + "_" + lexeme;
                    }

                    idParaAcessoVetor = lexeme; // Armazena o nome do vetor para uso posterior (em #33)
                    // NADA DE GERAÇÃO DE CÓDIGO AQUI. O LDV será gerado no case 33.
                } else { // SE o ID é uma variável simples (não um vetor)
                    if (s.escopo != escopoGlobal){
                        lexeme = s.escopo + "_" + lexeme;
                    }

                    if (!flagOp) {
                        gera_cod("LD", lexeme);
                    } else {
                        if (Objects.equals(oper, "+")) {
                            gera_cod("ADD", lexeme);
                        }
                        if (Objects.equals(oper, "-")) {
                            gera_cod("SUB", lexeme);
                        }
                        if (Objects.equals(oper, "&")) {
                            gera_cod("AND", lexeme);
                        }
                        if (Objects.equals(oper, "^")) {
                            gera_cod("XOR", lexeme);
                        }
                        if (Objects.equals(oper, "|")) {
                            gera_cod("OR", lexeme);
                        }
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

                Simbolo simboloLido = escopoAtual.buscarSimbolo(token.getLexeme());
                if (simboloLido != null && simboloLido.isVetor) {
                    // Se é um vetor, apenas armazena o nome do vetor e seta a flag.
                    // A geração do LD $in_port e STOV será feita no case 32.
                    idParaLeituraVetor = token.getLexeme();
                    lendoVetorIndexado = true;
                } else {
                    // Se é uma variável simples, gera o código de leitura direto
                    gera_cod("LD", "$in_port");
                    gera_cod("STO", token.getLexeme());
                    // Não é um vetor indexado, então as flags devem ser false (já são por padrão)
                }
                break;

            // Saida de dados
            case 9:
                // gera_cod("LDI", token.getLexeme());
                gera_cod("STO", "$out_port");
                break;

            // Nome p/ atribuição em variavel
            case 21:
                Simbolo temp = usarVariavel(token.getLexeme());

                if (temp != null && temp.escopo != escopoGlobal){
                    lexeme = temp.escopo.getNome() + "_" + lexeme;
                }
                nome_id_atrib = lexeme;
                break;

            // Geração de código (atribuição de variavel)
            case 22: // Geração de código (atribuição de variavel ou vetor)
                if (variavelVetor) {
                    // Neste ponto, o valor a ser atribuído (lado direito de "<-")
                    // já foi avaliado e está no acumulador (A).
                    // Precisamos salvá-lo em outro temporário.
                    Simbolo tempValor = GetTemp();
                    gera_cod("STO", tempValor.nome); // Salva o valor a ser atribuído em um temporário
                    nomeTempValorAtribuicao = tempValor.nome; // Guarda o nome do temporário

                    // 1. Carregar o índice de volta para o acumulador (A)
                    gera_cod("LD", nomeTempIndice);

                    // 2. Mover o índice do acumulador (A) para o registrador de índice (X ou $indr)
                    // O BIPIDE usa STO $indr para isso, ou MOVX (se suportado pelo seu Bipide/geração).
                    // Pelo seu código desejado, é STO $indr
                    gera_cod("STO", "$indr"); // Armazena o índice no registrador X (o seu "$indr")

                    // 3. Carregar o valor a ser atribuído de volta para o acumulador (A)
                    gera_cod("LD", nomeTempValorAtribuicao);

                    // 4. Finalmente, armazenar o valor do acumulador (A) no vetor no índice X
                    gera_cod("STOV", nome_id_atrib); // nome_id_atrib deve ser o nome base do vetor ('vetorb')

                    // Liberar os temporários após o uso
                    FreeTemp(nomeTempIndice);
                    FreeTemp(nomeTempValorAtribuicao);
                    nomeTempIndice = null;
                    nomeTempValorAtribuicao = null;
                    usarVariavel(tempValor.nome);

                } else {
                    // Lógica para atribuição de variável simples (que já está correta)
                    gera_cod("STO", nome_id_atrib);
                }
                variavelVetor = false; // Reset da flag para a próxima operação
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
                //Ação semântica para o índice de um vetor em ATRIBUIÇÃO (lado esquerdo)
                // Nesse ponto, a expressão <exp2> (o índice) já foi avaliada e seu resultado está no acumulador (A).
                // Precisamos salvar esse valor do índice em um temporário.
                Simbolo tempIndice = GetTemp(); // Pega um temporário novo
                gera_cod("STO", tempIndice.nome); // Salva o índice atual do acumulador no temporário
                nomeTempIndice = tempIndice.nome; // Guarda o nome do temporário para usar depois

                // A flag `variavelVetor` já está sendo usada, então vamos mantê-la.
                variavelVetor = true; // Sinaliza que a próxima atribuição é para um vetor
                break;

            case 32:
                if (!lendoVetorIndexado || idParaLeituraVetor == null) {
                    // Erro interno: #32 disparado sem um 'leia' de vetor correspondente
                    logger.addError("Erro interno: Ação #32 disparada sem contexto de leitura de vetor indexado.", action, token.getLexeme());
                    return;
                }

                // Nesse ponto, a expressão <exp2> (o índice) já foi avaliada
                // e seu resultado está no acumulador (A).
                // 1. Mover o índice de A para o registrador de índice X ($indr)
                gera_cod("STO", "$indr"); // Armazena o índice no registrador X ($indr)

                // 2. Carregar o valor da porta de entrada para o acumulador (A)
                gera_cod("LD", "$in_port");

                // 3. Armazenar o valor do acumulador (A) no vetor na posição indicada por X
                gera_cod("STOV", idParaLeituraVetor);

                // Resetar as flags e variáveis de estado para a próxima instrução
                lendoVetorIndexado = false;
                idParaLeituraVetor = null;
                break;


            case 33: // Leitura de elemento de vetor (ID #5 "[" <exp2> #33 "]")
                // Neste ponto, a <exp2> do índice já foi avaliada e seu resultado está no acumulador (A)
                // Precisamos mover o conteúdo de A para o registrador de índice (X) do BIPIDE.
                gera_cod("MOVX", ""); // Move o conteúdo de A para X

                if (idParaAcessoVetor == null) {
                    logger.addError("Erro interno: Nome do vetor não capturado para acesso indexado.", action, token.getLexeme());
                    return;
                }
                // Agora que o índice está em X, carregamos o valor do elemento do vetor em A
                gera_cod("LDV", idParaAcessoVetor); // Carrega o valor de [idParaAcessoVetor + X] para A
                idParaAcessoVetor = null; // Limpa a flag
                break;

            /// Casos da Geração de Código 2 - Estruturas de Controle de Fluxo (desvios e loops)
            // Expressões relacionais, obtém operador
            case 107:
                oprel = token.getLexeme();
                TEMP_ESQ = GetTemp();
                gera_cod("STO", TEMP_ESQ.nome);
                break;

            // Expressões relacionais, obtém operando direito
            case 108:
                TEMP_DIR = GetTemp();
                gera_cod("STO", TEMP_DIR.nome);
                gera_cod("LD", TEMP_ESQ.nome);
                gera_cod("SUB", TEMP_DIR.nome);
                break;

            // Começo do escopo do if statement
            case 109:
                rotIf = newRotulo();
                pilhaRotulo.push(rotIf);
                geraSaltoCondicional(oprel, rotIf, false);
                enterEscopo();
                break;

            // Fim do escopo do if statement
            case 110:
                rotFim = pilhaRotulo.pop();
                gera_cod("ROT", rotFim);
                exitEscopo();
                break;

            // Else statement
            case 111:
                exitEscopo(); // Sai do escopo do if
                enterEscopo(); // Entra no escopo do else
                rotIf = pilhaRotulo.pop();
                rotFim = newRotulo();
                gera_cod("JMP", rotFim);
                pilhaRotulo.push(rotFim);
                gera_cod("ROT", rotIf);
                break;

            // While statement
            case 112:
                rotIni = newRotulo();
                pilhaRotulo.push(rotIni);
                gera_cod("ROT", rotIni);
                break;

            // Salto condicional e começo do escopo do while statement
            case 113:
                rotFim = newRotulo();
                pilhaRotulo.push(rotFim);
                geraSaltoCondicional(oprel, rotFim, false);
                enterEscopo();
                break;

            // Fim do while statement
            case 114:
                rotFim = pilhaRotulo.pop();
                rotIni = pilhaRotulo.pop();
                gera_cod("JMP", rotIni);
                gera_cod("ROT", rotFim);
                exitEscopo();
                break;

            // Começo do escopo do DO WHILE statement
            case 115:
                rotIni = newRotulo();
                pilhaRotulo.push(rotIni);
                gera_cod("ROT", rotIni);
                enterEscopo();
                break;

            // Fim do DO WHILE statement e Salto condicional
            // NOTA: caso 123 trata fim do escopo antecipadamente
            case 116:
                rotIni = pilhaRotulo.pop();
                geraSaltoCondicional(oprel, rotIni, true);
                break;

            // Definição do rótulo do for loop
            case 119:
                pilhaOperandosFor.push(Integer.parseInt(token.getLexeme()));

                rotFor = newRotulo();
                pilhaRotulo.push(rotFor);
                gera_cod("ROT", rotFor);

                gera_cod("LD", nome_id_atrib);
                tempFor1 = GetTemp();
                gera_cod("STO", tempFor1.nome);
                break;

            // Definição da condição de conclusão for loop
            case 120:
                rotAposFor = newRotulo();
                pilhaRotulo.push(rotAposFor);

                tempFor2 = GetTemp();
                gera_cod("STO", tempFor2.nome);
                gera_cod("LD", tempFor1.nome);
                gera_cod("SUB", tempFor2.nome);
                geraOpRel(pilhaOperandosFor.pop(), Integer.parseInt(token.getLexeme()));
                geraSaltoCondicional(oprel, rotAposFor, false);
                break;

            // Definição do incremento do for loop (e inicio do código dentro do for)
            case 121:
                ASMForIncrementBuffer += "\tLD\t" + nome_id_atrib + "\n";

                ///  NÃO ESQUECE DE ME CONFERIR
                if (oprel == ">")
                    ASMForIncrementBuffer += "\tADDI\t" + token.getLexeme() + "\n";
                else
                    ASMForIncrementBuffer += "\tSUBI\t" + token.getLexeme() + "\n";

                ASMForIncrementBuffer += "\tSTO\t" + nome_id_atrib + "\n";

                oprel = "";
                break;

            // Fim do for loop
            case 122:
                asmTextSection += ASMForIncrementBuffer;
                ASMForIncrementBuffer = "";
                gera_cod("JMP", rotFor);
                gera_cod("ROT", rotAposFor);
                exitEscopo();
                break;

            // Fim do escopo do DO WHILE
            case 123:
                exitEscopo();
                break;

            // Começo do escopo do for loop
            case 124:
                enterEscopo();
                break;

            /// Casos da Geração de Código 3 - Subrotinas
            case 201:
                nome = token.getLexeme();

                simboloAtual = criarVariavel(token.getLexeme(), 0);

                if (simboloAtual != null) {
                    simboloAtual.inicializada = true;
                    simboloAtual.isFuncao = true;

                    if (nome.equals("principal")){
                        simboloAtual.usada = true;
                    }
                }

                enterEscopo(nome);
                gera_cod("ROT", "_" + nome);
                break;

            case 202:
                if (escopoAtual.getNome().equals("principal")){
                    gera_cod("HLT", "0");
                } else {
                    gera_cod("RETURN", "0");
                }
                exitEscopo();
                break;

            case 203:
                nome_call = token.getLexeme();
                usarVariavel(nome_call);
                contpar = 0;
                break;

            case 204:
                if (escopoAtual.buscarSimbolo(token.getLexeme()) != null){
                    gera_cod("LD", token.getLexeme()); // ver se é valor ou id
                    usarVariavel(token.getLexeme());
                } else {
                    if (Pattern.matches("0[bB][01]+|0[xX][0-9a-fA-F]+|[0-9]+", token.getLexeme())) {
                        gera_cod("LDI", token.getLexeme());
                    } else {
                        usarVariavel(token.getLexeme()); // Só para acionar o erro de var não encontrada
                    }
                }

                gera_cod("STO", nome_call + "_" + getParname(nome_call, contpar));
                contpar++;
                break;

            case 205:
                if (paramsValidados < maiorParam){
                    logger.addError("Era esperado " + maiorParam + " params, recebeu apenas " + paramsValidados, 0, "");
                }
                gera_cod ("CALL", "_" + nome_call);
                break;

            // Declaração de parametros
            case 206:
                parameterPosition++;
                // OBS: BIP usa apenas integers, tipo sempre 0
                if(nome != null){ // Garante que há um nome para processar
                    // nome = escopoAtual.getNome() + "_" + nome;
                    // Se isDeclarandoVetor for true, o case 30 já tratou, e este case 3 não faz nada
                    if(!isDeclarandoVetor){
                        Simbolo parametro = criarVariavel(nome, 0);

                        if (parametro != null){
                            parametro.isParametro = true;
                            parametro.parametroPosicao = parameterPosition;
                        }

                        gera_cod(escopoAtual.getNome() + "_" + nome, valor);
                    }
                }
                // Limpa as variáveis de estado para a próxima declaração/comando
                nome = null;
                valor = null;
                flagOp = false;
                oper = null;
                isDeclarandoVetor = false; // <--- LIMPA A FLAG AQUI!
                break;

            // Fim da declaração de parametros
            case 207:
                parameterPosition = -1;
                break;
        }
    }

    public String getParname(String nomeCall, int contpar) {
        Escopo escopoVerif = escopoAtual; // ou receba o escopo como parâmetro

        while (escopoVerif != null) {
            // primeiro, procure o símbolo da função
            Simbolo func = null;
            for (Simbolo s : escopoVerif.getSimbolos().values()) {
                if (s.isFuncao && s.nome.equals(nomeCall)) {
                    func = s;
                    break;
                }
            }

            // se achou a função, procure o parâmetro na mesma tabela
            if (func != null) {
                Escopo escopoFunc = null;
                for (Escopo e : func.escopo.children){
                    if (e.getNome().equals(func.nome)){
                        escopoFunc = e;
                    }
                }

                if (escopoFunc != null){
                    maiorParam = 0;
                    for (Simbolo s : escopoFunc.getSimbolos().values()) {
                        if (s.isParametro) {
                            maiorParam++;
                        }
                    }

                    for (Simbolo s : escopoFunc.getSimbolos().values()) {
                        if (s.isParametro
                                && s.parametroPosicao == contpar
                            // opcional: garantir que esse parâmetro pertence à função certa,
                            // caso haja algum link, como s.getFuncaoPai() == func
                        ) {
                            paramsValidados++;
                            return s.nome;
                        }
                    }

                    logger.addError("Era esperado " + maiorParam + " params, mas recebeu " + (contpar+1), 0, "");
                }
            }

            escopoVerif = escopoVerif.getParent();
        }

        // se não encontrou nada, devolve null (ou lança exceção, se preferir)
        return null;
    }


    private String newRotulo(){
        rotCount += 1;
        return "R" + (rotCount);
    }

    private void enterEscopo() {
        // gera nome aleatório para escopo
        int proximoInt = escopoAtual.children.size() + 1;
        String nomeEscopo = escopoAtual.getNome() + "_" + proximoInt;
        Escopo novoEscopo = new Escopo(nomeEscopo, escopoAtual);
        escopoAtual.children.add(novoEscopo);
        escopoAtual = novoEscopo;
    }

    private void enterEscopo(String nomeEscopo) {
        // gera nome aleatório para escopo
        Escopo novoEscopo = new Escopo(nomeEscopo, escopoAtual);
        escopoAtual.children.add(novoEscopo);
        escopoAtual = novoEscopo;
    }

    private void exitEscopo() {
        escopoAtual = escopoAtual.getParent();
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
            String novoNome = "temp" + (escopoAtual.getTempCount() + 1);
            simbolo = new Simbolo(novoNome, 0, escopoAtual);
            simbolo.inicializada = true;
            simbolo.isTemp = true;
            simbolo.usada = true;
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

    // For loop precisa gerar o simbolo de oprel manualmente
    private void geraOpRel(int num1, int num2){
        if (num1 < num2) {
            oprel = "<";
            return;
        }

        if (num1 > num2) {
            oprel = ">";
            return;
        }

        logger.addError("Valores de inicialização e fim do for loop são iguais", 0, null);
    }

    private void geraSaltoCondicional(String oprel, String nomeRotulo, boolean useExpLogic) {
        switch (oprel) {
            case ">":
                if (useExpLogic){
                    gera_cod("BGT", nomeRotulo);
                } else {
                    gera_cod("BLE", nomeRotulo); // se A > B falhar, A <= B, então salta
                }

                break;
            case "<":
                if (useExpLogic){
                    gera_cod("BLT", nomeRotulo);
                } else {
                    gera_cod("BGE", nomeRotulo); // se A < B falhar, A >= B, então salta
                }
                break;
            case "==":
                if (useExpLogic){
                    gera_cod("BEQ", nomeRotulo);
                } else {
                    gera_cod("BNE", nomeRotulo); // se A == B falhar, A != B, então salta
                }
                break;
            case "!=":
                if (useExpLogic){
                    gera_cod("BNE", nomeRotulo);
                } else {
                    gera_cod("BEQ", nomeRotulo); // se A != B falhar, A == B, então salta
                }
                break;
            case "<=":
                if (useExpLogic){
                    gera_cod("BLE", nomeRotulo);
                } else {
                    gera_cod("BGT", nomeRotulo); // se A <= B falhar, A > B, então salta
                }
                break;
            case ">=":
                if (useExpLogic){
                    gera_cod("BGE", nomeRotulo);
                } else {
                    gera_cod("BLT", nomeRotulo); // se A >= B falhar, A < B, então salta
                }
                break;
            default:
                throw new IllegalArgumentException("Operador relacional inválido: " + oprel);
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
