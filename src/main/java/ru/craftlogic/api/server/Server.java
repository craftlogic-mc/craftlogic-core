package ru.craftlogic.api.server;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import jline.internal.Nullable;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.command.CommandExecutor;
import ru.craftlogic.api.command.CommandRegisterer;
import ru.craftlogic.api.event.EventManager;
import ru.craftlogic.api.event.Listener;
import ru.craftlogic.api.world.*;
import ru.craftlogic.common.command.CommandRegistry;
import ru.craftlogic.common.command.GameplayCommands;
import ru.craftlogic.common.command.ManagementCommands;
import ru.craftlogic.common.permission.PermissionCommands;
import ru.craftlogic.common.permission.PermissionManager;
import ru.craftlogic.common.region.RegionCommands;
import ru.craftlogic.common.region.RegionManager;
import ru.craftlogic.common.script.ScriptCommands;
import ru.craftlogic.common.script.ScriptManager;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server implements CommandSender, Listener {
    final MinecraftServer server;
    final PermissionManager permissionManager;
    final CommandRegistry commandRegistry;
    final RegionManager regionManager;
    final EventManager eventManager;
    final ScriptManager scriptManager;
    final Set<Player> onlinePlayers = new HashSet<>();
    final Set<World> loadedWorlds = new HashSet<>();

    public Server(MinecraftServer server) {
        this.server = server;
        this.permissionManager = new PermissionManager(this);
        this.commandRegistry = new CommandRegistry(this, (CommandHandler) server.commandManager);
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

    public Set<Player> getOnlinePlayers() {
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

    public Player getPlayer(GameProfile profile) {
        for (Player player : this.getOnlinePlayers()) {
            GameProfile p = player.getProfile();
            if (profile.getId() == null ? profile.getName().equals(p.getName()) : profile.getId().equals(p.getId()))
                return player;
        }
        return null;
    }

    public Player getPlayerByName(String name) {
        for (Player player : this.getOnlinePlayers()) {
            GameProfile p = player.getProfile();
            if (name.equals(p.getName()))
                return player;
        }
        return null;
    }

    @Nullable
    public OfflinePlayer getOfflinePlayerByName(String name) {
        GameProfile profile = this.getProfileCache().getGameProfileForUsername(name);
        return profile == null ? null : new OfflinePlayer(this, profile);
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

    public ICommand registerCommand(ICommand command) {
        return ((CommandHandler)this.server.commandManager).registerCommand(command);
    }

    public boolean unregisterCommand(ICommand command) {
        return this.commandRegistry.unregisterCommand(command);
    }

    public ICommand registerCommand(String name, List<String> syntax, List<String> aliases, List<String> permissions, CommandExecutor executor) {
        return this.commandRegistry.registerCommand(name, syntax, aliases, permissions, executor);
    }

    public void registerCommands(Class<? extends CommandRegisterer> containerClass) {
        this.commandRegistry.registerCommandContainer(containerClass);
    }

    public boolean unregisterCommands(Class<? extends CommandRegisterer> containerClass) {
        return this.commandRegistry.unregisterCommandContainer(containerClass);
    }

    public boolean isPlayerOnline(GameProfile profile) {
        for (GameProfile p : this.getOnlinePlayerProfiles()) {
            if (profile.getId() == null ? profile.getName().equals(p.getName()) : profile.getId().equals(p.getId()))
                return true;
        }
        return false;
    }

    public int getPermissionLevel(GameProfile player) {
        return this.server.getPlayerList().getOppedPlayers().getPermissionLevel(player);
    }

    public boolean isPlayerOperator(GameProfile player) {
        return this.server.getPlayerList().getOppedPlayers().getEntry(player) != null;
    }

    public boolean setPlayerOperator(GameProfile player, boolean operator, int level, boolean bypassPlayerLimit) {
        UserListOps oppedPlayers = this.server.getPlayerList().getOppedPlayers();
        if (operator) {
            if (this.isPlayerOperator(player)) {
                UserListOpsEntry entry = oppedPlayers.getEntry(player);
                if (entry.getPermissionLevel() != level || entry.bypassesPlayerLimit() != bypassPlayerLimit) {
                    oppedPlayers.removeEntry(player);
                    this.setPlayerOperator(player, true, level, bypassPlayerLimit);
                    return true;
                }
            } else {
                oppedPlayers.addEntry(new UserListOpsEntry(player, level, bypassPlayerLimit));
            }
        } else if (this.isPlayerOperator(player)) {
            oppedPlayers.removeEntry(player);
            return true;
        }
        return false;
    }

    public boolean isBypassesPlayerLimit(GameProfile player) {
        return this.server.getPlayerList().bypassesPlayerLimit(player);
    }

    public boolean isPlayerWhitelisted(GameProfile player) {
        return this.server.getPlayerList().getWhitelistedPlayers().isWhitelisted(player);
    }

    public boolean setPlayerWhitelisted(GameProfile profile, boolean whitelisted) {
        UserListWhitelist whitelistedPlayers = this.server.getPlayerList().getWhitelistedPlayers();
        if (whitelistedPlayers.isWhitelisted(profile) && whitelisted) {
            whitelistedPlayers.removeEntry(profile);
            return true;
        } else if (!whitelisted) {
            whitelistedPlayers.addEntry(new UserListWhitelistEntry(profile));
            return true;
        }
        return false;
    }

    public void start() throws Exception {
        this.registerCommands(GameplayCommands.class);
        this.registerCommands(ManagementCommands.class);
        this.registerCommands(PermissionCommands.class);
        this.registerCommands(RegionCommands.class);
        this.registerCommands(ScriptCommands.class);

        this.permissionManager.load();
        this.regionManager.load();
        this.commandRegistry.load();
        this.scriptManager.load();

        for (World world : this.loadedWorlds) {
            GameRules rules = world.getRules();
            rules.addGameRule("hidePlayerJoinMessages", "false", GameRules.ValueType.BOOLEAN_VALUE);
            rules.addGameRule("hidePlayerLeaveMessages", "false", GameRules.ValueType.BOOLEAN_VALUE);
        }
    }

    public void stop(StopReason reason) {
        if (reason != StopReason.CORE && !isDedicated() && !isIntegratedPublic()) {
            throw new IllegalStateException("Can't stop integrated server!");
        }
        this.scriptManager.unload();

        if (reason != StopReason.CORE) {
            this.server.stopServer();
        }
    }

    public NBTTagCompound loadOfflinePlayerData(FakePlayer player) {
        return ((AdvancedPlayerList)this.server.getPlayerList()).getDataManager().readPlayerData(player);
    }

    public void saveOfflinePlayerData(FakePlayer player) {
        ((AdvancedPlayerList)this.server.getPlayerList()).getDataManager().writePlayerData(player);
    }

    public boolean isSinglePlayer() {
        return this.server.isSinglePlayer();
    }

    @Override
    public MinecraftServer getHandle() {
        return this.server;
    }

    public boolean isDedicated() {
        return FMLCommonHandler.instance().getSide() == Side.SERVER;
    }

    public boolean isPublic() {
        return isDedicated() || isIntegratedPublic();
    }

    @SideOnly(Side.CLIENT)
    private boolean isIntegratedPublic() {
        IntegratedServer server = FMLClientHandler.instance().getClient().getIntegratedServer();
        return server != null && server.getPublic();
    }

    @Override
    public String toString() {
        MinecraftServer handle = getHandle();
        if (isDedicated()) {
            return "DedicatedServer{" +
                    "host=" + handle.getServerHostname() +
                    ", port=" + handle.getServerPort() +
                    ", motd=" + handle.getMOTD() +
                    '}';
        } else {
            return "InternalServer{" +
                    "motd=" + handle.getMOTD() +
                    '}';
        }
    }

    public Set<GameProfile> getOperatorProfiles() {
        UserListOps oppedPlayers = this.server.getPlayerList().getOppedPlayers();
        Set<GameProfile> result = new HashSet<>();
        for (String username : oppedPlayers.getKeys()) {
            GameProfile profile = oppedPlayers.getGameProfileFromName(username);
            if (profile != null) {
                result.add(profile);
            }
        }
        return result;
    }

    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> result = new HashSet<>();
        for (GameProfile profile : this.getOperatorProfiles()) {
            result.add(this.isPlayerOnline(profile) ? this.getPlayer(profile) : new OfflinePlayer(this, profile));
        }
        return result;
    }

    @Override
    public Location getLocation() {
        World overworld = getWorld(Dimension.OVERWORLD);
        return overworld.getSpawnLocation();
    }

    @Override
    public Server getServer() {
        return this;
    }

    public enum StopReason {
        PLUGIN, CORE
    }
}
