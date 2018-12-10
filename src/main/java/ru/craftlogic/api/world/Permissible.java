package ru.craftlogic.api.world;

import net.minecraft.util.text.ITextComponent;

import java.util.function.Function;

public interface Permissible {
    ITextComponent getDisplayName();
    default boolean hasPermission(String permission) {
        return hasPermission(permission, 0);
    }
    boolean hasPermission(String permission, int opLevel);
    default <T> T getPermissionMetadata(String meta, T def, Function<String, T> mapper) {
        return def;
    }
    default String getPermissionMetadata(String meta, String def) {
        return getPermissionMetadata(meta, def, Function.identity());
    }
}
