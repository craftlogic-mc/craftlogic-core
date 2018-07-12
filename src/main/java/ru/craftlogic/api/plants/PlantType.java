package ru.craftlogic.api.plants;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.util.Registrable;
import ru.craftlogic.api.world.Location;

import java.util.function.Function;

public final class PlantType extends Registrable<PlantType> {
    public static final IForgeRegistry<PlantType> REGISTRY = new RegistryBuilder<PlantType>()
        .setType(PlantType.class)
        .setName(new ResourceLocation(CraftLogic.MODID, "plants"))
        .allowModification()
        .create();

    private Function<Location, Plant> factory;

    public PlantType(String name, Function<Location, Plant> factory) {
        this.setRegistryName(name);
        this.factory = factory;
    }

    public PlantType(ResourceLocation name, Function<Location, Plant> factory) {
        this.setRegistryName(name);
        this.factory = factory;
    }

    public Plant createPlant(Location location, PlantSoil soil) {
        Plant plant = this.factory.apply(location);
        plant.name = getRegistryName();
        plant.location = location;
        plant.soil = soil;
        return plant;
    }
}