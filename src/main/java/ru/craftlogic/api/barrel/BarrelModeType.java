package ru.craftlogic.api.barrel;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.util.Registrable;

import java.util.function.Function;

public final class BarrelModeType extends Registrable<BarrelModeType> {
    public static final IForgeRegistry<BarrelModeType> REGISTRY = new RegistryBuilder<BarrelModeType>()
        .setType(BarrelModeType.class)
        .setName(new ResourceLocation(CraftAPI.MOD_ID, "barrel_modes"))
        .allowModification()
        .create();

    private Function<Barrel, BarrelMode> factory;

    public BarrelModeType(String name, Function<Barrel, BarrelMode> factory) {
        this.setRegistryName(name);
        this.factory = factory;
    }

    public BarrelModeType(ResourceLocation name, Function<Barrel, BarrelMode> factory) {
        this.setRegistryName(name);
        this.factory = factory;
    }

    public BarrelMode createMode(Barrel barrel, Object... input) {
        BarrelMode mode = this.factory.apply(barrel);
        mode.name = getRegistryName();
        if (input != null && input.length > 0) {
            mode.onCreated(input);
        }
        return mode;
    }
}