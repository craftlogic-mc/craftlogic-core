package ru.craftlogic.api.event.player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

@Cancelable
public class PlayerTeleportHomeEvent extends PlayerEvent {
    public final Player player;
    public final OfflinePlayer target;
    public final Location bedLocation;
    public final CommandContext context;
    public final boolean offline;

    public PlayerTeleportHomeEvent(Player player, OfflinePlayer target, Location bedLocation, CommandContext context, boolean offline) {
        super(player.getEntity());
        this.player = player;
        this.target = target;
        this.bedLocation = bedLocation;
        this.context = context;
        this.offline = offline;
    }
}
