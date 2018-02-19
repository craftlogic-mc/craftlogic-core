package ru.craftlogic.api.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import ru.craftlogic.CraftLogic;

import java.util.Objects;

public interface ChunkLocation {
    static ChunkLocation wrap(World world, int x, int z) {
        return new Impl(world, x, z);
    }

    static ChunkLocation wrap(int dimension, int x, int z) {
        return new Impl(dimension, x, z);
    }

    default World getWorld() {
        return CraftLogic.getOrLoadDimension(this.getDimension());
    }

    default String getWorldName() {
        World world = this.getWorld();
        if (world != null) {
            return world.provider.getDimensionType().getName();
        } else {
            return "DIM" + this.getDimension();
        }
    }

    default Chunk getChunk() {
        return getWorld().getChunkFromChunkCoords(getChunkX(), getChunkZ());
    }

    int getChunkX();
    int getChunkZ();
    int getDimension();

    default boolean isDimensionLoaded() {
        return DimensionManager.getWorld(this.getDimension()) != null;
    }

    class Impl implements ChunkLocation {
        private final int x, z;
        private final int dimension;

        public Impl(World world, int x, int z) {
            this(world.provider.getDimension(), x, z);
        }

        public Impl(int dimension, int x, int z) {
            this.dimension = dimension;
            this.x = x;
            this.z = z;
        }

        @Override
        public int getChunkX() {
            return this.x;
        }

        @Override
        public int getChunkZ() {
            return this.z;
        }

        @Override
        public int getDimension() {
            return this.dimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkLocation)) return false;
            ChunkLocation loc = (ChunkLocation) o;
            return getChunkX() == loc.getChunkX() &&
                   getChunkZ() == loc.getChunkZ() &&
                   getDimension() == loc.getDimension();
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z, dimension);
        }
    }
}