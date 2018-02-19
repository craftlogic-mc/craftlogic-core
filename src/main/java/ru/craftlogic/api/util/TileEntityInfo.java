package ru.craftlogic.api.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class TileEntityInfo<T extends TileEntity> {
    public final Class<T> clazz;
    private final BiFunction<World, IBlockState, T> factory;
    private final IBlockState state;

    public TileEntityInfo(Class<T> clazz, IBlockState state, BiFunction<World, IBlockState, T> factory) {
        this.clazz = clazz;
        this.state = state;
        this.factory = factory;
    }

    public T create(World world) {
        return this.factory.apply(world, this.state);
    }

    public T create(World world, IBlockState state) {
        return this.factory.apply(world, state);
    }
}
