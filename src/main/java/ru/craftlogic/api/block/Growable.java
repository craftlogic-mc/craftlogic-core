package ru.craftlogic.api.block;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.craftlogic.api.world.Location;

import java.util.Random;

public interface Growable extends IGrowable {
    @Override
    @Deprecated
    default boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isWorldRemote) {
        return this.canGrow(new Location(world, pos), isWorldRemote);
    }

    default boolean canGrow(Location location, boolean isWorldRemote) {
        return true;
    }

    @Override
    @Deprecated
    default boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return this.canUseBonemeal(new Location(world, pos), random);
    }

    default boolean canUseBonemeal(Location location, Random rand) {
        return true;
    }

    @Override
    @Deprecated
    default void grow(World world, Random random, BlockPos pos, IBlockState state) {
        this.grow(new Location(world, pos), random);
    }

    void grow(Location location, Random rand);
}
