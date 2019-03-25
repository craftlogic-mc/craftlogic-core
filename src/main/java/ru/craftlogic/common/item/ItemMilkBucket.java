package ru.craftlogic.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import ru.craftlogic.api.CraftFluids;

import javax.annotation.Nullable;

public class ItemMilkBucket extends Item {
    public ItemMilkBucket() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public String getTranslationKey() {
        return "item.forge.bucketFilled.milk";
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) {
        return I18n.translateToLocal("item.forge.bucketFilled.milk");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        RayTraceResult target = this.rayTrace(world, player, false);
        ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, heldItem, target);
        if (ret != null) {
            return ret;
        } else if (target == null) {
            return new ActionResult<>(EnumActionResult.PASS, heldItem);
        } else if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, heldItem);
        } else {
            BlockPos targetPos = target.getBlockPos();
            if (!world.isBlockModifiable(player, targetPos)) {
                return new ActionResult<>(EnumActionResult.FAIL, heldItem);
            } else {
                boolean replaceable = world.getBlockState(targetPos).getBlock().isReplaceable(world, targetPos);
                if (!replaceable || target.sideHit != EnumFacing.UP) {
                    targetPos = targetPos.offset(target.sideHit);
                }
                if (!player.canPlayerEdit(targetPos, target.sideHit, heldItem)) {
                    return new ActionResult<>(EnumActionResult.FAIL, heldItem);
                } else if (this.tryPlaceContainedLiquid(player, world, targetPos)) {
                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, targetPos, heldItem);
                    }

                    player.addStat(StatList.getObjectUseStats(this));
                    return !player.capabilities.isCreativeMode ? new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, heldItem);
                } else {
                    return new ActionResult<>(EnumActionResult.FAIL, heldItem);
                }
            }
        }
    }

    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        boolean nonSolid = !material.isSolid();
        boolean replaceable = state.getBlock().isReplaceable(world, pos);
        if (!world.isAirBlock(pos) && !nonSolid && !replaceable) {
            return false;
        } else {
            if (!world.isRemote && (nonSolid || replaceable) && !material.isLiquid()) {
                world.destroyBlock(pos, true);
            }

            SoundEvent emptySound = SoundEvents.ITEM_BUCKET_EMPTY;
            world.playSound(player, pos, emptySound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, CraftFluids.MILK.getBlock().getDefaultState(), 11);

            return true;
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack item, @Nullable NBTTagCompound compound) {
        return new FluidBucketWrapper(item);
    }
}
