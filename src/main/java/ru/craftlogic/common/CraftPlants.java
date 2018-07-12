package ru.craftlogic.common;

import ru.craftlogic.api.plants.PlantType;
import ru.craftlogic.common.plant.PlantRedMushroom;

import static ru.craftlogic.CraftLogic.registerPlant;

public class CraftPlants {
    public static final PlantType RED_MUSHROOM = new PlantType("red_mushroom", PlantRedMushroom::new);

    static void init() {
        registerPlant(RED_MUSHROOM);
    }
}
