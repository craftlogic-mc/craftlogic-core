package ru.craftlogic.api.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import ru.craftlogic.api.CraftWorlds;

import java.util.Objects;

public class ChunkLocation {
    private final int x, z;
    private int dimension;

    public ChunkLocation(ChunkLocation location) {
        this(location.getDimensionId(), location.getChunkX(), location.getChunkZ());
    }

    public ChunkLocation(World world, int x, int z) {
        this(world.provider.getDimension(), x, z);
    }

    public ChunkLocation(Dimension dimension, int x, int z) {
        this(dimension.getVanilla().getId(), x, z);
    }

    public ChunkLocation(int dimension, int x, int z) {
        this(x, z);
        this.dimension = dimension;
    }

    protected ChunkLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    protected IBlockAccess getBlockAccessor() {
        return this.getWorld();
    }

    public World getWorld() {
        return CraftWorlds.getOrLoadWorld(this.getDimensionId());
    }

    public String getWorldName() {
        try {
            return DimensionType.getById(this.getDimensionId()).getName();
        } catch (IllegalArgumentException e) {
            return "DIM" + this.getDimensionId();
        }
    }

    public Chunk getChunk() {
        return getWorld().getChunkFromChunkCoords(getChunkX(), getChunkZ());
    }

    public int getChunkX() {
        return this.x;
    }

    public int getChunkZ() {
        return this.z;
    }

    public Dimension getDimension() {
        return Dimension.fromVanilla(DimensionType.getById(this.dimension));
    }

    public int getDimensionId() {
        return this.dimension;
    }

    public boolean isDimensionLoaded() {
        return DimensionManager.getWorld(this.getDimensionId()) != null;
    }

    public boolean isWithinWorldBorder() {
        return getWorld().getWorldBorder().contains(new ChunkPos(this.x, this.z));
    }

    public ChunkLocation offset(EnumFacing side) {
        return this.offset(side, 1);
    }

    public ChunkLocation offset(EnumFacing side, int amount) {
        return new ChunkLocation(this.dimension,
            this.x + side.getFrontOffsetX() * amount,
            this.z + side.getFrontOffsetZ() * amount
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkLocation)) return false;
        ChunkLocation loc = (ChunkLocation) o;
        return getChunkX() == loc.getChunkX() &&
                getChunkZ() == loc.getChunkZ() &&
                getDimensionId() == loc.getDimensionId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, dimension);
    }
}