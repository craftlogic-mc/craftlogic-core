package ru.craftlogic.api.util;

@FunctionalInterface
public interface CheckedFunction<A, R, E extends Throwable> {
    R apply(A a) throws E;
}
