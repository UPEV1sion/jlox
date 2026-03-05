package net.v1sion.lox;

import java.util.List;

import static net.v1sion.lox.Expr.*;
import static net.v1sion.lox.Stmt.*;

/**
 * @author Emanuel Schäffer
 * at 28.02.26
 **/

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    @Override
    public Object visitBinaryExpr(final Binary expr) {
        final Object left  = evaluate(expr.left());
        final Object right = evaluate(expr.right());

        return switch (expr.operator().type()) {
            case GREATER       -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left >= (double)right;
            }
            case LESS          -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left < (double)right;
            }
            case LESS_EQUAL    -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left <= (double)right;
            }
            case BANG_EQUAL    -> !isEqual(left, right);
            case EQUAL_EQUAL   -> isEqual(left, right);
            case MINUS         -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left - (double)right;
            }
            case PLUS  -> {
                if(left instanceof final Double lhs && right instanceof final Double rhs) yield lhs + rhs;
                if(left instanceof String || right instanceof String) yield stringify(left) + stringify(right);
                throw new RuntimeError(expr.operator(), "Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator(), left, right);
                if(right instanceof final Double rhs && rhs == 0.0) throw new RuntimeError(expr.operator(), "Division by zero error.");
                yield (double)left / (double)right;
            }
            case STAR  -> {
                checkNumberOperands(expr.operator(), left, right);
                yield (double)left * (double)right;
            }
            case COMMA -> right;
            default    -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(final Grouping expr) {
        return evaluate(expr.expression());
    }

    @Override
    public Object visitLiteralExpr(final Literal expr) {
        return expr.value();
    }

    @Override
    public Object visitUnaryExpr(final Unary expr) {
        final Object right = evaluate(expr.right());
        return switch (expr.operator().type()) {
            case BANG  -> !isTruthy(right);
            case MINUS -> {
                checkNumberOperand(expr.operator(), right);
                yield -(double)right;
            }
            default    -> null;
        };
    }

    @Override
    public Object visitTernaryExpr(final Ternary expr) {
        return isTruthy(evaluate(expr.cond())) ? evaluate(expr.thenBranch()) : evaluate(expr.elseBranch());
    }

    @Override
    public Object visitVariableExpr(final Variable expr) {
        return environment.get(expr.name()); // TODO maybe throw if no variable is present
    }

    void interpret(final List<Stmt> statements) {
        try {
            for (final Stmt statement : statements) {
                execute(statement);
            }
        } catch (final RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void checkNumberOperand(final Token operator, final Object operand) {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(final Token operator, final Object left, final Object right) {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(final Object object) {
        if (object instanceof final Boolean b) return b;
        if (object instanceof final Double d)  return d != 0.0;
        return object != null;
    }

    private boolean isEqual(final Object a, final Object b) {
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    private String stringify(final Object object) {
        if(object == null) return "nil";

        if(object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(final Expr expr) {
        return expr.accept(this);
    }

    private void execute(final Stmt statement) {
        statement.accept(this);
    }

    void executeBlock(final List<Stmt> statements, final Environment environment) {
        final Environment previous = this.environment;

        try {
            this.environment = environment;

            for(final Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(final Block stmt) {
        executeBlock(stmt.statements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(final Expression stmt) {
        evaluate(stmt.expression());

        return null;
    }

    @Override
    public Void visitPrintStmt(final Print stmt) {
        final Object value = evaluate(stmt.expression());
        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void visitVarStmt(final Var stmt) {
        final Object value = stmt.initializer() != null ? evaluate(stmt.initializer()) : null;
        environment.define(stmt.name().lexeme(), value);

        return null;
    }

    @Override
    public Object visitAssignExpr(final Assign expr) {
        final Object value = evaluate(expr.value());
        environment.assign(expr.name(), value);

        return value;
    }
}
