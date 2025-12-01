package ru.nsu.veretennikov.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Лексический анализатор (токенизатор)
 * Преобразует исходный код в последовательность токенов
 */
public class Lexer {
    private final String input;
    private int position;
    private int line;
    private int column;
    private char currentChar;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.currentChar = input.length() > 0 ? input.charAt(0) : '\0';
    }

    /**
     * Продвигает позицию на один символ вперед
     */
    private void advance() {
        if (currentChar == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        position++;
        if (position < input.length()) {
            currentChar = input.charAt(position);
        } else {
            currentChar = '\0';
        }
    }

    /**
     * Пропускает пробельные символы
     */
    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    /**
     * Просматривает следующий символ без изменения позиции
     */
    private char peek(int offset) {
        int peekPos = position + offset;
        if (peekPos < input.length()) {
            return input.charAt(peekPos);
        }
        return '\0';
    }

    /**
     * Проверяет, соответствует ли текущая позиция заданной строке
     */
    private boolean matchString(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (peek(i) != str.charAt(i)) {
                return false;
            }
        }
        // Проверяем, что после строки нет буквы или цифры (граница слова)
        char nextChar = peek(str.length());
        return !Character.isLetterOrDigit(nextChar) && nextChar != '_';
    }

    /**
     * Читает точное совпадение строки
     */
    private Token readExactMatch(String match, Token.TokenType type) {
        int startLine = line;
        int startColumn = column;

        for (int i = 0; i < match.length(); i++) {
            advance();
        }

        return new Token(type, match, startLine, startColumn);
    }

    /**
     * Читает идентификатор или ключевое слово, включая точки
     * Например: Console.ReadLine, onsole.ReadLine, Console.eadLine
     */
    private Token readWordWithDots() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        // Читаем первую часть (буквы, цифры, подчеркивания)
        while (currentChar != '\0' &&
                (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append(currentChar);
            advance();
        }

        // Проверяем, есть ли точка
        if (currentChar == '.') {
            sb.append(currentChar);
            advance();

            // Читаем вторую часть после точки
            while (currentChar != '\0' &&
                    (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
                sb.append(currentChar);
                advance();
            }
        }

        String word = sb.toString();

        // Проверяем, является ли это Console.ReadLine
        if (word.equals("Console.ReadLine")) {
            return new Token(Token.TokenType.CONSOLE_READLINE, word, startLine, startColumn);
        } else {
            // Это идентификатор или ошибочное ключевое слово
            return new Token(Token.TokenType.IDENTIFIER, word, startLine, startColumn);
        }
    }

    /**
     * Читает простой идентификатор без точек
     */
    private Token readSimpleIdentifier() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (currentChar != '\0' &&
                (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append(currentChar);
            advance();
        }

        return new Token(Token.TokenType.IDENTIFIER, sb.toString(), startLine, startColumn);
    }

    /**
     * Получает следующий токен
     */
    public Token getNextToken() {
        while (currentChar != '\0') {
            // Пропускаем пробелы
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            // Однострочный комментарий //
            if (currentChar == '/' && peek(1) == '/') {
                while (currentChar != '\0' && currentChar != '\n') {
                    advance();
                }
                continue;
            }

            // Многострочный комментарий /* */
            if (currentChar == '/' && peek(1) == '*') {
                advance(); // пропускаем /
                advance(); // пропускаем *
                while (currentChar != '\0') {
                    if (currentChar == '*' && peek(1) == '/') {
                        advance(); // пропускаем *
                        advance(); // пропускаем /
                        break;
                    }
                    advance();
                }
                continue;
            }

            // Левая скобка
            if (currentChar == '(') {
                Token token = new Token(Token.TokenType.LPAREN, "(", line, column);
                advance();
                return token;
            }

            // Правая скобка
            if (currentChar == ')') {
                Token token = new Token(Token.TokenType.RPAREN, ")", line, column);
                advance();
                return token;
            }

            // Точка с запятой
            if (currentChar == ';') {
                Token token = new Token(Token.TokenType.SEMICOLON, ";", line, column);
                advance();
                return token;
            }

            // Слово, начинающееся с 'C' - может быть Console.ReadLine
            if (currentChar == 'C') {
                return readWordWithDots();
            }

            // Слово, начинающееся с маленькой буквы - может быть опечатка в Console
            if (Character.isLowerCase(currentChar)) {
                // Проверяем, не пытается ли пользователь написать onsole.ReadLine
                if (currentChar == 'o' && matchString("onsole.ReadLine")) {
                    // Это опечатка в Console.ReadLine (забыли C)
                    return readWordWithDots();
                }
                return readWordWithDots();
            }

            // Идентификатор (начинается с буквы или _)
            if (Character.isLetter(currentChar) || currentChar == '_') {
                return readWordWithDots();
            }

            // Неизвестный символ
            throw new LexerException("Unexpected character: '" + currentChar + "'", line, column);
        }

        // Конец файла
        return new Token(Token.TokenType.EOF, "", line, column);
    }

    /**
     * Получает все токены из исходного кода
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            token = getNextToken();
            tokens.add(token);
        } while (token.getType() != Token.TokenType.EOF);

        return tokens;
    }
}