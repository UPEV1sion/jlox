package net.v1sion.lox.callables;

import net.v1sion.lox.Token;

import java.util.Map;

/**
 * @author Emanuel Schäffer
 * at 22.04.26
 **/

public record LoxMixin(Token name, Map<String, LoxFunction> methods) {

}
