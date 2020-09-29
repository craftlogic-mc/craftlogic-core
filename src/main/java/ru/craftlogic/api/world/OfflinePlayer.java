package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import ru.craftlogic.api.permission.PermissionManager;
import ru.craftlogic.api.server.Server;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class OfflinePlayer implements Permissible {
    protected final Server server;
    protected final GameProfile profile;

    public OfflinePlayer(Server server, GameProfile profile) {
        this.server = server;
        this.profile = profile;
    }

    @Override
    public boolean hasPermission(String permission, int opLevel) {
        PermissionManager permissionManager = server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            return permissionManager.hasPermission(profile, permission);
        } else {
            return getOperatorLevel() >= opLevel;
        }
    }

    @Override
    public <T> T getPermissionMetadata(String meta, T def, Function<String, T> mapper) {
        PermissionManager permissionManager = server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            String m = permissionManager.getPermissionMetadata(profile, meta);
            if (m != null) {
                try {
                    return mapper.apply(m);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return def;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(profile.getName());
    }

    public boolean isOperator() {
        return server.getPlayerManager().isOperator(profile);
    }

    public int getOperatorLevel() {
        return server.getPlayerManager().getOperatorLevel(profile);
    }

    public boolean setOperator(boolean operator, int level, boolean bypassPlayerLimit) {
        return server.getPlayerManager().setOperator(profile, operator, level, bypassPlayerLimit);
    }

    public boolean isBypassesPlayerLimit() {
        return server.getPlayerManager().isBypassesPlayerLimit(profile);
    }

    public boolean isWhitelisted() {
        return server.getPlayerManager().isWhitelisted(profile);
    }

    public void setWhitelisted(boolean whitelisted) {
        server.getPlayerManager().setWhitelisted(profile, whitelisted);
    }

    public boolean isOnline() {
        return server.getPlayerManager().isOnline(profile);
    }

    public GameProfile getProfile() {
        return profile;
    }

    public String getName() {
        return profile.getName();
    }

    public UUID getId() {
        return profile.getId();
    }

    public boolean hasData(World world) {
        return isOnline() || asPhantom(world).hasData(world);
    }

    public PhantomPlayer asPhantom(World world) {
        return new PhantomPlayer(world, getProfile());
    }

    public Player asOnline() {
        return server.getPlayerManager().getOnline(profile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfflinePlayer)) return false;
        OfflinePlayer that = (OfflinePlayer) o;
        return Objects.equals(profile.getId(), that.profile.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile.getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name=" + getDisplayName().getFormattedText() +
                ", id=" + profile.getId() +
                '}';
    }
}
