package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEndPortal.class)
public class MixinBlockEndPortal {

    /**
     * @author Pudo
     * @reason Fixed dupe with blocks etc.
     */

    @Overwrite
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss() && entity.getEntityBoundingBox().intersects(state.getBoundingBox(world, pos).offset(pos))) {
            if (entity instanceof EntityPlayer) {
                entity.changeDimension(1);
            }
        }

    }
}
