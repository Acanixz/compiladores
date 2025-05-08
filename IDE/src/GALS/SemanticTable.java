/*
 * Template to help verify type compatibility in a Semantic Analyzer.
 * Available to Computer Science course at UNIVALI.
 * Professor Eduardo Alves da Silva.
 */

package GALS;

public class SemanticTable {

    public static final int ERR = -1;
    public static final int OK_ = 0;
    public static final int WAR = 1;


    public static final int INT = 0;
    public static final int FLO = 1;
    public static final int CHA = 2;
    public static final int STR = 3;
    public static final int BOO = 4;

    public static final int SUM = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int REL = 4; // qualquer operador relacional

    // TIPO DE RETORNO DAS EXPRESSOES ENTRE TIPOS
    // 5 x 5 X 5  = TIPO X TIPO X OPER
    static int expTable [][][] =
            {/*       INT       */ /*       FLOAT     */ /*      CHAR       */ /*      STRING     */ /*     BOOL        */
                    /*   INT*/ {{INT,INT,INT,FLO,BOO},{FLO,FLO,FLO,FLO,BOO},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR}},
                    /* FLOAT*/ {{},{},{},{},{}},
                    /*  CHAR*/ {{},{},{},{},{}},
                    /*STRING*/ {{},{},{},{},{}},
                    /*  BOOL*/ {{},{},{},{},{}}
            };

    // atribuicoes compativeis
    // 5 x 5 = TIPO X TIPO
    static int atribTable [][]={/* INT FLO CHA STR BOO  */
            /*INT*/ {OK_,WAR,ERR,ERR,ERR},
            /*FLO*/ {},
            /*CHA*/ {},
            /*STR*/ {},
            /*BOO*/ {}
    };

    static int resultType (int TP1, int TP2, int OP){
        return (expTable[TP1][TP2][OP]);
    }

    static int atribType (int TP1, int TP2){
        return (atribTable[TP1][TP2]);
    }
}