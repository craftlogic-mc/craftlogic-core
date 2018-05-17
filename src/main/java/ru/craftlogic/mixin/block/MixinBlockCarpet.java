package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockCarpet.class)
public abstract class MixinBlockCarpet extends Block {
    public MixinBlockCarpet() {
        super(Material.CLOTH);
    }

    @Overwrite
    private boolean canBlockStay(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos.down());
        return state.isSideSolid(world, pos.down(), EnumFacing.UP)
                || state.getBlock() == Blocks.GLASS || state.getBlock() == Blocks.STAINED_GLASS;
    }
}
