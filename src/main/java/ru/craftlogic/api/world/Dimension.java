package ru.craftlogic.api.world;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.EnumHelper;

import java.util.HashMap;
import java.util.Map;

public enum Dimension {
    OVERWORLD(DimensionType.OVERWORLD),
    NETHER(DimensionType.NETHER),
    THE_END(DimensionType.THE_END);
    private static final Map<DimensionType, Dimension> VANILLA_TO_CL = new HashMap<>();

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
        if (VANILLA_TO_CL.containsKey(vanillaType)) {
            return VANILLA_TO_CL.get(vanillaType);
        }
        Dimension dimension = EnumHelper.addEnum(
            Dimension.class,
            vanillaType.getName().toUpperCase(),
            new Class[] { DimensionType.class },
            vanillaType
        );
        VANILLA_TO_CL.put(vanillaType, dimension);
        return dimension;
    }
}
