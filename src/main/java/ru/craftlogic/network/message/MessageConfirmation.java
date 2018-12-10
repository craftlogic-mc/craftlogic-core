package ru.craftlogic.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;

import java.io.IOException;

public class MessageConfirmation extends AdvancedMessage {
    private String id;
    private boolean choice;

    public MessageConfirmation() {}

    public MessageConfirmation(String id, boolean choice) {
        this.id = id;
        this.choice = choice;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.id = buf.readString(Short.MAX_VALUE);
        this.choice = buf.readBoolean();
    }

    @Override
    protected void write(AdvancedBuffer buf) throws IOException {
        buf.writeString(this.id);
        buf.writeBoolean(this.choice);
    }

    public String getId() {
        return id;
    }

    public boolean getChoice() {
        return choice;
    }
}
