package ru.craftlogic.network.message;

import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;

import java.io.IOException;

public class MessageToastQuestion extends AdvancedMessage {
    private String id;
    private ITextComponent question;
    private int color;
    private int timeout;

    public MessageToastQuestion() {}

    public MessageToastQuestion(String id, ITextComponent question, int color, int timeout) {
        this.id = id;
        this.question = question;
        this.color = color;
        this.timeout = timeout;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.id = buf.readString(Short.MAX_VALUE);
        this.question = buf.readTextComponent();
        this.color = buf.readInt();
        this.timeout = buf.readInt();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeString(this.id);
        buf.writeTextComponent(this.question);
        buf.writeInt(this.color);
        buf.writeInt(this.timeout);
    }

    public String getId() {
        return id;
    }

    public ITextComponent getQuestion() {
        return question;
    }

    public int getColor() {
        return color;
    }

    public int getTimeout() {
        return timeout;
    }
}
