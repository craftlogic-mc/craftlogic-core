package ru.craftlogic.util;

import java.lang.reflect.Field;

public class FieldAccessor<O, V> {
    private final Field field;

    public FieldAccessor(Class<O> owner, String name, boolean declared, boolean unlock) {
        try {
            this.field = declared ? owner.getDeclaredField(name) : owner.getField(name);
            if (unlock) {
                this.field.setAccessible(true);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public V get(O instance) {
        try {
            return (V)this.field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
