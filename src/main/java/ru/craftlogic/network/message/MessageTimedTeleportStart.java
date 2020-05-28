package ru.craftlogic.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Location;

public class MessageTimedTeleportStart extends AdvancedMessage {
    private Location pos;
    private int timeout;
    private boolean freeze;

    public MessageTimedTeleportStart() {}

    public MessageTimedTeleportStart(Location pos, int timeout, boolean freeze) {
        this.pos = pos;
        this.timeout = timeout;
        this.freeze = freeze;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        pos = buf.readEntityLocation();
        timeout = buf.readInt();
        freeze = buf.readBoolean();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeEntityLocation(pos);
        buf.writeInt(timeout);
        buf.writeBoolean(freeze);
    }

    public Location getPos() {
        return pos;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isFreeze() {
        return freeze;
    }
}
