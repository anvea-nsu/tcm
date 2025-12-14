package ru.nsu.veretennikov.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Кастомный синтаксический анализатор на основе ДКА
 */
public class CustomParser {

    private enum State {
        Q0, Q1, Q2, Q3, Q4, QF, ERROR
    }

    private List<Token> tokens;
    private int position;
    private Token currentToken;
    private State currentState;
    private StringBuilder trace;
    private List<String> errors;
    private int errorCount;

    public CustomParser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.currentToken = tokens.get(0);
        this.currentState = State.Q0;
        this.trace = new StringBuilder();
        this.errors = new ArrayList<>();
        this.errorCount = 0;
    }

    private void advance() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        }
    }

    private void addTrace(String action) {
        trace.append(String.format("State: %s, Token: %s (%s) -> %s\n",
                currentState,
                currentToken.getType(),
                currentToken.getValue(),
                action));
    }

    private void addError(String message, Token token) {
        errorCount++;
        String errorMsg;

        if (currentState == State.Q0 && token.getType() == Token.TokenType.IDENTIFIER) {
            String val = token.getValue();
            if (val.contains(".")) {
                errorMsg = String.format("Error #%d at line %d, column %d: Expected 'Console.ReadLine', found '%s'",
                        errorCount, token.getLine(), token.getColumn(), val);
            } else {
                errorMsg = String.format("Error #%d at line %d, column %d: %s (found: '%s')",
                        errorCount, token.getLine(), token.getColumn(), message, val);
            }
        } else {
            errorMsg = String.format("Error #%d at line %d, column %d: %s (found: '%s')",
                    errorCount, token.getLine(), token.getColumn(), message,
                    token.getValue().isEmpty() ? token.getType().toString() : token.getValue());
        }

        errors.add(errorMsg);
        trace.append(">>> " + errorMsg + "\n");
    }

    private String getExpectedTokens(State state, Token token) {
        switch (state) {
            case Q0:
                if (token.getType() == Token.TokenType.IDENTIFIER) {
                    String val = token.getValue();
                    if (val.startsWith("onsole") || val.startsWith("Console.") ||
                            val.contains(".ReadLine") || val.contains("ReadLine")) {
                        return "Console.ReadLine (found typo: '" + val + "')";
                    }
                }
                return "Console.ReadLine or EOF (empty program)";
            case Q1: return "'('";
            case Q2: return "')' or identifier (variable name)";
            case Q3: return "')' (this should not happen - identifier is one token)";
            case Q4: return "';'";
            default: return "unknown";
        }
    }

    private void recover() {
        trace.append(">>> Recovery: Attempting to recover from error in state " + currentState + "\n");

        switch (currentState) {
            case Q0:
                if (currentToken.getType() == Token.TokenType.IDENTIFIER) {
                    String val = currentToken.getValue();
                    if (val.contains(".")) {
                        advance();
                        currentState = State.Q1;
                        trace.append(">>> Recovery: Treating '" + val + "' as Console.ReadLine, moving to Q1\n");
                        return;
                    }
                }
                advance();
                break;

            case Q1:
                if (currentToken.getType() == Token.TokenType.LPAREN) {
                    return;
                }
                while (currentToken.getType() != Token.TokenType.EOF) {
                    if (currentToken.getType() == Token.TokenType.SEMICOLON) {
                        advance();
                        currentState = State.Q0;
                        trace.append(">>> Recovery: Found ';', returning to Q0\n");
                        return;
                    }
                    advance();
                }
                break;

            case Q2:
                while (currentToken.getType() != Token.TokenType.EOF) {
                    if (currentToken.getType() == Token.TokenType.RPAREN) {
                        currentState = State.Q4;
                        trace.append(">>> Recovery: Found ')', moving to Q4\n");
                        return;
                    }
                    if (currentToken.getType() == Token.TokenType.SEMICOLON) {
                        advance();
                        currentState = State.Q0;
                        trace.append(">>> Recovery: Found ';', returning to Q0\n");
                        return;
                    }
                    advance();
                }
                break;

            case Q3:
                while (currentToken.getType() != Token.TokenType.EOF) {
                    if (currentToken.getType() == Token.TokenType.RPAREN) {
                        currentState = State.Q4;
                        trace.append(">>> Recovery: Found ')', moving to Q4\n");
                        return;
                    }
                    if (currentToken.getType() == Token.TokenType.SEMICOLON) {
                        advance();
                        currentState = State.Q0;
                        trace.append(">>> Recovery: Found ';', returning to Q0\n");
                        return;
                    }
                    advance();
                }
                break;

            case Q4:
                if (currentToken.getType() == Token.TokenType.CONSOLE_READLINE) {
                    currentState = State.Q0;
                    trace.append(">>> Recovery: Missing ';', but found new statement, returning to Q0\n");
                    return;
                }
                advance();
                while (currentToken.getType() != Token.TokenType.EOF) {
                    if (currentToken.getType() == Token.TokenType.SEMICOLON) {
                        advance();
                        currentState = State.Q0;
                        trace.append(">>> Recovery: Found ';', returning to Q0\n");
                        return;
                    }
                    if (currentToken.getType() == Token.TokenType.CONSOLE_READLINE) {
                        currentState = State.Q0;
                        trace.append(">>> Recovery: Found new statement, returning to Q0\n");
                        return;
                    }
                    advance();
                }
                break;

            default:
                advance();
        }

        trace.append(">>> Recovery: Reached end of recovery attempt\n");
    }

    public boolean parse() {
        trace.append("=== Parser Trace ===\n");
        trace.append("Starting analysis...\n\n");

        while (currentToken.getType() != Token.TokenType.EOF) {
            State nextState = transition(currentState, currentToken);

            if (nextState == State.ERROR) {
                String expected = getExpectedTokens(currentState, currentToken);
                addError("Expected " + expected, currentToken);
                addTrace("ERROR - attempting recovery");
                recover();
                continue;
            }

            addTrace("Transition to " + nextState);
            currentState = nextState;
            advance();
        }

        boolean inFinalState = (currentState == State.Q0 || currentState == State.QF);

        if (!inFinalState) {
            String expected = getExpectedTokens(currentState, currentToken);
            addError("Unexpected end of file. Expected " + expected, currentToken);
        }

        trace.append("\n=====================================\n");

        if (errors.isEmpty()) {
            trace.append("Analysis completed successfully!\n");
            trace.append("Input string is ACCEPTED ✓\n");
            return true;
        } else {
            trace.append("Analysis completed with errors!\n");
            trace.append(String.format("Found %d error(s):\n\n", errors.size()));
            for (String error : errors) {
                trace.append("  • " + error + "\n");
            }
            trace.append("\nInput string is REJECTED ✗\n");
            return false;
        }
    }

    private State transition(State state, Token token) {
        Token.TokenType type = token.getType();

        switch (state) {
            case Q0:
                if (type == Token.TokenType.CONSOLE_READLINE) {
                    return State.Q1;
                }
                if (type == Token.TokenType.EOF) {
                    return State.QF;
                }
                return State.ERROR;

            case Q1:
                if (type == Token.TokenType.LPAREN) {
                    return State.Q2;
                }
                return State.ERROR;

            case Q2:
                if (type == Token.TokenType.RPAREN) {
                    return State.Q4;
                }
                if (type == Token.TokenType.IDENTIFIER) {
                    String id = token.getValue();
                    if (id.length() > 0 && Character.isLetter(id.charAt(0))) {
                        return State.Q3;
                    }
                }
                return State.ERROR;

            case Q3:
                if (type == Token.TokenType.RPAREN) {
                    return State.Q4;
                }
                return State.ERROR;

            case Q4:
                if (type == Token.TokenType.SEMICOLON) {
                    return State.Q0;
                }
                return State.ERROR;

            case QF:
                return State.ERROR;

            default:
                return State.ERROR;
        }
    }

    public String getTrace() {
        return trace.toString();
    }

    public List<String> getErrors() {
        return errors;
    }

    public static ParseResult parseCode(String code) {
        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            CustomParser parser = new CustomParser(tokens);
            boolean success = parser.parse();

            return new ParseResult(success, parser.getTrace(),
                    success ? null : String.join("\n", parser.getErrors()),
                    tokens, parser.getErrors());

        } catch (LexerException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return new ParseResult(false, "", e.getMessage(), null, errors);
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            String msg = "Unexpected error: " + e.getMessage();
            errors.add(msg);
            return new ParseResult(false, "", msg, null, errors);
        }
    }

    public static class ParseResult {
        private final boolean success;
        private final String trace;
        private final String errorMessage;
        private final List<Token> tokens;
        private final List<String> errors;

        public ParseResult(boolean success, String trace, String errorMessage,
                           List<Token> tokens, List<String> errors) {
            this.success = success;
            this.trace = trace;
            this.errorMessage = errorMessage;
            this.tokens = tokens;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTrace() {
            return trace;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<Token> getTokens() {
            return tokens;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}