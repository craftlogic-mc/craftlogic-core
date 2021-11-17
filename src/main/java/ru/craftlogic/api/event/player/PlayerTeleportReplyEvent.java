package ru.craftlogic.api.event.player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Player;

@Cancelable
public class PlayerTeleportReplyEvent extends PlayerEvent {
    public final Player player;
    public final Player target;
    public final boolean targetAccepted;
    public final CommandContext context;

    public PlayerTeleportReplyEvent(Player player, Player target, boolean targetAccepted, CommandContext context) {
        super(player.getEntity());
        this.player = player;
        this.target = target;
        this.targetAccepted = targetAccepted;
        this.context = context;
    }
}
