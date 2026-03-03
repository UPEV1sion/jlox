package net.v1sion.lox;

import static net.v1sion.lox.Expr.*;

/**
 * @author Emanuel Schäffer
 * at 22.02.26
 **/

public class RpnPrinter implements Visitor<String> {

    String print(final Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(final Binary expr) {
        return expr.left().accept(this) + " " + expr.right().accept(this) + " " + expr.operator().lexeme();
    }

    @Override
    public String visitGroupingExpr(final Grouping expr) {
        return expr.expression().accept(this);
    }

    @Override
    public String visitLiteralExpr(final Literal expr) {
        if (expr.value() == null) return "nil";
        return expr.value().toString();
    }

    @Override
    public String visitUnaryExpr(final Unary expr) {
        return expr.operator().lexeme() + expr.right().accept(this);
    }

    @Override
    public String visitTernaryExpr(final Ternary expr) {
        final String ternary = String.join(" ",
                                           expr.cond().accept(this),
                                           expr.thenBranch().accept(this),
                                           expr.elseBranch().accept(this)
        );

        return ternary + " ?:";
    }

    public static void main(String[] args) {
        final Expr comma = new Binary(
                new Binary(
                        new Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Literal(2)
                ),
                new Token(TokenType.COMMA, ",", null, 1),
                new Binary(
                        new Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Literal(3)
                )
        );

        final Expr expr = new Binary(
                new Grouping(
                        new Binary(
                                new Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Literal(2)
                        )
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Grouping(
                        new Binary(
                                new Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Literal(3)
                        )
                )
        );

        System.out.println(new RpnPrinter().print(comma));
    }
}
