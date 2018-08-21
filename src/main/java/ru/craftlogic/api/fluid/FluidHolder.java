package ru.craftlogic.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import ru.craftlogic.api.fluid.manager.FluidManager;

import javax.annotation.Nullable;

public interface FluidHolder extends IFluidHandler {
    IFluidTankProperties[] EMPTY_PROPERTIES = new IFluidTankProperties[0];

    FluidManager getFluidManager();

    @Override
    default IFluidTankProperties[] getTankProperties() {
        FluidManager fluidManager = getFluidManager();
        if (fluidManager != null) {
            return fluidManager.getProperties();
        } else {
            return EMPTY_PROPERTIES;
        }
    }

    @Override
    @Deprecated
    default int fill(FluidStack fluid, boolean doFill) {
        return this.fillFluid(fluid, !doFill);
    }

    default int fillFluid(FluidStack fluid, boolean simulate) {
        FluidManager fluidManager = getFluidManager();
        if (fluidManager != null) {
            return fluidManager.fill(fluid, simulate);
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    @Deprecated
    default FluidStack drain(FluidStack fluid, boolean doDrain) {
        return this.drainFluid(fluid, !doDrain);
    }

    @Nullable
    default FluidStack drainFluid(FluidStack fluid, boolean simulate) {
        FluidManager fluidManager = getFluidManager();
        if (fluidManager != null) {
            return fluidManager.drain(fluid, simulate);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    default FluidStack drain(int amount, boolean doDrain) {
        return this.drainFluid(amount, !doDrain);
    }

    @Nullable
    default FluidStack drainFluid(int amount, boolean simulate) {
        FluidManager fluidManager = getFluidManager();
        if (fluidManager != null) {
            return fluidManager.drain(amount, simulate);
        } else {
            return null;
        }
    }
}
