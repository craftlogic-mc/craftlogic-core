package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import ru.craftlogic.api.Permissible;
import ru.craftlogic.api.Server;

public class Player implements Permissible {
    protected final Server server;
    protected final GameProfile profile;

    public Player(Server server, GameProfile profile) {
        this.server = server;
        this.profile = profile;
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        return this.server.getPermissionManager().hasPermissions(this.profile, permissions);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.profile.getName());
    }

    public int isOperator() {
        return this.server.isPlayerOperator(this);
    }

    public boolean isBypassesPlayerLimit() {
        return this.server.isBypassesPlayerLimit(this);
    }

    public boolean isWhitelisted() {
        return this.server.isPlayerWhitelisted(this);
    }

    public GameProfile getProfile() {
        return this.profile;
    }
}
