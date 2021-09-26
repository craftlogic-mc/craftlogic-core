package ru.craftlogic.mixin.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucketMilk;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftFluids;

import javax.annotation.Nullable;

@Mixin(ItemBucketMilk.class)
public abstract class MixinItemBucketMilk extends Item {
    @Inject(method = "getItemUseAction", at = @At("HEAD"), cancellable = true)
    public void onGetUseAction(ItemStack item, CallbackInfoReturnable<EnumAction> ci) {
        if (!CraftConfig.tweaks.enableMilkBucketTweaks) {
            ci.setReturnValue(EnumAction.NONE);
        }
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
    public ICapabilityProvider initCapabilities(ItemStack item, @Nullable NBTTagCompound compound) {
        if (CraftConfig.tweaks.enableMilkBucketTweaks) {
            return new FluidBucketWrapper(item);
        } else {
            return null;
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

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    public void onRightClick(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> ci) {
        if (CraftConfig.tweaks.enableMilkBucketTweaks) {
            ItemStack heldItem = player.getHeldItem(hand);
            RayTraceResult target = this.rayTrace(world, player, false);
            ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, heldItem, target);
            if (ret != null) {
                ci.setReturnValue(ret);
            } else if (target == null) {
                ci.setReturnValue(new ActionResult<>(EnumActionResult.PASS, heldItem));
            } else if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
                ci.setReturnValue(new ActionResult<>(EnumActionResult.PASS, heldItem));
            } else {
                BlockPos targetPos = target.getBlockPos();
                if (!world.isBlockModifiable(player, targetPos)) {
                    ci.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, heldItem));
                } else {
                    boolean replaceable = world.getBlockState(targetPos).getBlock().isReplaceable(world, targetPos);
                    if (!replaceable || target.sideHit != EnumFacing.UP) {
                        targetPos = targetPos.offset(target.sideHit);
                    }
                    if (!player.canPlayerEdit(targetPos, target.sideHit, heldItem)) {
                        ci.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, heldItem));
                    } else if (this.tryPlaceContainedLiquid(player, world, targetPos)) {
                        if (player instanceof EntityPlayerMP) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, targetPos, heldItem);
                        }

                        player.addStat(StatList.getObjectUseStats(this));
                        ci.setReturnValue(!player.capabilities.isCreativeMode ? new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult<>(EnumActionResult.SUCCESS, heldItem));
                    } else {
                        ci.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, heldItem));
                    }
                }
            }
        }
    }
}
