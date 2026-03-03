package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 04.02.26
 **/

public record Token(TokenType type, String lexeme, Object literal, int line) {

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
