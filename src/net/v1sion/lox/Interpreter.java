package net.v1sion.lox;

import net.v1sion.lox.callables.LoxCallable;
import net.v1sion.lox.callables.LoxClass;
import net.v1sion.lox.callables.LoxFunction;
import net.v1sion.lox.callables.LoxInstance;
import net.v1sion.lox.exceptions.BreakException;
import net.v1sion.lox.exceptions.ContinueException;
import net.v1sion.lox.exceptions.ReturnException;
import net.v1sion.lox.exceptions.RuntimeError;

import java.util.*;

import static net.v1sion.lox.Expr.*;
import static net.v1sion.lox.Stmt.*;

/**
 * @author Emanuel Schäffer
 * at 28.02.26
 **/

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public final Map<String, Object> globals = new HashMap<>();
    private Environment environment;
    private final Map<Expr, Integer> locals = new IdentityHashMap<>();
    private final Map<Expr, Integer> slots  = new IdentityHashMap<>();
    private static final Object uninitialized = new Object();

    Interpreter () {
        globals.put("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(final Interpreter interpreter, final List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

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
    public Object visitCallExpr(final Call expr) {
        final Object callee = evaluate(expr.callee());
        final List<Object> arguments = new ArrayList<>();
        for(final Expr argument : expr.arguments()) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof final LoxCallable function)) {
            throw new RuntimeError(expr.paren(), "Can only call functions and classes.");
        } else {
            if(arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren(), "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
            }
            return function.call(this, arguments);
        }
    }

    @Override
    public Object visitGetExpr(final Get expr) {
        final Object object = evaluate(expr.object());
        if(object instanceof final LoxInstance instance) {
            final Object result = instance.get(expr.name());
            if(result instanceof final LoxFunction getter && getter.isGetter()) {
                return getter.call(this, null);
            }
            return result;
        }

        throw new RuntimeError(expr.name(), "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(final Expr.Set expr) {
        final Object object = evaluate(expr.object());

        if(object instanceof final LoxInstance instance) {
            final Object value = evaluate(expr.value());
            instance.set(expr.name(), value);
            return value;
        }

        throw new RuntimeError(expr.name(), "Only instances have fields.");
    }

    @Override
    public Object visitSuperExpr(final Super expr) {
        final int distance = locals.get(expr);
        final LoxClass superclass = (LoxClass) environment.getAt(distance, slots.get(expr));
        final LoxInstance object = (LoxInstance) environment.getAt(distance - 1, 0);

        final LoxFunction method = superclass.findMethod(expr.method().lexeme());
        if(method == null) {
            throw new RuntimeError(expr.method(), "Undefined property '" + expr.method().lexeme() + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(final This expr) {
        return lookupVariable(expr.keyword(), expr);
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
    public Object visitLogicalExpr(final Logical expr) {
        final Object left = evaluate(expr.left());

        if(expr.operator().type() == TokenType.OR) {
            if(isTruthy(left)) return left;
        } else {
            if(!isTruthy(left)) return left;
        }

        return evaluate(expr.right());
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
        return lookupVariable(expr.name(), expr);
    }

    private Object lookupVariable(final Token name, final Expr expr) {
        final Object value;

        final Integer distance = locals.get(expr);
        if(distance != null) {
            value = environment.getAt(distance, slots.get(expr));
        } else {
            if(globals.containsKey(name.lexeme())) {
                value = globals.get(name.lexeme());
            } else {
                throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
            }
        }

        if(value == uninitialized) throw new RuntimeError(name, "Variable must be initialized before use.");
        return value;
    }

    @Override
    public Object visitFunctionExpr(final Expr.Function expr) {
        return new LoxFunction(null, expr, environment, false);
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

    String interpret(final Expr expression) {
        try {
            final Object value = evaluate(expression);
            return stringify(value);
        } catch (final RuntimeError error) {
            Lox.runtimeError(error);
            return null;
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

    void resolve(final Expr expr, final int depth, final int slot) {
        locals.put(expr, depth);
        slots.put(expr, slot);
    }

    public void executeBlock(final List<Stmt> statements, final Environment environment) {
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
    public Void visitClassStmt(final Stmt.Class stmt) {
        Object superclass  = null;
        if(stmt.superclass() != null) {
            superclass = evaluate(stmt.superclass());
            if(!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass().name(), "Superclass must be a class.");
            }
        }

        final int slot = define(stmt.name(), null);

        if(superclass != null) {
            environment = new Environment(environment);
            environment.define(superclass);
        }

        final Map<String, LoxFunction> classMethods = new HashMap<>();
        for(final Stmt.Function classMethod : stmt.classMethods()) {
            final String name = classMethod.name().lexeme();
            final LoxFunction function = new LoxFunction(name, classMethod.function(), environment, false);
            classMethods.put(name, function);
        }

        final Map<String, LoxFunction> methods = new HashMap<>();
        for(final Stmt.Function method : stmt.methods()) {
            final String name = method.name().lexeme();
            final LoxFunction function = new LoxFunction(name, method.function(), environment, name.equals("init"));
            methods.put(name, function);
        }

        final LoxClass metaclass = new LoxClass(null, null, stmt.name().lexeme() + " metaclass", classMethods);

        final String name = stmt.name().lexeme();
        final LoxClass klass = new LoxClass(metaclass, (LoxClass) superclass, name, methods);

        if(superclass != null) environment = environment.enclosing;

        if(environment != null) {
            environment.assignAt(0, slot, klass);
        } else {
            if(globals.containsKey(name)) {
                globals.put(name, klass);
            } else {
                throw new RuntimeError(stmt.name(), "Undefined class name '" + name + "'.");
            }
        }

        return null;
    }

    @Override
    public Void visitExpressionStmt(final Expression stmt) {
        evaluate(stmt.expression());

        return null;
    }

    @Override
    public Void visitFunctionStmt(final Stmt.Function stmt) {
        final String name = stmt.name().lexeme();
        final LoxFunction function = new LoxFunction(name, stmt.function(), environment, false);
        define(stmt.name(), function);

        return null;
    }

    @Override
    public Void visitIfStmt(final If stmt) {
        if(isTruthy(evaluate(stmt.condition()))) {
            execute(stmt.thenBranch());
        } else if(stmt.elseBranch() != null) {
            execute(stmt.elseBranch());
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(final Print stmt) {
        final Object value = evaluate(stmt.expression());
        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void visitReturnStmt(final Return stmt) {
        final Object value = stmt.value() != null ? evaluate(stmt.value()) : null;

        throw new ReturnException(value);
    }

    @Override
    public Void visitVarStmt(final Var stmt) {
        final Object value = stmt.initializer() != null ? evaluate(stmt.initializer()) : uninitialized;
        define(stmt.name(), value);

        return null;
    }

    @Override
    public Void visitWhileStmt(final While stmt) {
        try {
            while(isTruthy(evaluate(stmt.condition()))) {
                try {
                    execute(stmt.body());
                } catch (final ContinueException error) {
                    throw new UnsupportedOperationException("TODO: implement continue");
                }
            }
        } catch (final BreakException error) {
            // fallthrough
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(final Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitContinueStmt(final Continue stmt) {
        throw new ContinueException();
    }

    @Override
    public Object visitAssignExpr(final Assign expr) {
        final Object value = evaluate(expr.value());

        final Integer distance = locals.get(expr);
        if(distance != null) {
            environment.assignAt(distance, slots.get(expr), value);
        } else {
            if (globals.containsKey(expr.name().lexeme())) {
                globals.put(expr.name().lexeme(), value);
            } else {
                throw new RuntimeError(expr.name(),"Undefined variable '" + expr.name().lexeme() + "'.");
            }
        }

        return value;
    }

    private int define(final Token name, final Object value) {
        if(environment != null) {
            return environment.define(value);
        } else {
            globals.put(name.lexeme(), value);
            return globals.size() - 1;
        }
    }
}
