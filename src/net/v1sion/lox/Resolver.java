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

    private void resolveFunction(final Stmt.Function function) {
        beginScope();
        for(final Token param : function.function().params()) {
            declare(param);
            define(param);
        }
        resolve(function.function().body());
        endScope();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(final Token name) {
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), false);
    }

    private void define(final Token name) {
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    private void resolveLocal(final Expr expr, final Token name) {
        int depth = 0;
        for(final Iterator<Map<String, Boolean>> it = scopes.descendingIterator(); it.hasNext(); depth++) {
            if(it.next().containsKey(name.lexeme())) {
                interpreter.resolve(expr, depth);
                return;
            }
        }
    }

    @Override
    public Void visitAssignExpr(final Expr.Assign expr) {
        resolve(expr.value());
        resolveLocal(expr, expr.name());

        return null;
    }

    @Override
    public Void visitBinaryExpr(final Expr.Binary expr) {
        resolve(expr.left());
        resolve(expr.right());

        return null;
    }

    @Override
    public Void visitCallExpr(final Expr.Call expr) {
        resolve(expr.callee());

        for(final Expr argument : expr.arguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(final Expr.Grouping expr) {
        resolve(expr.expression());

        return null;
    }

    @Override
    public Void visitLiteralExpr(final Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(final Expr.Logical expr) {
        resolve(expr.left());
        resolve(expr.right());

        return null;
    }

    @Override
    public Void visitUnaryExpr(final Expr.Unary expr) {
        resolve(expr.right());

        return null;
    }

    @Override
    public Void visitTernaryExpr(final Expr.Ternary expr) {
        resolve(expr.cond());
        resolve(expr.thenBranch());
        resolve(expr.elseBranch());

        return null;
    }

    @Override
    public Void visitVariableExpr(final Expr.Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().get(expr.name().lexeme()) == Boolean.FALSE) {
            Lox.error(expr.name(), "Can't read load variable which is its own initializer.");
        }

        resolveLocal(expr, expr.name());

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
        resolve(stmt.expression());

        return null;
    }

    @Override
    public Void visitFunctionStmt(final Stmt.Function stmt) {
        declare(stmt.name());
        define(stmt.name());
        resolveFunction(stmt);

        return null;
    }

    @Override
    public Void visitIfStmt(final Stmt.If stmt) {
        resolve(stmt.condition());
        resolve(stmt.thenBranch());
        if(stmt.elseBranch() != null) resolve(stmt.elseBranch());

        return null;
    }

    @Override
    public Void visitPrintStmt(final Stmt.Print stmt) {
        resolve(stmt.expression());

        return null;
    }

    @Override
    public Void visitReturnStmt(final Stmt.Return stmt) {
        if(stmt.value() != null) resolve(stmt.value());

        return null;
    }

    @Override
    public Void visitVarStmt(final Stmt.Var stmt) {
        declare(stmt.name());
        if(stmt.initializer() != null) {
            resolve(stmt.initializer());
        }
        define(stmt.name());

        return null;
    }

    @Override
    public Void visitWhileStmt(final Stmt.While stmt) {
        resolve(stmt.condition());
        resolve(stmt.body());

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
