package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 04.02.26
 **/

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    public Token(final TokenType type, final String lexeme, final Object literal, final int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
