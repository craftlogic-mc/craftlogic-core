package ru.craftlogic.mixin.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.ChunkGetter;
import ru.craftlogic.api.world.WorldGetter;

@Mixin(World.class)
public abstract class MixinWorld implements ChunkGetter, WorldGetter {
    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);

    @Override
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ);
    }

    @Override
    public World getWorld() {
        return (World) (Object) this;
    }
}
