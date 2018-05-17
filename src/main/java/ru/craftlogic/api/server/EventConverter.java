package ru.craftlogic.api.server;

import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import ru.craftlogic.api.event.player.PlayerEvent;
import ru.craftlogic.api.event.player.PlayerJoinedMessageEvent;
import ru.craftlogic.api.event.player.PlayerLeftMessageEvent;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

public class EventConverter {
    private final Server server;

    public EventConverter(Server server) {
        this.server = server;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoined(PlayerLoggedInEvent event) {
        Player player = new Player(this.server, event.player.getGameProfile());
        this.server.onlinePlayers.add(player);
        this.server.eventManager.dispatchEvent(new PlayerEvent.Joined(player));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeft(PlayerLoggedOutEvent event) {
        Player player = this.server.getPlayer(event.player.getGameProfile());
        this.server.onlinePlayers.remove(player);
        this.server.eventManager.dispatchEvent(new PlayerEvent.Left(player));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoaded(WorldEvent.Load event) {
        net.minecraft.world.World world = event.getWorld();
        if (world instanceof WorldServer) {
            World w = new World(this.server, Dimension.fromVanilla(world.provider.getDimensionType()));
            this.server.loadedWorlds.add(w);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnloaded(WorldEvent.Unload event) {
        net.minecraft.world.World world = event.getWorld();
        if (world instanceof WorldServer) {
            World w = this.server.getWorld(Dimension.fromVanilla(world.provider.getDimensionType()));
            this.server.loadedWorlds.remove(w);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerJoinedMessage(PlayerJoinedMessageEvent event) {
        net.minecraft.world.World world = event.getPlayer().getEntityWorld();
        if (world.getGameRules().getBoolean("hidePlayerJoinMessages")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLeftMessage(PlayerLeftMessageEvent event) {
        net.minecraft.world.World world = event.getPlayer().getEntityWorld();
        if (world.getGameRules().getBoolean("hidePlayerLeaveMessages")) {
            event.setCanceled(true);
        }
    }
}

