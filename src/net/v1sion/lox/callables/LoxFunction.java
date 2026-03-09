package net.v1sion.lox.callables;

import net.v1sion.lox.Environment;
import net.v1sion.lox.Interpreter;
import net.v1sion.lox.Stmt;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 09.03.26
 **/

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(final Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        final Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(), arguments.get(i));
        }

        interpreter.executeBlock(declaration.body(), environment);
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name().lexeme() + ">";
    }
}
