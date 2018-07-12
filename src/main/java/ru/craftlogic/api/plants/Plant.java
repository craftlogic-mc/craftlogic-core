package ru.craftlogic.api.plants;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class Plant implements INBTSerializable<NBTTagCompound>, Locatable {
    ResourceLocation name;
    Location location;
    PlantSoil soil;
    protected int ticksExisted;

    protected Plant() {}

    public abstract Block getPlantBlock();

    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public final Location getLocation() {
        return this.location;
    }

    public PlantSoil getSoil() {
        return soil;
    }

    @Override
    @Nonnull
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {}

    public void randomTick(Random random) {}
}
