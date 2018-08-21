package ru.craftlogic.api.fluid.manager;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class WrappedFluidManager implements FluidManager {
    private IFluidHandler fluidHandler;

    public WrappedFluidManager(IFluidHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public IFluidTankProperties[] getProperties() {
        return this.fluidHandler.getTankProperties();
    }

    @Override
    public int fill(FluidStack fluid, boolean simulate) {
        return this.fluidHandler.fill(fluid, !simulate);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluid, boolean simulate) {
        return this.fluidHandler.drain(fluid, !simulate);
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean simulate) {
        return this.fluidHandler.drain(amount, !simulate);
    }
}
