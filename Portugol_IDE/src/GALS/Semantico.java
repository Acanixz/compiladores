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
    // NOVO: Para capturar o nome do vetor antes de acessar seu índice em expressões de leitura
    private String idParaAcessoVetor = null; // <-- ADICIONADO AQUI
    private String nomeTempIndice = null; // Armazenará o nome da temp para o índice do vetor
    private String nomeTempValorAtribuicao = null; // Armazenará o nome da temp para o valor a ser atribuído

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
                Simbolo s = escopoAtual.buscarSimbolo(token.getLexeme()); // Busca o símbolo na tabela
                if (s != null && s.isVetor) { // SE o ID é um vetor
                    idParaAcessoVetor = token.getLexeme(); // Armazena o nome do vetor para uso posterior (em #33)
                    // NADA DE GERAÇÃO DE CÓDIGO AQUI. O LDV será gerado no case 33.
                } else { // SE o ID é uma variável simples (não um vetor)
                    if (!flagOp) {
                        gera_cod("LD", token.getLexeme());
                    } else {
                        if (Objects.equals(oper, "+")) {
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
                // Geração de código (atribuição de variavel)
                if (variavelVetor) {
                    // O índice já está em X (graças ao case 31)
                    // O valor a ser atribuído já está no acumulador (A)
                    // nome_id_atrib é o nome do vetor
                    gera_cod("STOV", nome_id_atrib); // Atribui valor de A para [nome_id_atrib + X]
                } else {
                    gera_cod("STO", nome_id_atrib);
                }
                variavelVetor = false; // Reset da flag
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
