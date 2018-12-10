package ru.craftlogic.util;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import ru.craftlogic.api.CraftItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidBottleWrapper implements IFluidHandlerItem, ICapabilityProvider {
    @Nonnull
    protected ItemStack container;
    protected final Fluid fluid;
    protected final int amount;

    public FluidBottleWrapper(@Nonnull ItemStack container, Fluid fluid, int amount) {
        this.container = container;
        this.fluid = fluid;
        this.amount = amount;
    }

    @Override
    @Nonnull
    public ItemStack getContainer() {
        return this.container;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.getFluid() == this.fluid;
    }

    @Nullable
    public FluidStack getFluid() {
        return new FluidStack(this.fluid, this.amount);
    }

    /** @deprecated */
    @Deprecated
    protected void setFluid(@Nullable Fluid fluid) {
        this.setFluid(new FluidStack(fluid, this.amount));
    }

    protected void setFluid(@Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            this.container = new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (fluidStack.tag == null || fluidStack.tag.hasNoTags()) {
                if (fluid == FluidRegistry.WATER) {
                    this.container = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
                }

                if (fluid.getName().equals("milk")) {
                    this.container = new ItemStack(CraftItems.MILK_BOTTLE);
                }
            }
        }

    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new FluidTankProperties[]{new FluidTankProperties(this.getFluid(), this.amount)};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (this.container.getCount() == 1 && resource != null && resource.amount >= this.amount && this.getFluid() == null && this.canFillFluidType(resource)) {
            if (doFill) {
                this.setFluid(resource);
            }

            return this.amount;
        } else {
            return 0;
        }
    }

    @Override
    @Nullable
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (this.container.getCount() == 1 && resource != null && resource.amount >= this.amount) {
            FluidStack fluidStack = this.getFluid();
            if (fluidStack != null && fluidStack.isFluidEqual(resource)) {
                if (doDrain) {
                    this.setFluid((FluidStack)null);
                }

                return fluidStack;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (this.container.getCount() == 1 && maxDrain >= this.amount) {
            FluidStack fluidStack = this.getFluid();
            if (fluidStack != null) {
                if (doDrain) {
                    this.setFluid((FluidStack)null);
                }

                return fluidStack;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this) : null;
    }
}
