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

    // Tipos básicos
    public static final int INT = 0;
    public static final int FLO = 1;
    public static final int CHA = 2;
    public static final int STR = 3;
    public static final int BOO = 4;

    // Operadores (IDs para empilhamento / resultType)
    public static final int SUM    = 0;   // +         (#3)
    public static final int SUB    = 1;   // -         (#3 e também para unário)
    public static final int MUL    = 2;   // *         (#4)
    public static final int DIV    = 3;   // /         (#5)
    public static final int MOD    = 4;   // %         (#6)
    public static final int REL    = 5;   // ==, !=, >, <, >=, <=, &&, ||  (#7)
    public static final int SHL    = 6;   // <<        (#10)
    public static final int SHR    = 7;   // >>        (#10)
    public static final int BOR    = 8;   // |         (#7)
    public static final int XOR    = 9;   // ^         (#7)
    public static final int BAND   = 10;  // &         (#7)
    public static final int LAND   = 11;  // &&        (#7)
    public static final int LOR    = 12;  // ||        (#7)
    public static final int BNOT   = 13;  // ~         (#9)
    public static final int LNOT   = 14;  // !         (#9)

    /**
     * Tabela de expressões: [tipo1][tipo2][operador] → tipo de resultado (ou ERR/WAR)
     * Operadores indexados por: SUM(0),SUB(1),MUL(2),DIV(3),MOD(4),
     * REL(5),SHL(6),SHR(7),BOR(8),XOR(9),BAND(10),LAND(11),LOR(12),BNOT(13),LNOT(14)
     */
    static int expTable[][][] = {
            // ---------- INT ---------- (linha tipo1 = INT)
            {       /*           + ,  - ,  * ,  / ,  % , REL, << , >> ,  | ,  ^ ,  & , && , || ,  ~ ,  ! */
                    /* INT  */ {INT, INT, INT, INT, INT, BOO, INT, INT, INT, INT, INT, BOO, BOO, ERR, ERR},
                    /* FLOAT*/ {INT, INT, INT, INT, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* BOOL */ {INT, INT, INT, INT, INT, BOO, INT, INT, INT, INT, BOO, BOO, BOO, ERR, ERR},
                    /* CHAR */ {INT, INT, INT, INT, INT, BOO, ERR, ERR, INT, INT, BOO, BOO, BOO, ERR, ERR},
                    /* STR  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
            },
            // ---------- FLOAT ---------- (linha tipo1 = FLOAT)
            {       /*           + ,  - ,  * ,  / ,  % , REL, << , >> ,  | ,  ^ ,  & , && , || ,  ~ ,  ! */
                    /* INT  */ {FLO, FLO, FLO, FLO, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* FLOAT*/ {FLO, FLO, FLO, FLO, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* BOOL */ {FLO, FLO, FLO, FLO, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* CHAR */ {FLO, FLO, FLO, FLO, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* STR  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
            },
            // ---------- CHAR ---------- (linha tipo1 = CHAR)
            {       /*           + ,  - ,  * ,  / ,  % , REL, << , >> ,  | ,  ^ ,  & , && , || ,  ~ ,  ! */
                    /* INT  */ {CHA, CHA, CHA, CHA, CHA, BOO, CHA, CHA, CHA, CHA, BOO, BOO, BOO, ERR, ERR},
                    /* FLOAT*/ {CHA, CHA, CHA, CHA, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* BOOL */ {CHA, CHA, CHA, CHA, CHA, BOO, CHA, CHA, CHA, CHA, BOO, BOO, BOO, ERR, ERR},
                    /* CHAR */ {CHA, CHA, CHA, CHA, CHA, BOO, ERR, ERR, CHA, CHA, CHA, BOO, BOO, ERR, ERR},
                    /* STR  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
            },
            // ---------- STRING ---------- (linha tipo1 = STRING)
            {       /*           + ,  - ,  * ,  / ,  % , REL, << , >> ,  | ,  ^ ,  & , && , || ,  ~ ,  ! */
                    /* INT  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
                    /* FLOAT*/ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
                    /* BOOL */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
                    /* CHAR */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
                    /* STR  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
            },
            // ---------- BOOL ---------- (linha tipo1 = BOOL)
            {       /*           + ,  - ,  * ,  / ,  % , REL, << , >> ,  | ,  ^ ,  & , && , || ,  ~ ,  ! */
                    /* INT  */ {ERR, ERR, ERR, ERR, ERR, BOO, ERR, ERR, ERR, ERR, BOO, BOO, BOO, ERR, ERR},
                    /* FLOAT*/ {ERR, ERR, ERR, ERR, ERR, BOO, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
                    /* BOOL */ {ERR, BOO, BOO, BOO, BOO, BOO, ERR, BOO, ERR, BOO, BOO, BOO, BOO, ERR, ERR},
                    /* CHAR */ {ERR, ERR, ERR, ERR, ERR, BOO, ERR, ERR, ERR, ERR, BOO, BOO, BOO, ERR, ERR},
                    /* STR  */ {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, BOO, BOO, ERR, ERR},
            }
    };

    // compatibilidade de atribuição: [destino][origem]
    static int atribTable [][] = {
            /*INT*/    {OK_, WAR, OK_, OK_, ERR},
            /*FLOAT*/  {OK_, OK_, OK_, OK_, ERR},
            /*BOOL*/   {OK_, WAR, OK_, WAR, OK_},
            /*CHAR*/   {OK_, WAR, OK_, OK_, ERR},
            /*STRING*/ {ERR, ERR, ERR, ERR, OK_}
    };

    /** retorna tipo resultante de TP1 OP TP2 */
    public static int resultType(int TP1, int TP2, int OP) {
        if (TP1 == -1 || TP2 == -1) return ERR;
        return expTable[TP1][TP2][OP];
    }

    /** retorna compatibilidade de atribuição dest ← orig */
    public static int atribType(int TP1, int TP2) {
        if (TP1 == -1 || TP2 == -1) return ERR;
        return atribTable[TP1][TP2];
    }
}