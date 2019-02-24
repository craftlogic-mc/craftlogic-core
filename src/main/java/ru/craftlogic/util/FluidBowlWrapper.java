package ru.craftlogic.util;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import ru.craftlogic.api.CraftItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidBowlWrapper extends FluidHandlerItemStack {
    protected final Fluid fluid;
    protected final int amount;

    public FluidBowlWrapper(@Nonnull ItemStack container, Fluid fluid, int amount) {
        super(container, amount);
        this.fluid = fluid;
        this.amount = amount;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.getFluid() == this.fluid;
    }

    @Nullable
    public FluidStack getFluid() {
        return new FluidStack(this.fluid, this.amount);
    }

    @Override
    protected void setFluid(@Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            this.container = new ItemStack(Items.BOWL);
        } else {
            if (fluidStack.tag == null || fluidStack.tag.hasNoTags()) {
                if (fluid == FluidRegistry.WATER) {
                    this.container = new ItemStack(CraftItems.WATER_BOWL);
                }

                if (fluid.getName().equals("milk")) {
                    this.container = new ItemStack(CraftItems.MILK_BOWL);
                }
            }
        }
    }

    @Override
    protected void setContainerToEmpty() {
        this.container = new ItemStack(Items.BOWL);
    }
}
