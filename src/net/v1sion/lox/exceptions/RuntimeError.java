package net.v1sion.lox.exceptions;

import net.v1sion.lox.Token;

/**
 * @author Emanuel Schäffer
 * at 01.03.26
 **/

public class RuntimeError extends RuntimeException {
    final Token token;

    public RuntimeError(final Token token, final String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
