package net.v1sion.lox;

import java.util.Iterator;
import java.util.List;

import static net.v1sion.lox.Expr.*;

public class AstPrinter implements Visitor<String> {
    String print(final Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(final Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(final Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(final Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(final Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitCommaExpr(final Comma expr) {
        return parenthesize("progn", expr.expressions.toArray(new Expr[0]));
    }

    @Override
    public String visitTernaryExpr(Ternary expr) {
        return parenthesize("if", expr.cond, expr.thenBranch, expr.elseBranch);
    }

    private String parenthesize(final String name, Expr... exprs) {
        final StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for(final Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        final Expr comma = new Comma(List.of(
                new Binary(
                        new Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Literal(2)
                ),
                new Binary(
                        new Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Literal(3)
                )
        ));

        final Expr expr = new Binary(
                new Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Grouping(new Literal(45.67))
        );

        System.out.println(new AstPrinter().print(comma));
    }
}
