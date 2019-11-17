package ru.craftlogic.api.block;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IShearable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.LocationReadOnly;

import javax.annotation.Nonnull;
import java.util.List;

public interface Shearable extends IShearable {
    @Override
    @Deprecated
    default boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
        return isShearable(new LocationReadOnly(world, pos, world.getBlockState(pos)), item);
    }

    @Nonnull
    @Override
    @Deprecated
    default List<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return onSheared(new LocationReadOnly(world, pos, world.getBlockState(pos)), fortune, item);
    }

    boolean isShearable(Location location, @Nonnull ItemStack tool);
    List<ItemStack> onSheared(Location location, int fortune, @Nonnull ItemStack tool);
}
