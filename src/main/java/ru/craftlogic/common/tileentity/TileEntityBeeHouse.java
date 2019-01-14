package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.tile.TileEntityBase;

public class TileEntityBeeHouse extends TileEntityBase {
    public TileEntityBeeHouse(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        return super.onActivated(player, hand, target);
    }
}
