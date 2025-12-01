package ru.nsu.veretennikov.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Синтаксический анализатор на основе детерминированного конечного автомата (ДКА)
 * Реализует автоматную грамматику типа 3
 *
 * Грамматика:
 * <Program> → Console.ReadLine <P1> | ε
 * <P1> → ( <P2>
 * <P2> → ) <P3> | <Letter> <Arg>
 * <Arg> → <Letter> <Arg> | <Digit> <Arg> | _ <Arg> | ) <P3>
 * <P3> → ; <Program>
 */
public class Parser {

    /**
     * Состояния детерминированного конечного автомата
     */
    private enum State {
        Q0,      // <Program> - начальное состояние
        Q1,      // <P1> - после Console.ReadLine
        Q2,      // <P2> - после (
        Q3,      // <Arg> - внутри аргумента
        Q4,      // <P3> - после )
        QF,      // финальное состояние
        ERROR    // состояние ошибки
    }

    private List<Token> tokens;
    private int position;
    private Token currentToken;
    private State currentState;
    private StringBuilder trace; // для трассировки работы автомата
    private List<String> errors; // список всех ошибок
    private int errorCount;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.currentToken = tokens.get(0);
        this.currentState = State.Q0;
        this.trace = new StringBuilder();
        this.errors = new ArrayList<>();
        this.errorCount = 0;
    }

    /**
     * Получает следующий токен
     */
    private void advance() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        }
    }

    /**
     * Добавляет запись в трассировку
     */
    private void addTrace(String action) {
        trace.append(String.format("State: %s, Token: %s (%s) -> %s\n",
                currentState,
                currentToken.getType(),
                currentToken.getValue(),
                action));
    }

    /**
     * Добавляет ошибку в список
     */
    private void addError(String message, Token token) {
        errorCount++;
        String errorMsg;

        // Специальная обработка для опечаток в Console.ReadLine
        if (currentState == State.Q0 && token.getType() == Token.TokenType.IDENTIFIER) {
            String val = token.getValue();
            if (val.contains(".")) {
                // Это похоже на Console.ReadLine с опечаткой
                errorMsg = String.format("Error #%d at line %d, column %d: Expected 'Console.ReadLine', found '%s'",
                        errorCount,
                        token.getLine(),
                        token.getColumn(),
                        val);
            } else {
                errorMsg = String.format("Error #%d at line %d, column %d: %s (found: '%s')",
                        errorCount,
                        token.getLine(),
                        token.getColumn(),
                        message,
                        val);
            }
        } else {
            errorMsg = String.format("Error #%d at line %d, column %d: %s (found: '%s')",
                    errorCount,
                    token.getLine(),
                    token.getColumn(),
                    message,
                    token.getValue().isEmpty() ? token.getType().toString() : token.getValue());
        }

        errors.add(errorMsg);
        trace.append(">>> " + errorMsg + "\n");
    }

    /**
     * Получает ожидаемые токены для текущего состояния
     */
    private String getExpectedTokens(State state, Token token) {
        switch (state) {
            case Q0:
                // Проверяем, не опечатка ли это в Console.ReadLine
                if (token.getType() == Token.TokenType.IDENTIFIER) {
                    String val = token.getValue();
                    if (val.startsWith("onsole") || val.startsWith("Console.") ||
                            val.contains(".ReadLine") || val.contains("ReadLine")) {
                        return "Console.ReadLine (found typo: '" + val + "')";
                    }
                }
                return "Console.ReadLine or EOF (empty program)";
            case Q1:
                return "'('";
            case Q2:
                return "')' or identifier (variable name)";
            case Q3:
                return "')' (this should not happen - identifier is one token)";
            case Q4:
                return "';'";
            default:
                return "unknown";
        }
    }

    /**
     * Пытается восстановиться после ошибки
     */
    private void recover() {
        // Стратегия восстановления зависит от текущего состояния
        trace.append(">>> Recovery: Attempting to recover from error in state " + currentState + "\n");

        switch (currentState) {
            case Q0:
                // Ожидали Console.ReadLine - пропускаем неизвестный токен
                // Но если это похоже на опечатку в Console.ReadLine, пытаемся продолжить как будто это был Console.ReadLine
                if (currentToken.getType() == Token.TokenType.IDENTIFIER) {
                    String val = currentToken.getValue();
                    if (val.contains(".")) {
                        // Это похоже на Console.ReadLine с опечаткой
                        // Переходим к состоянию Q1 (ожидаем '(')
                        advance();
                        currentState = State.Q1;
                        trace.append(">>> Recovery: Treating '" + val + "' as Console.ReadLine, moving to Q1\n");
                        return;
                    }
                }
                advance();
                break;

            case Q1:
                // Ожидали '(' - пропускаем токен и ищем '(' или начало нового оператора
                if (currentToken.getType() == Token.TokenType.LPAREN) {
                    // Нашли '(', продолжаем нормально
                    return;
                }
                // Ищем либо '(', либо ';' чтобы начать заново
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
                // Ожидали ')' или идентификатор - пропускаем и ищем ')'
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
                // Ожидали ')' - ищем ')'
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
                // Ожидали ';' - это самая частая ошибка
                if (currentToken.getType() == Token.TokenType.CONSOLE_READLINE) {
                    // Пользователь забыл ';' и начал новый оператор
                    currentState = State.Q0;
                    trace.append(">>> Recovery: Missing ';', but found new statement, returning to Q0\n");
                    return;
                }
                // Пропускаем токен и пытаемся найти ';' или новый оператор
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

    /**
     * Основной метод парсинга - реализация ДКА с обработкой всех ошибок
     */
    public boolean parse() {
        trace.append("=== Parser Trace ===\n");
        trace.append("Starting analysis...\n\n");

        while (currentToken.getType() != Token.TokenType.EOF) {
            State nextState = transition(currentState, currentToken);

            if (nextState == State.ERROR) {
                // Записываем ошибку
                String expected = getExpectedTokens(currentState, currentToken);
                addError("Expected " + expected, currentToken);
                addTrace("ERROR - attempting recovery");

                // Пытаемся восстановиться
                recover();
                continue;
            }

            addTrace("Transition to " + nextState);
            currentState = nextState;
            advance();
        }

        // Проверяем, что мы в допускающем состоянии
        boolean inFinalState = (currentState == State.Q0 || currentState == State.QF);

        if (!inFinalState) {
            // Программа закончилась в недопустимом состоянии
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

    /**
     * Функция переходов детерминированного конечного автомата
     * Реализует таблицу переходов из документации
     */
    private State transition(State state, Token token) {
        Token.TokenType type = token.getType();

        switch (state) {
            case Q0: // <Program>
                if (type == Token.TokenType.CONSOLE_READLINE) {
                    return State.Q1;
                }
                if (type == Token.TokenType.EOF) {
                    return State.QF; // пустая программа допустима
                }
                return State.ERROR;

            case Q1: // <P1> - после Console.ReadLine
                if (type == Token.TokenType.LPAREN) {
                    return State.Q2;
                }
                return State.ERROR;

            case Q2: // <P2> - после (
                if (type == Token.TokenType.RPAREN) {
                    return State.Q4; // без аргумента
                }
                if (type == Token.TokenType.IDENTIFIER) {
                    // Проверяем, что идентификатор начинается с буквы
                    String id = token.getValue();
                    if (id.length() > 0 && Character.isLetter(id.charAt(0))) {
                        return State.Q3; // с аргументом
                    }
                }
                return State.ERROR;

            case Q3: // <Arg> - внутри аргумента
                if (type == Token.TokenType.RPAREN) {
                    return State.Q4;
                }
                // В лексере мы уже проверили корректность идентификатора
                // Здесь этот случай не должен возникнуть, т.к. весь идентификатор
                // читается как один токен
                return State.ERROR;

            case Q4: // <P3> - после )
                if (type == Token.TokenType.SEMICOLON) {
                    return State.Q0; // возврат к началу программы
                }
                return State.ERROR;

            case QF: // финальное состояние
                return State.ERROR; // не должны сюда попасть во время парсинга

            default:
                return State.ERROR;
        }
    }

    /**
     * Получает трассировку работы парсера
     */
    public String getTrace() {
        return trace.toString();
    }

    /**
     * Получает список ошибок
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Главный метод для запуска парсера
     */
    public static ParseResult parseCode(String code) {
        try {
            // Лексический анализ
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            // Синтаксический анализ
            Parser parser = new Parser(tokens);
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

    /**
     * Класс для результата парсинга
     */
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