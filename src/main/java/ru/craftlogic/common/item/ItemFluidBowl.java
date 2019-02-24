package ru.craftlogic.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.util.FluidBowlWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemFluidBowl extends ItemBase {
    @Nonnull
    private final Fluid fluid;

    public ItemFluidBowl(@Nonnull Fluid fluid) {
        super(fluid.getName() + "_bowl", CreativeTabs.FOOD);
        this.fluid = fluid;
        this.setMaxStackSize(1);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack item) {
        return this.fluid == FluidRegistry.WATER || this.fluid.getName().equals("milk") ? EnumAction.DRINK : EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack item) {
        return 40;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack item, World world, EntityLivingBase user) {
        if (this.fluid.getName().equals("milk")) {
            user.clearActivePotions();
        }

        if (user instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)user;
            CriteriaTriggers.CONSUME_ITEM.trigger(player, item);
            player.addStat(StatList.getObjectUseStats(this));
        }

        if (user instanceof EntityPlayer && !((EntityPlayer)user).capabilities.isCreativeMode) {
            item.shrink(1);
        }

        return item.isEmpty() ? new ItemStack(Items.BOWL) : item;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack item, @Nullable NBTTagCompound compound) {
        return new FluidBowlWrapper(item, this.fluid, 250);
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerCustomMeshDefinition(this, stack ->
            new ModelResourceLocation(new ResourceLocation(CraftAPI.MOD_ID, "fluid_bowl"), "inventory")
        );
        modelManager.registerItemVariants(this, "fluid_bowl");
    }
}
