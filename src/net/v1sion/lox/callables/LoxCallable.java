package net.v1sion.lox.callables;

import net.v1sion.lox.Interpreter;

import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 06.03.26
 **/

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
