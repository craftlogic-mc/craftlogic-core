package ru.craftlogic.api.plants;

public interface PlantSoil {
    int getWater();
    int drainWater(int amount, boolean simulate);
    int gainWater(int amount, boolean simulate);
    int getNutrients();
    int drainNutrients(int amount, boolean simulate);
    int gainNutrients(int amount, boolean simulate);
    Plant getPlant();
    void setPlant(PlantType plant);
}
