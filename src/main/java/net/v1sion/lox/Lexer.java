package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

/**
 * @author Emanuel Schäffer
 * at 10.02.26
 **/

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Lexer(final String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        final char c = advance();
        switch(c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multilineCommentIter();
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
            }
        }
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        final String text = source.substring(start, current);
        final TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())) {
            do {
                advance();
            } while (isDigit(peek()));
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        final StringBuilder sb = new StringBuilder();

        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            if(match('\\')) {
                sb.append(parseEscape(advance()));
            } else {
                sb.append(advance());
            }
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();
        addToken(STRING, sb.toString());
    }

    // This is a recursive implementation of nested comments, the iterative one will be used.
    private void multilineCommentRec() {
        while((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
            if(peek() == '\n') line++;
            if(peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                multilineCommentRec();
            } else {
                advance();
            }
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated multiline comment.");
            return;
        }

        advance();
        advance();
    }

    // This is an iterative implementation of nested comments.
    private void multilineCommentIter() {
        int depth = 1;
        while(depth > 0 && !isAtEnd()) {
            if(peek() == '\n') line++;
            if(peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                depth++;
            } else if(peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                depth--;
            } else {
                advance();
            }
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated multiline comment.");
            return;
        }
    }


    // Normal multiline comments, without nesting
    private void multilineComment() {
        while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated multiline comment.");
            return;
        }

        advance();
        advance();
    }

    private boolean match(final char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char parseEscape(final char c) {
        return switch (c) {
            case 't'  -> '\t';
            case 'b'  -> '\b';
            case 'n'  -> '\n';
            case 'r'  -> '\r';
            case 'f'  -> '\f';
            case '\'' -> '\'';
            case '"'  -> '"';
            case '\\' -> '\\';
            default   -> '\0';
        };
    }

    private boolean isAlpha(final char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(final char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(final TokenType type) {
        addToken(type, null);
    }

    private void addToken(final TokenType type, final Object literal) {
        final String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
