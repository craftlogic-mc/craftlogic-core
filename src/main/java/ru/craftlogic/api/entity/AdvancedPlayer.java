package ru.craftlogic.api.entity;

import ru.craftlogic.api.world.Player;

public interface AdvancedPlayer {
    void setFirstPlayed(long firstPlayed);
    long getFirstPlayed();
    Player wrapped();
}
