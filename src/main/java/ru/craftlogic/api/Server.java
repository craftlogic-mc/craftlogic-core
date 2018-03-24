package ru.craftlogic.api;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import jline.internal.Nullable;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import ru.craftlogic.api.command.AdvancedCommandManager;
import ru.craftlogic.api.command.CommandContainer;
import ru.craftlogic.api.command.CommandExecutor;
import ru.craftlogic.api.event.EventManager;
import ru.craftlogic.api.event.Listener;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;
import ru.craftlogic.common.command.CommandRegistry;
import ru.craftlogic.common.command.GameplayCommands;
import ru.craftlogic.common.permission.PermissionCommands;
import ru.craftlogic.common.permission.PermissionManager;
import ru.craftlogic.common.region.RegionCommands;
import ru.craftlogic.common.region.RegionManager;
import ru.craftlogic.common.script.ScriptCommands;
import ru.craftlogic.common.script.ScriptManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server implements Permissible, Listener {
    final MinecraftServer server;
    final PermissionManager permissionManager;
    final CommandRegistry commandRegistry;
    final RegionManager regionManager;
    final EventManager eventManager;
    final ScriptManager scriptManager;
    final Set<OnlinePlayer> onlinePlayers = new HashSet<>();
    final Set<World> loadedWorlds = new HashSet<>();

    public Server(MinecraftServer server) {
        this.server = server;
        this.permissionManager = new PermissionManager(this);
        this.commandRegistry = new CommandRegistry(this, (AdvancedCommandManager) server.commandManager);
        this.regionManager = new RegionManager(this);
        this.eventManager = new EventManager(this);
        this.scriptManager = new ScriptManager(this);
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

    public ScriptManager getScriptManager() {
        return this.scriptManager;
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
        return ((AdvancedCommandManager)this.server.commandManager).registerCommand(command);
    }

    public boolean unregisterCommand(ICommand command) {
        return this.commandRegistry.unregisterCommand(command);
    }

    public ICommand registerCommand(String name, List<String> syntax, List<String> aliases, List<String> permissions, CommandExecutor executor) {
        return this.commandRegistry.registerCommand(name, syntax, aliases, permissions, executor);
    }

    public void registerCommands(Class<? extends CommandContainer> containerClass) {
        this.commandRegistry.registerCommandContainer(containerClass);
    }

    public boolean unregisterCommands(Class<? extends CommandContainer> containerClass) {
        return this.commandRegistry.unregisterCommandContainer(containerClass);
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

    public boolean isPlayerWhitelisted(GameProfile player) {
        return this.server.getPlayerList().getWhitelistedPlayers().isWhitelisted(player);
    }

    public void start() throws Exception {
        this.registerCommands(GameplayCommands.class);
        this.registerCommands(PermissionCommands.class);
        this.registerCommands(RegionCommands.class);
        this.registerCommands(ScriptCommands.class);

        this.permissionManager.load();
        this.regionManager.load();
        this.commandRegistry.load();
        this.scriptManager.load();
    }

    public void stop(StopReason reason) {
        this.scriptManager.unload();

        if (reason != StopReason.CORE) {
            this.server.stopServer();
        }
    }

    public NBTTagCompound loadOfflinePlayerData(FakePlayer player) {
        return this.server.getPlayerList().readPlayerDataFromFile(player);
    }

    public void saveOfflinePlayerData(FakePlayer player) {
        try {
            Method saveData = PlayerList.class.getDeclaredMethod("writePlayerData", EntityPlayerMP.class);
            saveData.setAccessible(true);
            saveData.invoke(this.server.getPlayerList(), player);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSinglePlayer() {
        return this.server.isSinglePlayer();
    }

    public enum StopReason {
        PLUGIN, CORE
    }
}
