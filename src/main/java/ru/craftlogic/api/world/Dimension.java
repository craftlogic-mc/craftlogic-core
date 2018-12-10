package ru.craftlogic.api.world;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.EnumHelper;

public enum Dimension {
    OVERWORLD(DimensionType.OVERWORLD),
    NETHER(DimensionType.NETHER),
    THE_END(DimensionType.THE_END);

    private final DimensionType vanillaType;

    Dimension(DimensionType vanillaType) {
        this.vanillaType = vanillaType;
    }

    public DimensionType getVanilla() {
        return this.vanillaType;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public static Dimension fromVanilla(DimensionType vanillaType) {
        for (Dimension dimension : values()) {
            if (dimension.vanillaType == vanillaType) {
                return dimension;
            }
        }
        return EnumHelper.addEnum(
            Dimension.class,
            vanillaType.getName().toUpperCase(),
            new Class[] { DimensionType.class },
            vanillaType
        );
    }
}
