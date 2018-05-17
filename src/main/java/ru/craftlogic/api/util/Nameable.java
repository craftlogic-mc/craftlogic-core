package ru.craftlogic.api.util;

import net.minecraft.util.IStringSerializable;

public interface Nameable extends IStringSerializable {
    @Override
    default String getName() {
        if (this instanceof Enum) {
            return ((Enum) this).name().toLowerCase();
        } else {
            return this.getClass().getTypeName().toLowerCase();
        }
    }
}
