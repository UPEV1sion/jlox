package net.v1sion.lox.exceptions;

/**
 * @author Emanuel Schäffer
 * at 09.03.26
 **/

public class ReturnException extends RuntimeException {
    public final Object value;

    public ReturnException(final Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
