package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 02.03.2026
 **/

public abstract class Stmt {
    abstract <R> R accept(final Visitor<R> visitor);

    public interface Visitor<R> {
        R visitExpressionStmt(final Expression stmt);
        R visitPrintStmt(final Print stmt);
    }

    public static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept (final Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }
}
