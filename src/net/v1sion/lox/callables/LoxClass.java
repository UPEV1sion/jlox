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
    private final LoxClass superclass;

    public LoxClass(final LoxClass metaclass,
                    final LoxClass superclass,
                    final String name,
                    final Map<String, LoxFunction> methods) {
        super(metaclass);
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
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
        if (initializer != null) initializer.call(interpreter, arguments);

        return instance;
    }

    public LoxFunction findMethod(final String name) {
        LoxFunction method = null;
        LoxFunction inner = null;
        LoxClass klass = this;
        while(klass != null) {
            if(klass.methods.containsKey(name)) {
                inner = method;
                method = klass.methods.get(name);
            }

            klass = klass.superclass;
        }

        if(method != null) {
            return method.bind(new LoxInstance(this), inner);
        }

        return null;
    }

    public String name() {
        return name;
    }

    public Map<String, LoxFunction> methods() {
        return methods;
    }
}
