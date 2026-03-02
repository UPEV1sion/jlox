package net.v1sion.lox;

import static net.v1sion.lox.Expr.*;

/**
 * @author Emanuel Schäffer
 * at 28.02.26
 **/

public class Interpreter implements Visitor<Object> {
    @Override
    public Object visitBinaryExpr(final Binary expr) {
        final Object left  = evaluate(expr.left);
        final Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case GREATER       -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left >= (double)right;
            }
            case LESS          -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left < (double)right;
            }
            case LESS_EQUAL    -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left <= (double)right;
            }
            case BANG_EQUAL    -> !isEqual(left, right);
            case EQUAL_EQUAL   -> isEqual(left, right);
            case MINUS         -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left - (double)right;
            }
            case PLUS  -> {
                if(left instanceof final Double lhs && right instanceof final Double rhs) yield lhs + rhs;
                if(left instanceof final String lhs && right instanceof final String rhs) yield lhs + rhs;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left / (double)right;
            }
            case STAR  -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double)left * (double)right;
            }
            case COMMA -> right;
            default    -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(final Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(final Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(final Unary expr) {
        final Object right = evaluate(expr.right);
        return switch (expr.operator.type) {
            case BANG  -> !isTruthy(right);
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double)right;
            }
            default    -> null;
        };
    }

    @Override
    public Object visitTernaryExpr(final Ternary expr) {
        return isTruthy(evaluate(expr.cond)) ? evaluate(expr.thenBranch) : evaluate(expr.elseBranch);
    }

    void interpret(final Expr expr) {
        try {
            final Object value = evaluate(expr);
            System.out.println(stringify(value));
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
        if (object instanceof final Double d) return d != 0.0;
        return object != null;
    }

    private boolean isEqual(final Object a, final Object b) {
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    private String stringify(final Object object) {
        if(object == null) return "";

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
}
