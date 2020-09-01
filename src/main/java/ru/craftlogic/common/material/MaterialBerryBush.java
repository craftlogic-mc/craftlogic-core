package ru.craftlogic.common.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialBerryBush extends Material {
    public MaterialBerryBush() {
        super(MapColor.FOLIAGE);
        setBurning();
        setNoPushMobility();
        setRequiresTool();
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
