package ru.craftlogic.api.fluid.manager;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public interface FluidManager {
    int fill(FluidStack fluid, boolean simulate);

    @Nullable
    FluidStack drain(FluidStack fluid, boolean simulate);

    @Nullable
    FluidStack drain(int amount, boolean simulate);

    IFluidTankProperties[] getProperties();
}
