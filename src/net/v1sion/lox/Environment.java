package net.v1sion.lox;

import net.v1sion.lox.exceptions.RuntimeError;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emanuel Schäffer
 * at 03.03.26
 **/

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(final Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(final String name, final Object value) {
        values.put(name, value);
    }

    public Object get(final Token name) {
        if(values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    public void assign(final Token name, final Object value) {
        if(values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        if(enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }
}
