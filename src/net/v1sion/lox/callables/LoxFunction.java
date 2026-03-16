package net.v1sion.lox.callables;

import net.v1sion.lox.Environment;
import net.v1sion.lox.Expr;
import net.v1sion.lox.Interpreter;
import net.v1sion.lox.exceptions.ReturnException;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 09.03.26
 **/

public class LoxFunction implements LoxCallable {
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;

    public LoxFunction(
            final String name,
            final Expr.Function declaration,
            final Environment closure) {
        this.name = name;
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        final Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body(), environment);
        }catch (final ReturnException returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return name != null ? "<fn " + name + ">" : "<lambda>";
    }
}
