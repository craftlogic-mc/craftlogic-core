package ru.craftlogic.api.block;

public interface HeatConductor {
    int getTemperature();
    void setTemperature(int temperature);
    int getHotTemperature();
    int getMaxTemperature();
}
