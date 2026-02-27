package net.v1sion.lox;

import java.util.Iterator;
import java.util.List;

import static net.v1sion.lox.Expr.*;

public class RpnPrinter implements Visitor<String> {

    String print(final Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(final Binary expr) {
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(final Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(final Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(final Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitCommaExpr(final Comma expr) {
        final StringBuilder sb = new StringBuilder();
        Iterator<Expr> iterator = expr.expressions.iterator();
        sb.append(iterator.next().accept(this));
        while(iterator.hasNext()) {
            sb.append(" pop ").append(iterator.next().accept(this));
        }
        return sb.toString();
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
