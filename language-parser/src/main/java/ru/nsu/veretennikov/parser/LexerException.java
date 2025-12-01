package ru.nsu.veretennikov.parser;

/**
 * Исключение лексического анализатора
 */
public class LexerException extends RuntimeException {
    private final int line;
    private final int column;

    public LexerException(String message, int line, int column) {
        super(String.format("Lexical error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
