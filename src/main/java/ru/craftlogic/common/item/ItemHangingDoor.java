package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemHangingDoor extends ItemDoor {
    private final Block block;

    public ItemHangingDoor(Block block) {
        super(block);
        this.block = block;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (side == EnumFacing.UP) {
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (!block.isReplaceable(world, pos)) {
                pos = pos.offset(side);
            }

            ItemStack heldItem = player.getHeldItem(hand);
            if (player.canPlayerEdit(pos, side, heldItem) && this.block.canPlaceBlockAt(world, pos)) {
                EnumFacing facing = EnumFacing.fromAngle((double) player.rotationYaw);
                int ox = facing.getXOffset();
                int oz = facing.getZOffset();
                boolean rightHinge = ox < 0 && hitZ < 0.5F || ox > 0 && hitZ > 0.5F || oz < 0 && hitX > 0.5F || oz > 0 && hitX < 0.5F;
                EnumFacing handleDir = rightHinge ? facing.rotateY() : facing.rotateYCCW();
                EnumFacing handleOppDir = handleDir.getOpposite();
                BlockPos posHandle = pos.offset(handleDir);
                IBlockState handleState = world.getBlockState(posHandle);
                BlockPos posHandleUp = posHandle.up();
                IBlockState handleUpState = world.getBlockState(posHandleUp);
                if (handleState.getBlockFaceShape(world, posHandle, handleOppDir) == BlockFaceShape.SOLID
                    && handleUpState.getBlockFaceShape(world, posHandleUp, handleOppDir) == BlockFaceShape.SOLID) {

                    ItemDoor.placeDoor(world, pos, facing, this.block, rightHinge);
                    IBlockState s = world.getBlockState(pos);
                    SoundType sound = s.getBlock().getSoundType(s, world, pos, player);
                    world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    heldItem.shrink(1);
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return EnumActionResult.FAIL;
    }
}
