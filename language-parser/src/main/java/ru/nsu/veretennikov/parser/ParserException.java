package ru.nsu.veretennikov.parser;

/**
 * Исключение синтаксического анализатора
 */
public class ParserException extends RuntimeException {
    private final Token token;

    public ParserException(String message, Token token) {
        super(String.format("Syntax error at line %d, column %d: %s (found: %s)", 
                          token.getLine(), token.getColumn(), message, token.getValue()));
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
