package ru.craftlogic.api.util;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.NBTTagCompound;
import ru.craftlogic.api.world.Location;

import static java.lang.Math.random;
import static net.minecraft.util.math.MathHelper.ceil;
import static net.minecraft.util.math.MathHelper.floor;

public class ExperienceBuffer implements NBTReadWrite {
    private final float capacity;
    private float stored;

    public ExperienceBuffer(float capacity) {
        this.capacity = capacity;
    }

    public float getStored() {
        return stored;
    }

    public float getCapacity() {
        return capacity;
    }

    public float drain(float amount, boolean simulate) {
        float drawn = Math.min(amount, this.stored);
        if (!simulate) {
            this.stored -= drawn;
        }
        return drawn;
    }

    public float accept(float amount, boolean simulate) {
        float accepted = Math.min(this.capacity - this.stored, amount);
        if (!simulate) {
            this.stored += accepted;
        }
        return accepted;
    }

    public float accept(Location location, float amount, boolean simulate) {
        float accepted = this.accept(amount, simulate);
        if (accepted < amount && !simulate) {
            float rest = amount - accepted;
            int xp = 0;
            if (rest != 0 && rest < 1) {
                int j = floor(rest);
                if (j < ceil(rest) && random() < (double) (rest - (float) j)) {
                    ++j;
                }

                xp = j;
            }

            while(xp > 0) {
                int j = EntityXPOrb.getXPSplit(xp);
                xp -= j;
                location.spawnEntity(new EntityXPOrb(location.getWorld(), location.getX(), location.getY(), location.getZ(), j));
            }
        }
        return accepted;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.stored = compound.getFloat("stored");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setFloat("stored", this.stored);
        return compound;
    }
}
