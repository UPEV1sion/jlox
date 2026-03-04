package net.v1sion.lox;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 04.03.2026
 **/

public interface Expr {
    <R> R accept(final Visitor<R> visitor);

    interface Visitor<R> {
        R visitAssignExpr(final Assign expr);
        R visitBinaryExpr(final Binary expr);
        R visitGroupingExpr(final Grouping expr);
        R visitLiteralExpr(final Literal expr);
        R visitUnaryExpr(final Unary expr);
        R visitTernaryExpr(final Ternary expr);
        R visitVariableExpr(final Variable expr);
    }

    record Assign(Token name, Expr value) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    record Binary(Expr left, Token operator, Expr right) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    record Grouping(Expr expression) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    record Literal(Object value) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    record Unary(Token operator, Expr right) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    record Ternary(Expr cond, Expr thenBranch, Expr elseBranch) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    record Variable(Token name) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }
}
