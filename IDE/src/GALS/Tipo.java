package GALS;

public enum Tipo {
    INT,
    FLOAT,
    BOOL,
    CHAR,
    STRING,
    VOID;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
