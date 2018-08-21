package ru.craftlogic.network.message;

import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.IOException;

public class MessageToast extends AdvancedMessage {
    private ITextComponent title, subtitle;
    private long timeout;

    @ReflectiveUsage
    public MessageToast() {}

    public MessageToast(ITextComponent title, long timeout) {
        this.title = title;
        this.timeout = timeout;
    }

    public MessageToast(ITextComponent title, ITextComponent subtitle, long timeout) {
        this.title = title;
        this.subtitle = subtitle;
        this.timeout = timeout;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.title = buf.readTextComponent();
        if (buf.readBoolean()) {
            this.subtitle = buf.readTextComponent();
        }
        this.timeout = buf.readLong();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeTextComponent(this.title);
        if (this.subtitle != null) {
            buf.writeBoolean(true);
            buf.writeTextComponent(this.subtitle);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeLong(this.timeout);
    }

    public ITextComponent getTitle() {
        return title;
    }

    public ITextComponent getSubtitle() {
        return subtitle;
    }

    public long getTimeout() {
        return timeout;
    }
}
