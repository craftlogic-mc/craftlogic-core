package ru.craftlogic.api.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<A, E extends Throwable> {
    void accept(A a) throws E;

    default Consumer<A> unwrap() {
        return a -> {
            try {
                this.accept(a);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
