package ru.craftlogic.api.entity;

import net.minecraft.util.IStringSerializable;

public interface Bird<V extends IStringSerializable> {
    void setEggLayingDelay(int delay);
    int getEggLayingDelay();
    int getPossibleEggsCount();
    void setPossibleEggsCount(int possibleEggs);
    boolean isRooster();
    V getVariant();
}
