package net.v1sion.lox.callables;

import net.v1sion.lox.Interpreter;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 17.03.26
 **/

public record LoxClass(String name) implements LoxCallable {

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        return new LoxInstance(this);
    }
}
