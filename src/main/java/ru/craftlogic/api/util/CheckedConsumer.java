package ru.craftlogic.api.util;

@FunctionalInterface
public interface CheckedConsumer<A, E extends Throwable> {
    void accept(A a) throws E;
}
