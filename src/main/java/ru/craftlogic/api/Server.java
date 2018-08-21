package ru.craftlogic.api;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import jline.internal.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.server.AdvancedPlayerList;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.*;
import ru.craftlogic.common.chat.ChatManager;
import ru.craftlogic.common.command.CommandManager;
import ru.craftlogic.common.economy.EconomyManager;
import ru.craftlogic.common.permission.PermissionManager;
import ru.craftlogic.common.region.RegionManager;
import ru.craftlogic.common.script.ScriptManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

public class Server implements CommandSender {
    final MinecraftServer server;
    final Map<Class<? extends ConfigurableManager>, ConfigurableManager> managers = new HashMap<>();
    final Set<Player> onlinePlayers = new HashSet<>();
    final Set<World> loadedWorlds = new HashSet<>();

    public Server(MinecraftServer server) {
        this.server = server;
        this.addManager(PermissionManager.class, PermissionManager::new);
        this.addManager(CommandManager.class, CommandManager::new);
        this.addManager(RegionManager.class, RegionManager::new);
        this.addManager(ScriptManager.class, ScriptManager::new);
        this.addManager(ChatManager.class, ChatManager::new);
        this.addManager(EconomyManager.class, EconomyManager::new);
        MinecraftForge.EVENT_BUS.register(new EventConverter(this));
    }

    public <M extends ConfigurableManager> void addManager(Class<? extends M> type, BiFunction<Server, Path, M> factory) {
        if (this.managers.containsKey(type)) {
            throw new IllegalStateException("Manager of type " + type + " is already registered!");
        }
        M manager = factory.apply(this, this.getSettingsDirectory());
        this.managers.put(type, manager);
        MinecraftForge.EVENT_BUS.register(manager);
    }

    public <M extends ConfigurableManager> M getManager(Class<? extends M> type) {
        return (M) this.managers.get(type);
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public String getName() {
        return "Server";
    }

    public Player[] getOnlinePlayers() {
        return this.onlinePlayers.toArray(new Player[0]);
    }

    public GameProfile[] getOnlinePlayerProfiles() {
        return this.server.getOnlinePlayerProfiles();
    }

    public String[] getOnlinePlayerNames() {
        return this.server.getOnlinePlayerNames();
    }

    public CommandManager getCommandManager() {
        return this.getManager(CommandManager.class);
    }

    public PermissionManager getPermissionManager() {
        return this.getManager(PermissionManager.class);
    }

    public RegionManager getRegionManager() {
        return this.getManager(RegionManager.class);
    }

    public ScriptManager getScriptManager() {
        return this.getManager(ScriptManager.class);
    }

    public ChatManager getChatManager() {
        return this.getManager(ChatManager.class);
    }

    public EconomyManager getEconomyManager() {
        return this.getManager(EconomyManager.class);
    }

    public Path getDataDirectory() {
        return this.server.getDataDirectory().toPath();
    }

    public Path getSettingsDirectory() {
        return this.getDataDirectory().resolve("settings/");
    }

    public Player getPlayer(GameProfile profile) {
        for (Player player : this.getOnlinePlayers()) {
            GameProfile p = player.getProfile();
            if (profile.getId() == null ? p.getName().equals(profile.getName()) : profile.getId().equals(p.getId()))
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
        return profile == null ? null : this.getOfflinePlayer(profile);
    }

    public OfflinePlayer getOfflinePlayer(@Nonnull UUID id) {
        GameProfile profile = this.getProfileCache().getProfileByUUID(id);
        return profile == null ? null : this.getOfflinePlayer(profile);
    }

    public OfflinePlayer getOfflinePlayer(@Nonnull GameProfile profile) {
        return this.isPlayerOnline(profile) ? this.getPlayer(profile) : new OfflinePlayer(this, profile);
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
        UserListOps ops = this.server.getPlayerList().getOppedPlayers();
        if (operator) {
            if (this.isPlayerOperator(player)) {
                UserListOpsEntry entry = ops.getEntry(player);
                if (entry.getPermissionLevel() != level || entry.bypassesPlayerLimit() != bypassPlayerLimit) {
                    ops.removeEntry(player);
                    this.setPlayerOperator(player, true, level, bypassPlayerLimit);
                    return true;
                }
            } else {
                ops.addEntry(new UserListOpsEntry(player, level, bypassPlayerLimit));
                return true;
            }
        } else if (this.isPlayerOperator(player)) {
            ops.removeEntry(player);
            return true;
        }
        return false;
    }

    public boolean isPlayerBypassesPlayerLimit(GameProfile player) {
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

    public void start() {
        if (!Files.exists(this.getSettingsDirectory())) {
            try {
                Files.createDirectory(this.getSettingsDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CommandManager commandManager = this.getCommandManager();

        for (ConfigurableManager manager : this.managers.values()) {
            try {
                manager.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (ConfigurableManager manager : this.managers.values()) {
            manager.registerCommands(commandManager);
        }

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
        this.getManager(ScriptManager.class).unload();

        for (ConfigurableManager manager : this.managers.values()) {
            try {
                manager.save(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
            return "IntegratedServer{" +
                    "motd=" + handle.getMOTD() +
                    '}';
        }
    }

    public Set<GameProfile> getOperatorProfiles() {
        return this.getOperatorProfiles(1);
    }

    public Set<GameProfile> getOperatorProfiles(int level) {
        UserListOps oppedPlayers = this.server.getPlayerList().getOppedPlayers();
        Set<GameProfile> result = new HashSet<>();
        for (String username : oppedPlayers.getKeys()) {
            GameProfile profile = oppedPlayers.getGameProfileFromName(username);
            if (profile != null && oppedPlayers.getPermissionLevel(profile) >= level) {
                result.add(profile);
            }
        }
        return result;
    }

    public Set<OfflinePlayer> getOperators() {
        return this.getOperators(1);
    }

    public Set<OfflinePlayer> getOperators(int level) {
        Set<OfflinePlayer> result = new HashSet<>();
        for (GameProfile profile : this.getOperatorProfiles(level)) {
            result.add(this.getOfflinePlayer(profile));
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

    public void broadcast(Text<?, ?> message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcastPacket(AdvancedMessage packet) {
        for (Player player : getOnlinePlayers()) {
            player.sendPacket(packet);
        }
    }

    public void broadcastPacket(String channel, NBTTagCompound packet) {
        for (Player player : getOnlinePlayers()) {
            player.sendPacket(channel, packet);
        }
    }

    public void broadcastToast(Text<?, ?> title, long timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendToast(title, timeout);
        }
    }

    public void broadcastToast(ITextComponent title, long timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendToast(title, timeout);
        }
    }

    public void broadcastToast(Text<?, ?> title, Text<?, ?> subtitle, long timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendToast(title, subtitle, timeout);
        }
    }

    public void broadcastToast(ITextComponent title, ITextComponent subtitle, long timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendToast(title, subtitle, timeout);
        }
    }

    public void broadcastCountdown(String id, Text<?, ?> title, int timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendCountdown(id, title, timeout);
        }
    }

    public void broadcastCountdown(String id, ITextComponent title, int timeout) {
        for (Player player : getOnlinePlayers()) {
            player.sendCountdown(id, title, timeout);
        }
    }

    public enum StopReason {
        PLUGIN, CORE
    }
}
