package ru.craftlogic.common.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.plants.Plant;
import ru.craftlogic.api.plants.PlantSoil;
import ru.craftlogic.api.plants.PlantType;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.block.GrassProperties;

import java.util.Random;

public class TileEntityGrass extends TileEntityBase implements Updatable, PlantSoil {
    private Plant plant;
    private final int maxWater = 1000, maxNutrients = 1000;
    private int water, nutrients;

    public TileEntityGrass(World world, IBlockState state) {
        super(world, state);
    }

    public void setPlant(PlantType type) {
        this.plant = type.createPlant(getLocation(), this);
    }

    public Plant getPlant() {
        return plant;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("plant")) {
            NBTTagCompound plant = compound.getCompoundTag("plant");
            ResourceLocation name = new ResourceLocation(plant.getString("name"));
            PlantType type = PlantType.REGISTRY.getValue(name);
            if (type != null) {
                this.plant = type.createPlant(getLocation(), this);
                this.plant.deserializeNBT(plant.getCompoundTag("data"));
            } else {
                System.out.println("Unable to find plant: " + name);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        if (this.plant != null) {
            NBTTagCompound plant = new NBTTagCompound();
            String name = this.plant.getRegistryName().toString();
            System.out.println("Saving plant with name: " + name);
            plant.setString("name", name);
            plant.setTag("data", this.plant.serializeNBT());
            compound.setTag("plant", plant);
        }
        return compound;
    }

    @Override
    public void randomTick(Random random) {
        if (this.plant != null) {
            this.plant.randomTick(random);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        System.out.println("Grass tile invalidated on " + (world.isRemote ? "client" : "server"));
    }

    @Override
    public void update() {
        if (!this.world.isRemote) {
            this.ticksExisted++;
            Location location = getLocation();
            if (this.water < this.maxWater) {
                if (this.ticksExisted % 10 == 0) {
                    for (EnumFacing side : EnumFacing.values()) {
                        if (location.offset(side).getBlockMaterial() == Material.WATER) {
                            this.water = Math.min(this.water + 20, this.maxWater);
                        }
                    }
                }
            }
            if (this.plant != null) {
                if (this.plant instanceof Updatable) {
                    ((Updatable) this.plant).update();
                }
            } else {
                location.setBlockProperty(GrassProperties.HAS_PLANT, false);
            }
        }
    }

    @Override
    public int getWater() {
        return this.water;
    }

    @Override
    public int gainWater(int amount, boolean simulate) {
        amount = Math.min(this.maxWater - this.water, amount);
        if (!simulate) this.water += amount;
        return amount;
    }

    @Override
    public int drainWater(int amount, boolean simulate) {
        amount = Math.min(this.water, amount);
        if (!simulate) this.water -= amount;
        return amount;
    }

    @Override
    public int getNutrients() {
        return this.nutrients;
    }

    @Override
    public int gainNutrients(int amount, boolean simulate) {
        amount = Math.min(this.maxNutrients - this.nutrients, amount);
        if (!simulate) this.nutrients += amount;
        return amount;
    }

    @Override
    public int drainNutrients(int amount, boolean simulate) {
        amount = Math.min(this.nutrients, amount);
        if (!simulate) this.nutrients -= amount;
        return amount;
    }
}
