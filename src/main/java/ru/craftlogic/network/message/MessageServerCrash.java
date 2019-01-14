package ru.craftlogic.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.util.ReflectiveUsage;

public class MessageServerCrash extends AdvancedMessage {
    private int reconnect;

    @Deprecated
    @ReflectiveUsage
    public MessageServerCrash() {}

    public MessageServerCrash(int reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        this.reconnect = buf.readInt();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeInt(this.reconnect);
    }

    public int getReconnect() {
        return reconnect;
    }
}
