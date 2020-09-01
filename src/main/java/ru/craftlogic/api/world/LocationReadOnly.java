package ru.craftlogic.api.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class LocationReadOnly extends Location {
    private final IBlockAccess blockAccessor;
    @Nullable private final IBlockState fallbackState;

    public LocationReadOnly(IBlockAccess blockAccessor, BlockPos pos, @Nullable IBlockState fallbackState) {
        super(pos);
        this.blockAccessor = blockAccessor;
        this.fallbackState = fallbackState;
    }

    @Override
    public IBlockState getBlockState() {
        IBlockState state = this.blockAccessor.getBlockState(this.getPos());
        if (fallbackState != null && state.getBlock() != fallbackState.getBlock()) {
            return fallbackState;
        }
        return state;
    }

    @Override
    public int getDimensionId() {
        if (this.blockAccessor instanceof World) {
            return ((World) this.blockAccessor).provider.getDimension();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld() {
        if (blockAccessor instanceof WorldGetter) {
            return ((WorldGetter) blockAccessor).getWorld();
        } else {
            throw new UnsupportedOperationException("Cannot get world from " + blockAccessor.getClass());
        }
    }

    @Override
    public Biome getBiome() {
        return blockAccessor instanceof WorldGetter ? super.getBiome() : getBiomeClientSide();
    }

    @SideOnly(Side.CLIENT)
    private Biome getBiomeClientSide() {
        return blockAccessor.getBiome(getPos());
    }

    @Override
    public IBlockAccess getBlockAccessor() {
        return this.blockAccessor;
    }

    @Override
    public boolean setBlockState(IBlockState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Location add(double x, double y, double z) {
        return new LocationReadOnly(blockAccessor, getPos().add(x, y, z), null);
    }
}
