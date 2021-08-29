package ru.craftlogic.api.event.player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Player;

@Cancelable
public class PlayerTeleportRequestEvent extends PlayerEvent {
    public final Player player;
    public final Player target;
    public final CommandContext context;

    public PlayerTeleportRequestEvent(Player player, Player target, CommandContext context) {
        super(player.getEntity());
        this.player = player;
        this.target = target;
        this.context = context;
    }
}
