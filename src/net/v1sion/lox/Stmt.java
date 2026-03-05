package net.v1sion.lox;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 05.03.2026
 **/

public interface Stmt {
    <R> R accept(final Visitor<R> visitor);

    interface Visitor<R> {
        R visitBlockStmt(final Block stmt);
        R visitExpressionStmt(final Expression stmt);
        R visitIfStmt(final If stmt);
        R visitPrintStmt(final Print stmt);
        R visitVarStmt(final Var stmt);
        R visitWhileStmt(final While stmt);
    }

    record Block(List<Stmt> statements) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    record Expression(Expr expression) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
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

    record While(Expr condition, Stmt body) implements Stmt {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
}
