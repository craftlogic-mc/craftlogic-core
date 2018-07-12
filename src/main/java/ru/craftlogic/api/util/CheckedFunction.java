package ru.craftlogic.api.util;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<A, R, E extends Throwable> {
    R apply(A a) throws E;

    default Function<A, R> unwrap() {
        return a -> {
            try {
                return this.apply(a);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
