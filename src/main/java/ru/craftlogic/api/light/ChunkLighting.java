package ru.craftlogic.api.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

public interface ChunkLighting {
    int getCachedLightFor(EnumSkyBlock enumSkyBlock, BlockPos pos);
}
