package net.v1sion.lox.callables;

import net.v1sion.lox.Token;
import net.v1sion.lox.exceptions.RuntimeError;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emanuel Schäffer
 * at 19.03.26
 **/


public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(final LoxClass klass) {
        this.klass = klass;
    }

    public Object get(final Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        final LoxFunction method = klass.findMethod(name.lexeme());
        if(method != null) return method;

        throw new RuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
    }

    public void set(final Token name, final Object value) {
        fields.put(name.lexeme(), value);
    }

    @Override
    public String toString() {
        return klass.name() + " instance";
    }
}
