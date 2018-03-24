package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockStone.class)
public abstract class MixinBlockStone extends Block {
    public MixinBlockStone() {
        super(Material.ROCK);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            if (state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) {
                for (EnumFacing side : EnumFacing.values()) {
                    BlockPos offsetPos = pos.offset(side);
                    IBlockState s = world.getBlockState(offsetPos);
                    if (s.getBlock() == this && s.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE
                            && world.rand.nextBoolean() && !world.isSideSolid(offsetPos.down(), EnumFacing.UP)) {

                        world.setBlockState(offsetPos, Blocks.COBBLESTONE.getDefaultState());
                        this.onBlockDestroyedByPlayer(world, offsetPos, s);
                    }
                }
            }
        }
    }
}
