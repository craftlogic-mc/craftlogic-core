package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockHangingDoor extends BlockDoor {
    public BlockHangingDoor(Material material) {
        super(material);
        if (material == Material.WOOD) {
            setSoundType(SoundType.WOOD);
        } else if (material == Material.IRON) {
            setSoundType(SoundType.METAL);
        } else {
            setSoundType(SoundType.STONE);
        }
        disableStats();
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return pos.getY() < world.getHeight() - 1;
    }

    protected int getCloseSound() {
        return this.material == Material.IRON ? 1011 : 1012;
    }

    protected int getOpenSound() {
        return this.material == Material.IRON ? 1005 : 1006;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        EnumFacing facing = state.getValue(FACING).rotateY();
        if (state.getValue(HALF) == EnumDoorHalf.UPPER) {
            BlockPos downPos = pos.down();
            IBlockState downState = world.getBlockState(downPos);
            if (downState.getBlock() != this) {
                world.setBlockToAir(pos);
                //world.playEvent(2001, pos, Block.getStateId(state));
            } else if (neighborBlock != this) {
                this.neighborChanged(downState, world, downPos, neighborBlock, neighborPos);
            }
        } else {
            boolean invalidState = false;
            BlockPos upPos = pos.up();
            IBlockState upState = world.getBlockState(upPos);
            if (upState.getBlock() != this) {
                world.setBlockToAir(pos);
                invalidState = true;
            }

            if (state.getActualState(world, pos).getValue(HINGE) == EnumHingePosition.LEFT) {
                facing = facing.getOpposite();
            }

            if (!world.isSideSolid(pos.offset(facing), facing.getOpposite())
                || !world.isSideSolid(upPos.offset(facing), facing.getOpposite())) {

                invalidState = true;
                world.playEvent(2001, pos, Block.getStateId(state));
                world.setBlockToAir(pos);
                if (upState.getBlock() == this && upState.getValue(HALF) == EnumDoorHalf.UPPER) {
                    world.playEvent(2001, upPos, Block.getStateId(upState));
                    world.setBlockToAir(upPos);
                }
            }
            if (invalidState) {
                if (!world.isRemote) {
                    this.dropBlockAsItem(world, pos, state, 0);
                }
            } else {
                boolean opening = world.isBlockPowered(pos) || world.isBlockPowered(upPos);

                if (neighborBlock != this && (opening || neighborBlock.getDefaultState().canProvidePower()) && opening != upState.getValue(POWERED)) {
                    world.setBlockState(upPos, upState.withProperty(POWERED, opening), 2);
                    if (opening != state.getValue(OPEN)) {
                        world.setBlockState(pos, state.withProperty(OPEN, opening), 2);
                        world.markBlockRangeForRenderUpdate(pos, pos);
                        world.playEvent(null, opening ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                    }
                }
            }
        }
    }
}
