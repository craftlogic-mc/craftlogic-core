package ru.craftlogic.api.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

public interface LightingEngine {
    void scheduleLightUpdate(EnumSkyBlock lightType, BlockPos pos);

    void processLightUpdates();

    void processLightUpdatesForType(EnumSkyBlock lightType);
}
