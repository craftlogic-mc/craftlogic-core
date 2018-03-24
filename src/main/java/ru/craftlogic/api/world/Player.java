package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import ru.craftlogic.api.Permissible;
import ru.craftlogic.api.Server;

public class Player implements Permissible {
    protected final Server server;
    protected final GameProfile profile;
    protected FakePlayer fakeEntity;

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
        return this.server.isPlayerOperator(this.profile);
    }

    public boolean isBypassesPlayerLimit() {
        return this.server.isBypassesPlayerLimit(this.profile);
    }

    public boolean isWhitelisted() {
        return this.server.isPlayerWhitelisted(this.profile);
    }

    public boolean isOnline() {
        return this.server.isPlayerOnline(this.profile);
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public boolean loadData(World world, boolean reload) {
        if (this.fakeEntity == null || reload) {
            NBTTagCompound data = this.server.loadOfflinePlayerData(this.asFake(world));
            return data != null;
        }
        return false;
    }

    public boolean saveData(World world, boolean unload) {
        if (this.fakeEntity != null) {
            this.server.saveOfflinePlayerData(this.fakeEntity);
            if (unload) {
                this.fakeEntity = null;
            }
            return true;
        } else {
            return false;
        }
    }

    public FakePlayer asFake(World world) {
        if (this.fakeEntity == null) {
            this.fakeEntity = FakePlayerFactory.get(world.getHandle(), this.profile);
        }
        return this.fakeEntity;
    }

    public OnlinePlayer asOnline() {
        return this.server.getPlayer(this.profile);
    }
}
