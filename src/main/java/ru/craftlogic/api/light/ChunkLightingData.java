package ru.craftlogic.api.light;

public interface ChunkLightingData {
    short[] getNeighborLightChecks();

    void setNeighborLightChecks(short[] data);

    boolean isLightInitialized();

    void setLightInitialized(boolean val);

    void setSkylightUpdatedPublic();
}
