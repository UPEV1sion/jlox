package net.v1sion.lox;

import java.util.ArrayList;
import java.util.List;

import static net.v1sion.lox.TokenType.*;

/**
 * @author Emanuel Schäffer
 * at 23.02.26
 **/

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current;

    Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (final ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = ternary();
        if(!match(COMMA)) return expr;

        final List<Expr> exprs = new ArrayList<>();
        exprs.add(expr);

        do {
            final Expr right = ternary();
            exprs.add(right);
        } while(match(COMMA));

        return new Expr.Comma(exprs);
    }

    private Expr ternary() {
        Expr expr = equality();

        if(!match(QUESTION_MARK)) return expr;

        Expr thenBranch = ternary();
        consume(COLON, "Expect ':' in ternary operator");
        Expr elseBranch = ternary();

        return new Expr.Ternary(expr, thenBranch, elseBranch);
    }

    private Expr equality() {
        Expr expr = comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            final Token operator = previous();
            final Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            final Token operator = previous();
            final Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(MINUS, PLUS)) {
            final Token operator = previous();
            final Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(SLASH, STAR)) {
            final Token operator = previous();
            final Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            final Token operator = previous();
            final Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE))  return new Expr.Literal(true);
        if(match(NIL))   return new Expr.Literal(null);

        if(match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

        if(match(LEFT_PAREN)) {
            final Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

    private boolean match(final TokenType... types) {
        for(final TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(final TokenType type, final String message) {
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(final TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(final Token token, final String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if(previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE -> {return;}
            }
        }

        advance();
    }
}
