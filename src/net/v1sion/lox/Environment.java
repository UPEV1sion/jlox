package net.v1sion.lox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 03.03.26
 **/

public class Environment {
    final Environment enclosing;
    private final List<Object> values = new ArrayList<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(final Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(final Object value) {
        values.add(value); // TODO disallow shadowing
    }

    Environment ancestor(final int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    public Object getAt(final int distance, final int slot) {
        return ancestor(distance).values.get(slot);
    }

    public void assignAt(final int distance, final int slot, final Object value) {
        ancestor(distance).values.set(slot, value);
    }

    @Override
    public String toString() {
        String result = values.toString();
        if(enclosing != null) {
            result += enclosing.toString();
        }

        return result;
    }
}
