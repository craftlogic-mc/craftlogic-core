package ru.craftlogic.api.server;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import jline.internal.Nullable;
import net.minecraft.server.management.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import ru.craftlogic.api.event.player.PlayerJoinedMessageEvent;
import ru.craftlogic.api.event.player.PlayerLeftMessageEvent;
import ru.craftlogic.api.util.ServerManager;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerManager extends ServerManager {
    private final Set<Player> onlinePlayers = new HashSet<>();

    public PlayerManager(Server server, Path settingsDirectory) {
        super(server, LogManager.getLogger("PlayerManager"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = new Player(this.server, event.player.getGameProfile());
        this.onlinePlayers.add(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = getOnline(event.player.getGameProfile());
        this.onlinePlayers.remove(player);
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

    private <P extends PlayerList & AdvancedPlayerList> P getPlayerList() {
        return (P)this.server.unwrap().getPlayerList();
    }

    private PlayerProfileCache getProfileCache() {
        return this.server.unwrap().getPlayerProfileCache();
    }

    public AdvancedPlayerFileData getFileData() {
        return ((AdvancedPlayerList)this.server.unwrap().getPlayerList()).getDataManager();
    }

    public Set<Player> getAllOnline() {
        return ImmutableSet.copyOf(this.onlinePlayers);
    }

    public Set<GameProfile> getAllOnlineProfiles() {
        Set<GameProfile> result = new HashSet<>();
        for (Player player : getAllOnline()) result.add(player.getProfile());
        return ImmutableSet.copyOf(result);
    }

    public Set<String> getAllOnlineNames() {
        Set<String> result = new HashSet<>();
        for (Player player : getAllOnline()) result.add(player.getName());
        return ImmutableSet.copyOf(result);
    }

    public boolean isOnline(GameProfile profile) {
        for (GameProfile p : getAllOnlineProfiles()) {
            if (profile.getId() == null ? profile.getName().equals(p.getName()) : profile.getId().equals(p.getId()))
                return true;
        }
        return false;
    }

    public Player getOnline(GameProfile profile) {
        for (Player player : getAllOnline()) {
            GameProfile p = player.getProfile();
            if (profile.getId() == null ? p.getName().equals(profile.getName()) : profile.getId().equals(p.getId()))
                return player;
        }
        return null;
    }

    public Player getOnline(UUID id) {
        for (Player player : getAllOnline()) {
            if (id.equals(player.getId()))
                return player;
        }
        return null;
    }

    public Player getOnline(String name) {
        for (Player player : getAllOnline()) {
            GameProfile p = player.getProfile();
            if (name.equals(p.getName()))
                return player;
        }
        return null;
    }

    public Set<GameProfile> getAllOfflineProfiles() {
        Set<GameProfile> result = new HashSet<>();
        PlayerProfileCache profileCache = getProfileCache();
        for (String name : getAllOfflineNames()) result.add(profileCache.getGameProfileForUsername(name));
        return ImmutableSet.copyOf(result);
    }

    public Set<String> getAllOfflineNames() {
        return ImmutableSet.copyOf(getProfileCache().getUsernames());
    }

    public OfflinePlayer getOffline(@Nonnull GameProfile profile) {
        return isOnline(profile) ? getOnline(profile) : new OfflinePlayer(this.server, profile);
    }

    public OfflinePlayer getOffline(@Nonnull UUID id) {
        GameProfile profile = getProfileCache().getProfileByUUID(id);
        return profile == null ? null : this.getOffline(profile);
    }

    @Nullable
    public OfflinePlayer getOffline(String name) {
        GameProfile profile = getProfileCache().getGameProfileForUsername(name);
        return profile == null ? null : this.getOffline(profile);
    }

    public int getOperatorLevel(GameProfile player) {
        return getPlayerList().getOppedPlayers().getPermissionLevel(player);
    }

    public boolean isOperator(GameProfile player) {
        return getPlayerList().getOppedPlayers().getEntry(player) != null;
    }

    public boolean setOperator(GameProfile player, boolean operator, int level, boolean bypassPlayerLimit) {
        UserListOps ops = getPlayerList().getOppedPlayers();
        if (operator) {
            if (isOperator(player)) {
                UserListOpsEntry entry = ops.getEntry(player);
                if (entry.getPermissionLevel() != level || entry.bypassesPlayerLimit() != bypassPlayerLimit) {
                    ops.removeEntry(player);
                    setOperator(player, true, level, bypassPlayerLimit);
                    return true;
                }
            } else {
                ops.addEntry(new UserListOpsEntry(player, level, bypassPlayerLimit));
                return true;
            }
        } else if (isOperator(player)) {
            ops.removeEntry(player);
            return true;
        }
        return false;
    }

    public boolean isBypassesPlayerLimit(GameProfile player) {
        return getPlayerList().bypassesPlayerLimit(player);
    }

    public boolean isWhitelisted(GameProfile player) {
        return getPlayerList().getWhitelistedPlayers().isWhitelisted(player);
    }

    public boolean setWhitelisted(GameProfile profile, boolean whitelisted) {
        UserListWhitelist whitelistedPlayers = getPlayerList().getWhitelistedPlayers();
        if (whitelistedPlayers.isWhitelisted(profile) && whitelisted) {
            whitelistedPlayers.removeEntry(profile);
            return true;
        } else if (!whitelisted) {
            whitelistedPlayers.addEntry(new UserListWhitelistEntry(profile));
            return true;
        }
        return false;
    }

    public Set<GameProfile> getOperatorProfiles() {
        return this.getOperatorProfiles(1);
    }

    public Set<GameProfile> getOperatorProfiles(int level) {
        UserListOps oppedPlayers = getPlayerList().getOppedPlayers();
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
            result.add(getOffline(profile));
        }
        return result;
    }
}
