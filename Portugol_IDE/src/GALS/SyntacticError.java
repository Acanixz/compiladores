package GALS;


public class SyntacticError extends AnalysisError
{
    public SyntacticError(String msg, int position)
	 {
        super(msg, position);
    }

    public SyntacticError(String msg)
    {
        super(msg);
    }
}
