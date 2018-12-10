package ru.craftlogic.api.integration;

public interface ModIntegration {
    String getModId();
    void preInit();
    void init();
    void postInit();
}
