package net.v1sion.lox.callables;

import net.v1sion.lox.Lox;

/**
 * @author Emanuel Schäffer
 * at 17.03.26
 **/

public class LoxClass {
    public final String name;

    public LoxClass(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
