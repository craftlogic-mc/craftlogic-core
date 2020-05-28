package ru.craftlogic.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Location;

public class MessageTimedTeleportEnd extends AdvancedMessage {
    private Location pos;

    public MessageTimedTeleportEnd() {}

    public MessageTimedTeleportEnd(Location pos) {
        this.pos = pos;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        pos = buf.readEntityLocation();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeEntityLocation(pos);
    }

    public Location getPos() {
        return pos;
    }
}
