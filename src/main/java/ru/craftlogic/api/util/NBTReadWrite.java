package ru.craftlogic.api.util;

import net.minecraft.nbt.NBTTagCompound;

public interface NBTReadWrite {
    default void readFromNBT(NBTTagCompound compound, String tag) {
        this.readFromNBT(compound.getCompoundTag(tag));
    }
    default NBTTagCompound writeToNBT(NBTTagCompound compound, String tag) {
        NBTTagCompound tmp = new NBTTagCompound();
        tmp = this.writeToNBT(tmp);
        compound.setTag(tag, tmp);
        return compound;
    }

    void readFromNBT(NBTTagCompound compound);
    NBTTagCompound writeToNBT(NBTTagCompound compound);
}
