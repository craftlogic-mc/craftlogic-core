package ru.craftlogic.network.message;

import com.mojang.authlib.GameProfile;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.io.IOException;

public class MessagePlayerInfo extends AdvancedMessage {
    private GameProfile profile;
    private long firstPlayed, lastPlayed, timePlayed;
    private boolean allowEdit;
    private Location lastLocation, bedLocation;

    public MessagePlayerInfo() {}

    public MessagePlayerInfo(GameProfile profile, long firstPlayed, long lastPlayed, long timePlayed, boolean allowEdit,
                             Location lastLocation, @Nullable Location bedLocation) {
        this.profile = profile;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
        this.timePlayed = timePlayed;
        this.allowEdit = allowEdit;
        this.lastLocation = lastLocation;
        this.bedLocation = bedLocation;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.profile = buf.readProfile();
        this.firstPlayed = buf.readLong();
        this.lastPlayed = buf.readLong();
        this.timePlayed = buf.readLong();
        this.allowEdit = buf.readBoolean();
        this.lastLocation = buf.readEntityLocation();
        if (buf.readBoolean()) {
            this.bedLocation = buf.readBlockLocation();
        }
    }

    @Override
    protected void write(AdvancedBuffer buf) throws IOException {
        buf.writeProfile(this.profile);
        buf.writeLong(this.firstPlayed);
        buf.writeLong(this.lastPlayed);
        buf.writeLong(this.timePlayed);
        buf.writeBoolean(this.allowEdit);
        buf.writeEntityLocation(this.lastLocation);
        if (this.bedLocation != null) {
            buf.writeBoolean(true);
            buf.writeBlockLocation(this.bedLocation);
        } else {
            buf.writeBoolean(false);
        }
    }

    public GameProfile getProfile() {
        return profile;
    }

    public long getFirstPlayed() {
        return firstPlayed;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public boolean isEditAllowed() {
        return allowEdit;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    @Nullable
    public Location getBedLocation() {
        return bedLocation;
    }
}
