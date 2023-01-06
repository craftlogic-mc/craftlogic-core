package ru.craftlogic.api.event.player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.craftlogic.api.world.Player;

public class PlayerMoveEvent extends PlayerEvent {
    public final Player player;

    public PlayerMoveEvent(Player player) {
        super(player.getEntity());
        this.player = player;
    }
}
