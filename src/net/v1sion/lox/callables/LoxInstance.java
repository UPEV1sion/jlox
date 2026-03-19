package net.v1sion.lox.callables;

public record LoxInstance(LoxClass klass) {

    @Override
    public String toString() {
        return klass.name() + " instance";
    }
}
