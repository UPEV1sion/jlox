package net.v1sion.lox;

import java.util.*;

/**
 * @author Emanuel Schäffer
 * at 12.03.26
 **/

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

    private final Interpreter interpreter;
    private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();

    Resolver(final Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void resolve(final Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(final Expr expr) {
        expr.accept(this);
    }

    void resolve(final List<Stmt> statements) {
        for(final Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitAssignExpr(final Expr.Assign expr) {
        return null;
    }

    @Override
    public Void visitBinaryExpr(final Expr.Binary expr) {
        return null;
    }

    @Override
    public Void visitCallExpr(final Expr.Call expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(final Expr.Grouping expr) {
        return null;
    }

    @Override
    public Void visitLiteralExpr(final Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(final Expr.Logical expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(final Expr.Unary expr) {
        return null;
    }

    @Override
    public Void visitTernaryExpr(final Expr.Ternary expr) {
        return null;
    }

    @Override
    public Void visitVariableExpr(final Expr.Variable expr) {
        return null;
    }

    @Override
    public Void visitFunctionExpr(final Expr.Function expr) {
        return null;
    }

    @Override
    public Void visitBlockStmt(final Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements());
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(final Stmt.Expression stmt) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(final Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(final Stmt.If stmt) {
        return null;
    }

    @Override
    public Void visitPrintStmt(final Stmt.Print stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(final Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(final Stmt.Var stmt) {
        return null;
    }

    @Override
    public Void visitWhileStmt(final Stmt.While stmt) {
        return null;
    }

    @Override
    public Void visitBreakStmt(final Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(final Stmt.Continue stmt) {
        return null;
    }
}
