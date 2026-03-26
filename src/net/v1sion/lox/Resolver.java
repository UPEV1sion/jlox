package net.v1sion.lox;

import java.util.*;

/**
 * @author Emanuel Schäffer
 * at 12.03.26
 **/

// TODO more checks like "break", "continue" inside loop
// TODO maybe an light unused variable check

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

    private final Interpreter interpreter;
    private final Deque<Map<String, Variable>> scopes = new ArrayDeque<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    private record Variable(Token name, VariableState state, int slot) {
        public Variable withState(final VariableState state) {
            return new Variable(name, state, slot);
        }
    }

    private enum VariableState {
        DECLARED,
        DEFINED,
        READ,
    }

    private enum FunctionType {
        INITIALIZER,
        FUNCTION,
        METHOD,
        NONE,
    }

    private enum ClassType {
        CLASS,
        NONE,
    }

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

    private void resolveFunction(final Stmt.Function function, final FunctionType type) {
        final FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for(final Token param : function.function().params()) {
            declare(param);
            define(param);
        }
        resolve(function.function().body());
        endScope();

        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        for(final Map.Entry<String, Variable> var : scopes.pop().entrySet()) {
            if(var.getValue().state() != VariableState.READ) {
                Lox.warning(var.getValue().name().line(), "Unused local variable: '" + var.getKey() + "'.");
            }
        }
    }

    private void declare(final Token name) {
        if(scopes.isEmpty()) return;

        final Map<String, Variable> scope = scopes.peek();
        if(scope.containsKey(name.lexeme())) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme(), new Variable(name, VariableState.DECLARED, scope.size()));
    }

    private void define(final Token name) {
        if(scopes.isEmpty()) return;
        final Map<String, Variable> scope = scopes.peek();
        final Variable old = scope.get(name.lexeme());
        assert old != null : "FATAL: Variable define before declaration.";
        scope.put(name.lexeme(), old.withState(VariableState.DEFINED));
    }

    private void resolveLocal(final Expr expr, final Token name, final boolean isRead) {
        int depth = 0;

        for (final Map<String, Variable> scope : scopes) {
            if (scope.containsKey(name.lexeme())) {
                final Variable var = scope.get(name.lexeme());
                interpreter.resolve(expr, depth, var.slot());
                if(isRead) scope.put(name.lexeme(), new Variable(name, VariableState.READ, var.slot()));

                return;
            }

            depth++;
        }
    }

    @Override
    public Void visitAssignExpr(final Expr.Assign expr) {
        resolve(expr.value());
        resolveLocal(expr, expr.name(), false);

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
    public Void visitGetExpr(final Expr.Get expr) {
        resolve(expr.object());

        return null;
    }

    @Override
    public Void visitSetExpr(final Expr.Set expr) {
        resolve(expr.value());
        resolve(expr.object());

        return null;
    }

    @Override
    public Void visitThisExpr(final Expr.This expr) {
        if(currentClass == ClassType.NONE) {
            Lox.error(expr.keyword(), "Can't use 'this' outside of an class.");
            return null;
        }

        resolveLocal(expr, expr.keyword(), true);
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
        if(!scopes.isEmpty()
                && scopes.peek().containsKey(expr.name().lexeme())
                && scopes.peek().get(expr.name().lexeme()).state() == VariableState.DECLARED) {
            Lox.error(expr.name(), "Can't read load variable which is its own initializer.");
        }

        resolveLocal(expr, expr.name(), true);

        return null;
    }

    @Override
    public Void visitFunctionExpr(final Expr.Function expr) {
        final FunctionType enclosingFunction = currentFunction;
        currentFunction = FunctionType.FUNCTION;

        beginScope();
        for(final Token param : expr.params()) {
            declare(param);
            define(param);
        }
        resolve(expr.body());
        endScope();

        currentFunction = enclosingFunction;

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
    public Void visitClassStmt(final Stmt.Class stmt) {
        final ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name());
        define(stmt.name());

        beginScope();
        final Token thisToken = new Token(TokenType.THIS, "this", null, stmt.name().line());
        scopes.peek().put("this", new Variable(thisToken, VariableState.READ, scopes.peek().size()));

        for (final Stmt.Function method : stmt.methods()) {
            FunctionType declaration = FunctionType.METHOD;
            if(method.name().lexeme().equals("init")) declaration = FunctionType.INITIALIZER;
            resolveFunction(method, declaration);
        }

        endScope();
        currentClass = enclosingClass;

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
        resolveFunction(stmt, FunctionType.FUNCTION);

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
        if(currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword(), "Can't return from top-level code.");
        }

        if(stmt.value() != null){
            if(currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword(), "Can't return a value from an initializer.");
            }

            resolve(stmt.value());
        }

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
