package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public interface Colored {
    default int getBlockColor(IBlockState state, @Nullable IBlockAccess blockAccessor, @Nullable BlockPos pos, int tint) {
        return 0xFFFFFF;
    }
    default int getItemColor(ItemStack stack, int tint) {
        return 0xFFFFFF;
    }
}
