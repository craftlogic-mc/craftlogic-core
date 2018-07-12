package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.block.BlockDoor.*;

@Mixin(BlockDoor.class)
public class MixinBlockDoor extends Block {
    protected MixinBlockDoor(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     */
    @Overwrite
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return pos.getY() < world.getHeight() - 1;
    }

    /**
     * @author Radviger
     */
    @Overwrite
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        EnumFacing facing = state.getValue(FACING).rotateY();
        if (state.getValue(HALF) == EnumDoorHalf.UPPER) {
            BlockPos downPos = pos.down();
            IBlockState downState = world.getBlockState(downPos);
            if (downState.getBlock() != this) {
                world.setBlockToAir(pos);
                world.playEvent(2001, pos, Block.getStateId(state));
            } else if (neighborBlock != this) {
                this.neighborChanged(downState, world, downPos, neighborBlock, neighborPos);
            }
        } else {
            if (state.getActualState(world, pos).getValue(HINGE) == EnumHingePosition.LEFT) {
                facing = facing.getOpposite();
            }
            boolean invalidState = false;
            BlockPos upPos = pos.up();
            IBlockState upState = world.getBlockState(upPos);
            if (upState.getBlock() != this) {
                world.setBlockToAir(pos);
                invalidState = true;
            }
            if (!world.isSideSolid(pos.offset(facing), facing.getOpposite())
                    || !world.isSideSolid(pos.up().offset(facing), facing.getOpposite())) {

                invalidState = true;
                world.setBlockToAir(pos);
                if (upState.getBlock() == this && upState.getValue(HALF) == EnumDoorHalf.UPPER) {
                    world.setBlockToAir(upPos);
                    world.playEvent(2001, pos, Block.getStateId(upState));
                }
                world.playEvent(2001, pos, Block.getStateId(state));
            }
            if (invalidState) {
                if (!world.isRemote) {
                    this.dropBlockAsItem(world, pos, state, 0);
                }
            } else {
                boolean opening = world.isBlockPowered(pos) || world.isBlockPowered(upPos);

                if ((opening || world.getBlockState(neighborPos).canProvidePower())
                        && world.getBlockState(neighborPos).getBlock() != this && opening != upState.getValue(POWERED)) {

                    world.setBlockState(upPos, upState.withProperty(POWERED, opening), 2);
                    if (opening != state.getValue(OPEN)) {
                        world.setBlockState(pos, state.withProperty(OPEN, opening), 2);
                        world.markBlockRangeForRenderUpdate(pos, pos);
                        world.playEvent(opening ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                    }
                }
            }
        }
    }

    @Shadow
    private int getCloseSound() {
        return 0;
    }

    @Shadow
    private int getOpenSound() {
        return 0;
    }
}
