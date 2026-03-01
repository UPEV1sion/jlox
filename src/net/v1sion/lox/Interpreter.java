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
            case GREATER       -> (double)left > (double)right;
            case GREATER_EQUAL -> (double)left >= (double)right;
            case LESS          -> (double)left < (double)right;
            case LESS_EQUAL    -> (double)left <= (double)right;
            case BANG_EQUAL    -> (double)left != (double)right;
            case EQUAL_EQUAL   -> (double)left == (double)right;
            case MINUS         -> (double)left - (double)right;
            case PLUS  -> {
                if(left instanceof final Double lhs && right instanceof final Double rhs) yield lhs + rhs;
                if(left instanceof final String lhs && right instanceof final String rhs) yield lhs + rhs;
                yield null; // TODO this should be an error
            }
            case SLASH -> (double)left / (double)right;
            case STAR  -> (double)left * (double)right;
            default    -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(final Grouping expr) {
        return null;
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
            case MINUS -> -(double)right;
            default    -> null;
        };
    }

    @Override
    public Object visitTernaryExpr(final Ternary expr) {
        return null;
    }

    private boolean isTruthy(final Object object) {
        if (object instanceof final Boolean b) return b;
        return object != null;
    }

    private boolean isEqual(final Object a, final Object b) {
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    private Object evaluate(final Expr expr) {
        return expr.accept(this);
    }
}
