package net.v1sion.lox;

import java.util.ArrayList;
import java.util.List;

import static net.v1sion.lox.TokenType.*;

/**
 * @author Emanuel Schäffer
 * at 23.02.26
 **/

public class Parser {
    private boolean allowExpression;
    private boolean foundExpression;
    private int loopDepth;

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current;

    Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        final List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    Object parseRepl() {
        allowExpression = true;
        final List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());

            if(foundExpression) {
                final Stmt last = statements.getLast();
                return ((Stmt.Expression) last).expression();
            }

            allowExpression = false;
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if(match(VAR)) return varDeclaration();

            return statement();
        } catch (final ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if(match(FOR)) return forStatement();
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(WHILE)) return whileStatement();
        if(match(BREAK)) return breakStatement();
        if(match(CONTINUE)) return continueStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        final Stmt initializer;
        if(match(SEMICOLON)) {
            initializer = null;
        } else if(match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = !check(SEMICOLON) ? expression() : null;
        consume(SEMICOLON, "Expect ';' after loop condition");

        final Expr increment = !check(RIGHT_PAREN) ? expression() : null;
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        try {
            loopDepth++;
            Stmt body = statement();

            if(increment != null) body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
            if(condition == null) condition = new Expr.Literal(true);
            body = new Stmt.While(condition, body);
            if(initializer != null) body = new Stmt.Block(List.of(initializer, body));

            return body;
        } finally {
            loopDepth--;
        }
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        final Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        final Stmt thenBranch = statement();
        final Stmt elseBranch = match(ELSE) ? statement() : null;

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        final Expr value = expression();
        consume(SEMICOLON, "Expect ';' after expression");

        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        final Token name = consume(IDENTIFIER, "Expect variable name.");

        final Expr initializer = match(EQUAL) ? expression() : null;
        consume(SEMICOLON, "Expect ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        final Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");

        try {
            loopDepth++;
            final Stmt body = statement();

            return new Stmt.While(condition, body);
        } finally {
            loopDepth--;
        }
    }

    private Stmt breakStatement() {
        if(loopDepth == 0) throw error(previous(), "Must be inside a loop to use 'break'.");

        consume(SEMICOLON, "Expect ';' after 'break'.");
        return new Stmt.Break();
    }

    private Stmt continueStatement() {
        if(loopDepth == 0) throw error(previous(), "Must be inside a loop to use 'continue'.");

        consume(SEMICOLON, "Expect ';' after 'continue'.");
        return new Stmt.Continue();
    }

    private Stmt expressionStatement() {
        final Expr expr = expression();

        if(allowExpression && isAtEnd()) {
            foundExpression = true;
        } else {
            consume(SEMICOLON, "Expect ';' after expression");
        }

        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        final List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = assignment();

        while (match(COMMA)) {
            final Token operator = previous();
            final Expr right = assignment();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr assignment() {
        final Expr expr = ternary();

        if(match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
            final Token operator = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable(final Token name)) {
                final Token binaryOperator = switch (operator.type()) {
                    case PLUS_EQUAL  -> new Token(PLUS,  "+", null, operator.line());
                    case MINUS_EQUAL -> new Token(MINUS, "-", null, operator.line());
                    case STAR_EQUAL  -> new Token(STAR,  "*", null, operator.line());
                    case SLASH_EQUAL -> new Token(SLASH, "/", null, operator.line());
                    default          -> null;
                };

                if(binaryOperator != null) value = new Expr.Binary(expr, binaryOperator, value);

                return new Expr.Assign(name, value);
            }

            error(operator, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        final Expr expr = or();

        if (!match(QUESTION_MARK)) return expr;

        final Expr thenBranch = expression();
        consume(COLON, "Expect ':' in ternary operator");
        final Expr elseBranch = ternary();

        return new Expr.Ternary(expr, thenBranch, elseBranch);
    }

    private Expr or() {
        Expr expr = and();

        while(match(OR)) {
            final Token operator = previous();
            final Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(match(AND)) {
            final Token operator = previous();
            final Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            final Token operator = previous();
            final Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            final Token operator = previous();
            final Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            final Token operator = previous();
            final Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            final Token operator = previous();
            final Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (!match(BANG, MINUS)) return primary();

        final Token operator = previous();
        final Expr right = unary();
        return new Expr.Unary(operator, right);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE))  return new Expr.Literal(true);
        if (match(NIL))   return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());

        if(match(IDENTIFIER)) return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            final Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            error(previous(), "Missing left hand operand");
            equality();
            return null;
        }

        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            error(previous(), "Missing left hand operand");
            comparison();
            return null;
        }

        if (match(PLUS)) {
            error(previous(), "Missing left hand operand");
            term();
            return null;
        }

        if (match(STAR, SLASH)) {
            error(previous(), "Missing left hand operand");
            factor();
            return null;
        }

        throw error(peek(), "Expect expression");
    }

    private boolean match(final TokenType... types) {
        for (final TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(final TokenType type, final String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(final TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
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
        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type()) {
                case CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE -> {
                    return;
                }
            }

            advance();
        }
    }
}
