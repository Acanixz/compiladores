package GALS;

public class Semantico implements Constants
{
    Simbolo[] tabelaSimbolos;
    public void executeAction(int action, Token token)	throws SemanticError
    {
        System.out.println("Ação #"+action+", Token: "+token);
    }
}
