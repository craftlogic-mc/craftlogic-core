package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.LocationReadOnly;

import javax.annotation.Nullable;

public interface Colored {
    @Deprecated
    default int getBlockColor(IBlockState state, @Nullable IBlockAccess blockAccessor, @Nullable BlockPos pos, int tint) {
        Location location = blockAccessor != null && pos != null ? new LocationReadOnly(blockAccessor, pos, state) : null;
        return this.getBlockColor(location, state, tint);
    }
    default int getBlockColor(@Nullable Location location, IBlockState state, int tint) {
        return 0xFFFFFF;
    }
    default int getItemColor(ItemStack stack, int tint) {
        return 0xFFFFFF;
    }
}
