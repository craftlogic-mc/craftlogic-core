package ru.craftlogic.api.event.player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.Player;

@Cancelable
public class PlayerTimedTeleportEvent extends PlayerEvent {
    public final Player player;
    public final Location target;

    public PlayerTimedTeleportEvent(Player player, Location target) {
        super(player.getEntity());
        this.player = player;
        this.target = target;
    }
}
