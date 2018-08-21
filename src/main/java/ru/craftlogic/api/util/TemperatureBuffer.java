package ru.craftlogic.api.util;

import net.minecraft.nbt.NBTTagCompound;

public class TemperatureBuffer implements NBTReadWrite {
    private final int capacity;
    private int stored;

    public TemperatureBuffer(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.stored = compound.getInteger("stored");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("stored", this.stored);
        return compound;
    }

    public int getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDemanded() {
        return getCapacity() - getStored();
    }

    public int accept(int amount, boolean simulate) {
        int accepted = Math.min(this.capacity - this.stored, amount);
        if (!simulate) {
            this.stored += accepted;
        }
        return accepted;
    }

    public int drain(int amount, boolean simulate) {
        int drained = Math.min(this.stored, amount);
        if (!simulate) {
            this.stored -= drained;
        }
        return drained;
    }
}
