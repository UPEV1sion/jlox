package net.v1sion.lox.callables;

import net.v1sion.lox.Interpreter;

import java.util.List;
import java.util.Map;

/**
 * @author Emanuel Schäffer
 * at 17.03.26
 **/

public class LoxClass extends LoxInstance implements LoxCallable {
    private final String name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(final LoxClass metaclass, final String name, final Map<String, LoxFunction> methods) {
        super(metaclass);
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        final LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        final LoxInstance instance = new LoxInstance(this);
        final LoxFunction initializer = findMethod("init");
        if (initializer != null) initializer.bind(instance).call(interpreter, arguments);

        return instance;
    }

    public LoxFunction findMethod(final String name) {
        if (methods.containsKey(name)) return methods.get(name);

        return null;
    }

    public String name() {
        return name;
    }

    public Map<String, LoxFunction> methods() {
        return methods;
    }
}
