package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(BlockLilyPad.class)
public abstract class MixinBlockLilyPad extends BlockBush {
    /**
     * @author Radviger
     * @reason No collision box on waterlily
     */
    @Overwrite
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {}
}
