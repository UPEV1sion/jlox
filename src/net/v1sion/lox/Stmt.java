package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 03.03.2026
 **/

public interface Stmt {
    <R> R accept(final Visitor<R> visitor);

    interface Visitor<R> {
        R visitExpressionStmt(final Expression stmt);
        R visitPrintStmt(final Print stmt);
        R visitVarStmt(final Var stmt);
    }

    record Expression(Expr expression) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    record Print(Expr expression) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    record Var(Token name, Expr initializer) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }
}
