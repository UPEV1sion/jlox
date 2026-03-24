package net.v1sion.lox;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 21.02.26, revision 24.03.2026
 **/

public interface Expr {
    <R> R accept(final Visitor<R> visitor);

    interface Visitor<R> {
        R visitAssignExpr(final Assign expr);
        R visitBinaryExpr(final Binary expr);
        R visitCallExpr(final Call expr);
        R visitGetExpr(final Get expr);
        R visitSetExpr(final Set expr);
        R visitThisExpr(final This expr);
        R visitGroupingExpr(final Grouping expr);
        R visitLiteralExpr(final Literal expr);
        R visitLogicalExpr(final Logical expr);
        R visitUnaryExpr(final Unary expr);
        R visitTernaryExpr(final Ternary expr);
        R visitVariableExpr(final Variable expr);
        R visitFunctionExpr(final Function expr);
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

    record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    record Get(Expr object, Token name) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    record Set(Expr object, Token name, Expr value) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    record This(Token keyword) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
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

    record Logical(Expr left, Token operator, Expr right) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
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

    record Function(List<Token> params, List<Stmt> body) implements Expr {

        @Override
        public <R> R accept (final Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }
    }
}
