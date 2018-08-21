package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import ru.craftlogic.api.Server;

import java.util.Objects;
import java.util.UUID;

public class OfflinePlayer implements Permissible {
    protected final Server server;
    protected final GameProfile profile;
    protected FakePlayer fakeEntity;
    protected boolean loadedData;

    public OfflinePlayer(Server server, GameProfile profile) {
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

    public boolean isOperator() {
        return this.server.isPlayerOperator(this.profile);
    }

    public boolean setOperator(boolean operator, int level, boolean bypassPlayerLimit) {
        return this.server.setPlayerOperator(this.profile, operator, level, bypassPlayerLimit);
    }

    public int getPermissionLevel() {
        return this.server.getPermissionLevel(this.profile);
    }

    public boolean isBypassesPlayerLimit() {
        return this.server.isPlayerBypassesPlayerLimit(this.profile);
    }

    public boolean isWhitelisted() {
        return this.server.isPlayerWhitelisted(this.profile);
    }

    public void setWhitelisted(boolean whitelisted) {
        this.server.setPlayerWhitelisted(this.profile, whitelisted);
    }

    public boolean isOnline() {
        return this.server.isPlayerOnline(this.profile);
    }

    public long getLastPlayed(World world) {
        if (this.loadData(world, true)) {
            return this.asFake(world).getLastActiveTime();
        } else {
            return 0;
        }
    }

    public Location getLastLocation(World world) {
        if (this.loadData(world, true)) {
            FakePlayer data = this.asFake(world);
            return new Location(world.getHandle(), data.posX, data.posY, data.posZ, data.rotationYaw, data.rotationPitch);
        } else {
            return null;
        }
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

    public boolean loadData(World world, boolean reload) {
        if ((this.fakeEntity == null || reload) && !this.loadedData) {
            NBTTagCompound data = this.server.loadOfflinePlayerData(this.asFake(world));
            if (data != null) {
                this.loadedData = true;
                return true;
            }
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

    public Player asOnline() {
        return this.server.getPlayer(this.profile);
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
