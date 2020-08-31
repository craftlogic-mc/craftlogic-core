package ru.craftlogic.api.world;

import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public interface ChunkGetter {
    @Nullable
    Chunk getChunkAt(int chunkX, int chunkZ);
}
