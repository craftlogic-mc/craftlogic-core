package ru.craftlogic.api.barrel;

import net.minecraft.block.material.Material;
import ru.craftlogic.api.world.Locatable;

public interface Barrel extends Locatable {
    Material getMaterial();
    BarrelMode getMode();
    void clear();
    boolean isEmpty();
    boolean isClosed();
    void markForUpdate();
}
