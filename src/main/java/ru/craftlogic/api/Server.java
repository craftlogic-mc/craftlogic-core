package ru.craftlogic.api;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import jline.internal.Nullable;
import net.minecraft.command.ICommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import ru.craftlogic.api.command.CommandContainer;
import ru.craftlogic.api.event.EventManager;
import ru.craftlogic.api.event.Listener;
import ru.craftlogic.api.event.player.PlayerEvent;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;
import ru.craftlogic.common.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Server implements Permissible, Listener {
    private final MinecraftServer server;
    private final PermissionManager permissionManager;
    private final CommandRegistry commandRegistry;
    private final RegionManager regionManager;
    private final EventManager eventManager;
    private final Set<OnlinePlayer> onlinePlayers = new HashSet<>();
    private final Set<World> loadedWorlds = new HashSet<>();

    public Server(MinecraftServer server) {
        this.server = server;
        this.permissionManager = new PermissionManager(this);
        this.commandRegistry = new CommandRegistry(this, (ServerCommandManager) server.commandManager);
        this.regionManager = new RegionManager(this);
        this.eventManager = new EventManager(this);
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("Server");
    }

    public Set<OnlinePlayer> getOnlinePlayers() {
        return ImmutableSet.copyOf(this.onlinePlayers);
    }

    public GameProfile[] getOnlinePlayerProfiles() {
        return this.server.getOnlinePlayerProfiles();
    }

    public String[] getOnlinePlayerNames() {
        return this.server.getOnlinePlayerNames();
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public RegionManager getRegionManager() {
        return this.regionManager;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public Path getDataDirectory() {
        return this.server.getDataDirectory().toPath();
    }

    public OnlinePlayer getPlayer(GameProfile profile) {
        for (OnlinePlayer player : this.getOnlinePlayers()) {
            GameProfile p = player.getProfile();
            if (profile.getId() == null ? profile.getName().equals(p.getName()) : profile.getId().equals(p.getId()))
                return player;
        }
        return null;
    }

    public OnlinePlayer getPlayerByName(String name) {
        for (OnlinePlayer player : this.getOnlinePlayers()) {
            GameProfile p = player.getProfile();
            if (name.equals(p.getName()))
                return player;
        }
        return null;
    }

    @Nullable
    public Player getOfflinePlayerByName(String name) {
        GameProfile profile = this.getProfileCache().getGameProfileForUsername(name);
        if (profile != null) {
            return new Player(this, profile);
        } else {
            return null;
        }
    }

    public PlayerProfileCache getProfileCache() {
        return this.server.getPlayerProfileCache();
    }

    public Set<World> getLoadedWorlds() {
        return ImmutableSet.copyOf(this.loadedWorlds);
    }

    @Nullable
    public World getWorld(Dimension dimension) {
        for (World world : this.loadedWorlds) {
            if (dimension == world.getDimension()) {
                return world;
            }
        }
        return null;
    }

    @Nullable
    public World getWorld(String name) {
        for (World world : this.loadedWorlds) {
            if (name.equalsIgnoreCase(world.getName())) {
                return world;
            }
        }
        return null;
    }

    public WorldServer getWorldByDimension(int dimensionId) {
        for (WorldServer world : this.server.worlds) {
            if (world.provider.getDimension() == dimensionId) {
                return world;
            }
        }
        return null;
    }

    public ICommand registerCommand(ICommand command) {
        return ((ServerCommandManager)this.server.commandManager).registerCommand(command);
    }

    public void registerCommands(Class<? extends CommandContainer> containerClass) {
        this.commandRegistry.registerCommandContainer(containerClass);
    }

    public boolean isPlayerOnline(GameProfile profile) {
        for (GameProfile p : this.getOnlinePlayerProfiles()) {
            if (profile.getId() == null ? profile.getName().equals(p.getName()) : profile.getId().equals(p.getId()))
                return true;
        }
        return false;
    }

    public int isPlayerOperator(GameProfile player) {
        return this.server.getPlayerList().getOppedPlayers().getPermissionLevel(player);
    }

    public boolean isBypassesPlayerLimit(GameProfile player) {
        return this.server.getPlayerList().bypassesPlayerLimit(player);
    }

    public int isPlayerOperator(Player player) {
        return this.isPlayerOperator(player.getProfile());
    }

    public boolean isBypassesPlayerLimit(Player player) {
        return this.isBypassesPlayerLimit(player.getProfile());
    }

    public boolean isPlayerWhitelisted(GameProfile player) {
        return this.server.getPlayerList().getWhitelistedPlayers().isWhitelisted(player);
    }

    public boolean isPlayerWhitelisted(Player player) {
        return this.isPlayerWhitelisted(player.getProfile());
    }

    public void start() throws Exception {
        this.registerCommands(GameplayCommands.class);
        this.registerCommands(PermissionCommands.class);
        this.registerCommands(RegionCommands.class);

        this.permissionManager.load();
        this.regionManager.load();
        this.commandRegistry.load();
    }

    public void stop(StopReason reason) {
        if (reason != StopReason.CORE) {
            this.server.stopServer();
        }
    }

    public enum StopReason {
        PLUGIN, CORE
    }

    @Listen
    private void onPlayerJoined(PlayerEvent.Joined event) {
        OnlinePlayer player = event.getPlayer();
        this.onlinePlayers.add(player);
    }

    @Listen
    private void onPlayerLeft(PlayerEvent.Left event) {
        OnlinePlayer player = event.getPlayer();
        this.onlinePlayers.remove(player);
    }
}
