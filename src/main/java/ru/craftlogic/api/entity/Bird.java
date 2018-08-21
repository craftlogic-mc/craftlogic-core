package ru.craftlogic.api.entity;

public interface Bird extends Animal {
    void setEggLayingDelay(int delay);
    int getEggLayingDelay();
    int getPossibleEggsCount();
    void setPossibleEggsCount(int possibleEggs);
    boolean isRooster();
}
