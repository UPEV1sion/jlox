package net.v1sion.lox;

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
}
