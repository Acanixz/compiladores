package GALS;

import java.util.Stack;

public class Sintatico implements Constants
{
    private final Stack<Integer> stack = new Stack<Integer>();
    private Token currentToken;
    private Token previousToken;
    private Lexico scanner;
    private Semantico semanticAnalyser;

    public void parse(Lexico scanner, Semantico semanticAnalyser) throws LexicalError, SyntacticError, SemanticError
    {
        this.scanner = scanner;
        this.semanticAnalyser = semanticAnalyser;

        stack.clear();
        stack.push(0);

        currentToken = scanner.nextToken();

        while ( ! step() )
            ;
    }

    private boolean step() throws LexicalError, SyntacticError, SemanticError
    {
        if (currentToken == null)
        {
            int pos = 0;
            if (previousToken != null)
                pos = previousToken.getPosition()+previousToken.getLexeme().length();

            currentToken = new Token(DOLLAR, "$", pos);
        }

        int token = currentToken.getId();
        int state = stack.peek();

        int[] cmd = PARSER_TABLE[state][token-1];

        switch (cmd[0])
        {
            case SHIFT:
                stack.push(cmd[1]);
                previousToken = currentToken;
                currentToken = scanner.nextToken();
                return false;

            case REDUCE:
                int[] prod = PRODUCTIONS[cmd[1]];

                for (int i=0; i<prod[1]; i++)
                    stack.pop();

                int oldState = stack.peek();
                stack.push(PARSER_TABLE[oldState][prod[0]-1][1]);
                return false;

            case ACTION:
                int action = FIRST_SEMANTIC_ACTION + cmd[1] - 1;
                stack.push(PARSER_TABLE[state][action][1]);
                semanticAnalyser.executeAction(cmd[1], previousToken, currentToken.getPosition());
                return false;

            case ACCEPT:
                return true;

            case ERROR:
                throw new SyntacticError(PARSER_ERROR[state], currentToken.getPosition());
        }
        return false;
    }

}
