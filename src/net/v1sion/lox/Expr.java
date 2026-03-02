package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 02.03.2026
 **/

public abstract class Expr {
    abstract <R> R accept(final Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBinaryExpr(final Binary expr);
        R visitGroupingExpr(final Grouping expr);
        R visitLiteralExpr(final Literal expr);
        R visitUnaryExpr(final Unary expr);
        R visitTernaryExpr(final Ternary expr);
    }

    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Ternary extends Expr {
        final Expr cond;
        final Expr thenBranch;
        final Expr elseBranch;

        Ternary(Expr cond, Expr thenBranch, Expr elseBranch) {
            this.cond = cond;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }
}
