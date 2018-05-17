package ru.craftlogic.util;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Reflection {
    public static <T> void setFinalField(Class<T> type, T instance, Object value, String... fieldNames) {
        try {
            Field field = ReflectionHelper.findField(type, fieldNames);
            int modifiers = field.getModifiers();
            Field fm = Field.class.getDeclaredField("modifiers");
            fm.setAccessible(true);
            fm.set(field, modifiers & ~Modifier.FINAL);
            field.set(instance, value);
            fm.set(field, modifiers);
            fm.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException | UnableToFindFieldException ignored) {}
    }
}
