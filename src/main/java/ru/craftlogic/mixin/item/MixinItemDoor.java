package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemDoor.class)
public class MixinItemDoor extends Item {
    @Shadow @Final private Block block;

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            String id = this.getRegistryName().toString();
            if (id.startsWith("minecraft:") && id.endsWith("door")) {
                if (this == Items.OAK_DOOR) {
                    items.add(new ItemStack(Items.IRON_DOOR));
                    items.add(new ItemStack(Items.OAK_DOOR));
                    items.add(new ItemStack(Items.SPRUCE_DOOR));
                    items.add(new ItemStack(Items.BIRCH_DOOR));
                    items.add(new ItemStack(Items.JUNGLE_DOOR));
                    items.add(new ItemStack(Items.ACACIA_DOOR));
                    items.add(new ItemStack(Items.DARK_OAK_DOOR));
                }
            } else {
                items.add(new ItemStack(this));
            }
        }
    }

    /**
     * @author Radviger
     * @reason Extended doors' features
     */
    @Overwrite
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (side != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (!block.isReplaceable(world, pos)) {
                pos = pos.offset(side);
            }

            ItemStack heldItem = player.getHeldItem(hand);
            if (player.canPlayerEdit(pos, side, heldItem) && this.block.canPlaceBlockAt(world, pos)) {
                EnumFacing facing = EnumFacing.fromAngle((double)player.rotationYaw);
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
            return EnumActionResult.FAIL;
        }
    }
}
