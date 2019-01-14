package ru.craftlogic.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.util.ReflectiveUsage;

public class MessageServerStop extends AdvancedMessage {
    private int delay;
    private int reconnect;

    @Deprecated
    @ReflectiveUsage
    public MessageServerStop() {}

    public MessageServerStop(int delay, int reconnect) {
        this.delay = delay;
        this.reconnect = reconnect;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        this.delay = buf.readInt();
        this.reconnect = buf.readInt();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeInt(this.delay);
        buf.writeInt(this.reconnect);
    }

    public int getDelay() {
        return delay;
    }

    public int getReconnect() {
        return reconnect;
    }
}
