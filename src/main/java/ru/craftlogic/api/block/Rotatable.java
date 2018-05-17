package ru.craftlogic.api.block;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import ru.craftlogic.api.world.Location;

public interface Rotatable {
    default boolean onRotated(Location location, EntityPlayer player, ItemStack tool, RayTraceResult target) {
        IBlockState state = location.getBlockState();
        if (state.getProperties().containsKey(BlockHorizontal.FACING) && target.sideHit.getAxis().isHorizontal()) {
            EnumFacing facing = state.getValue(BlockHorizontal.FACING);
            if (facing != target.sideHit) {
                location.setBlockProperty(BlockHorizontal.FACING, target.sideHit);
                return true;
            }
        } else if (state.getProperties().containsKey(BlockDirectional.FACING)) {
            EnumFacing facing = state.getValue(BlockDirectional.FACING);
            if (facing != target.sideHit) {
                location.setBlockProperty(BlockDirectional.FACING, target.sideHit);
                return true;
            }
        }
        return false;
    }
}
