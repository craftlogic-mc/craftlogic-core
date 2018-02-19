package ru.craftlogic.api.block.holders;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public interface FluidHolder extends IFluidHandler {
    @Override
    @Deprecated
    default int fill(FluidStack fluid, boolean doFill) {
        return this.fillFluid(fluid, !doFill);
    }

    int fillFluid(FluidStack fluid, boolean simulate);

    @Nullable
    @Override
    @Deprecated
    default FluidStack drain(FluidStack fluid, boolean doDrain) {
        return this.drainFluid(fluid, !doDrain);
    }

    @Nullable
    FluidStack drainFluid(FluidStack fluid, boolean simulate);

    @Nullable
    @Override
    default FluidStack drain(int amount, boolean doDrain) {
        return this.drainFluid(amount, !doDrain);
    }

    @Nullable
    FluidStack drainFluid(int amount, boolean simulate);
}
