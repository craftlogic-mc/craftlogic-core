package ru.craftlogic.api.entity;

import com.mojang.authlib.GameProfile;
import ru.craftlogic.api.world.Player;

public interface AdvancedPlayer {
    void setFirstPlayed(long firstPlayed);
    long getFirstPlayed();
    void setTimePlayed(long timePlayed);
    long getTimePlayed();
    Player wrapped();
    void initialize(GameProfile profile);
}
