package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Rotatable {
    boolean onRotated(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack tool, EnumFacing side);
}
