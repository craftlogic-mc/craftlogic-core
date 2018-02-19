package ru.craftlogic.api.inventory;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class InventoryField {
    private final IntSupplier getter;
    private IntConsumer setter;

    public InventoryField(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public int get() {
        return this.getter.getAsInt();
    }

    public void set(int value) {
        if (this.setter != null) {
            this.setter.accept(value);
        }
    }
}
