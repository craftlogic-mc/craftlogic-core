package ru.craftlogic.api;

import net.minecraft.util.text.ITextComponent;

public interface Permissible {
    boolean hasPermissions(String... permissions);
    ITextComponent getDisplayName();
}
