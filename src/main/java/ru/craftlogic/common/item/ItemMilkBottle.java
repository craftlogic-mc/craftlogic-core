package ru.craftlogic.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import ru.craftlogic.api.CraftFluids;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.util.FluidBottleWrapper;

import javax.annotation.Nullable;

public class ItemMilkBottle extends ItemBase {
    public ItemMilkBottle() {
        super("milk_bottle", CreativeTabs.FOOD);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase user) {
        if (!world.isRemote) {
            user.curePotionEffects(stack);
        }

        if (user instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)user;
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.addStat(StatList.getObjectUseStats(this));
        }

        if (user instanceof EntityPlayer && !((EntityPlayer)user).capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack item) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack item) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack item, @Nullable NBTTagCompound compound) {
        return new FluidBottleWrapper(item, CraftFluids.MILK, 250);
    }
}
