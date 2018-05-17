package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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

    @Override
    public void onNeighborChange(IBlockAccess blockAccessor, BlockPos pos, BlockPos neighborPos) {
        if (blockAccessor instanceof WorldServer && ((WorldServer)blockAccessor).rand.nextInt(5) == 0) {
            World world = (World)blockAccessor;
            IBlockState state = world.getBlockState(pos);
            if (state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) {
                for (EnumFacing side : EnumFacing.values()) {
                    BlockPos offsetPos = pos.offset(side);
                    IBlockState s = world.getBlockState(offsetPos);
                    if (s.getBlock() == this && s.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE
                            && world.rand.nextBoolean() && !world.isSideSolid(offsetPos.down(), EnumFacing.UP)) {

                        world.setBlockState(offsetPos, Blocks.COBBLESTONE.getDefaultState());
                    }
                }
            }
        }
    }
}
