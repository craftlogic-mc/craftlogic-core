package ru.craftlogic.api.fluid;

import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.awt.*;

public class FluidBase extends Fluid implements IForgeRegistryEntry<Fluid> {
    private ResourceLocation registryName;

    public FluidBase(ResourceLocation name, ResourceLocation still, ResourceLocation flowing, int color) {
        super(name.getResourcePath(), still, flowing, color);
        this.registryName = name;
    }

    @Override
    public Fluid setRegistryName(ResourceLocation name) {
        this.registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public Class<Fluid> getRegistryType() {
        return Fluid.class;
    }

    @Override
    public FluidBase setLuminosity(int luminosity) {
        return (FluidBase) super.setLuminosity(luminosity);
    }

    @Override
    public FluidBase setDensity(int density) {
        return (FluidBase) super.setDensity(density);
    }

    @Override
    public FluidBase setTemperature(int temperature) {
        return (FluidBase) super.setTemperature(temperature);
    }

    @Override
    public FluidBase setViscosity(int viscosity) {
        return (FluidBase) super.setViscosity(viscosity);
    }

    @Override
    public FluidBase setGaseous(boolean gaseous) {
        return (FluidBase) super.setGaseous(gaseous);
    }

    @Override
    public FluidBase setRarity(EnumRarity rarity) {
        return (FluidBase) super.setRarity(rarity);
    }

    @Override
    public FluidBase setFillSound(SoundEvent fillSound) {
        return (FluidBase) super.setFillSound(fillSound);
    }

    @Override
    public FluidBase setEmptySound(SoundEvent emptySound) {
        return (FluidBase) super.setEmptySound(emptySound);
    }

    @Override
    public FluidBase setColor(Color color) {
        return (FluidBase) super.setColor(color);
    }

    @Override
    public FluidBase setColor(int color) {
        return (FluidBase) super.setColor(color);
    }
}
