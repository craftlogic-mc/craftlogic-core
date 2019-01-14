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
        PermissionManager permissionManager = this.server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            return permissionManager.hasPermission(this.profile, permission);
        } else {
            return getOperatorLevel() >= opLevel;
        }
    }

    @Override
    public <T> T getPermissionMetadata(String meta, T def, Function<String, T> mapper) {
        PermissionManager permissionManager = this.server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            String m = permissionManager.getPermissionMetadata(this.profile, meta);
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
        return new TextComponentString(this.profile.getName());
    }

    public boolean isOperator() {
        return this.server.getPlayerManager().isOperator(this.profile);
    }

    public int getOperatorLevel() {
        return this.server.getPlayerManager().getOperatorLevel(this.profile);
    }

    public boolean setOperator(boolean operator, int level, boolean bypassPlayerLimit) {
        return this.server.getPlayerManager().setOperator(this.profile, operator, level, bypassPlayerLimit);
    }

    public boolean isBypassesPlayerLimit() {
        return this.server.getPlayerManager().isBypassesPlayerLimit(this.profile);
    }

    public boolean isWhitelisted() {
        return this.server.getPlayerManager().isWhitelisted(this.profile);
    }

    public void setWhitelisted(boolean whitelisted) {
        this.server.getPlayerManager().setWhitelisted(this.profile, whitelisted);
    }

    public boolean isOnline() {
        return this.server.getPlayerManager().isOnline(this.profile);
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public String getName() {
        return this.profile.getName();
    }

    public UUID getId() {
        return this.profile.getId();
    }

    public boolean hasData(World world) {
        return isOnline() || asPhantom(world).hasData(world);
    }

    public PhantomPlayer asPhantom(World world) {
        return new PhantomPlayer(world, this.getProfile());
    }

    public Player asOnline() {
        return this.server.getPlayerManager().getOnline(this.profile);
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
                "name=" + this.getDisplayName().getFormattedText() +
                ", id=" + profile.getId() +
                '}';
    }
}
