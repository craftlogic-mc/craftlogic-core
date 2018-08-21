package ru.craftlogic.api.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface FloatSupplier extends Supplier<Float> {
    float getAsFloat();

    @Override
    default Float get() {
        return this.getAsFloat();
    }
}
