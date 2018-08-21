package ru.craftlogic.api;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.plants.PlantType;
import ru.craftlogic.common.plant.PlantRedMushroom;

import javax.annotation.Nonnull;

public class CraftPlants {
    public static final PlantType RED_MUSHROOM = new PlantType("red_mushroom", PlantRedMushroom::new);

    static void init(Side side) {
        registerPlant(RED_MUSHROOM);
    }

    public static <P extends PlantType> P registerPlant(@Nonnull P plant) {
        GameRegistry.findRegistry(PlantType.class).register(plant);
        return plant;
    }
}
