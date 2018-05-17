package ru.craftlogic.api.world;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;

public interface WorldNameable extends IWorldNameable {
    @Override
    default String getName() {
        if (this instanceof Enum) {
            return ((Enum) this).name().toLowerCase();
        } else {
            return this.getClass().getSimpleName().toLowerCase();
        }
    }

    @Override
    default boolean hasCustomName() {
        return false;
    }

    @Override
    default ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }
}
