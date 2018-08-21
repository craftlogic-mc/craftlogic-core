package ru.craftlogic.api.world;

import net.minecraft.util.text.ITextComponent;

public interface Permissible {
    boolean hasPermissions(String... permissions);
    ITextComponent getDisplayName();
}
