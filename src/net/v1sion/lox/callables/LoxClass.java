package net.v1sion.lox.callables;

import net.v1sion.lox.Interpreter;
import net.v1sion.lox.Stmt;

import java.util.List;
import java.util.Map;

/**
 * @author Emanuel Schäffer
 * at 17.03.26
 **/

public record LoxClass(String name, Map<String, LoxFunction> methods) implements LoxCallable {

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        final LoxFunction initializer = findMethod("init");
        if(initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        final LoxInstance instance = new LoxInstance(this);
        final LoxFunction initializer = findMethod("init");
        if(initializer != null) initializer.bind(instance).call(interpreter, arguments);

        return instance;
    }

    public LoxFunction findMethod(final String name) {
        if(methods.containsKey(name)) return methods.get(name);

        return null;
    }
}
