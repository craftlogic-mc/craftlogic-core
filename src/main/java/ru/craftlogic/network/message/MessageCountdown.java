package ru.craftlogic.network.message;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.IOException;

public class MessageCountdown extends AdvancedMessage {
    private String id;
    private ITextComponent title;
    private int timeout;
    private int color;
    private SoundEvent tickSound;

    @ReflectiveUsage
    public MessageCountdown() {}

    public MessageCountdown(String id, ITextComponent title, int timeout, int color, SoundEvent tickSound) {
        this.id = id;
        this.title = title;
        this.timeout = timeout;
        this.color = color;
        this.tickSound = tickSound;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.id = buf.readString(Short.MAX_VALUE);
        this.title = buf.readTextComponent();
        this.timeout = buf.readInt();
        this.color = buf.readInt();
        ResourceLocation sound = new ResourceLocation(buf.readString(Short.MAX_VALUE));
        this.tickSound = SoundEvent.REGISTRY.getObject(sound);
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeString(this.id);
        buf.writeTextComponent(this.title);
        buf.writeInt(this.timeout);
        buf.writeInt(this.color);
        buf.writeString(this.tickSound.getRegistryName().toString());
    }

    public String getId() {
        return id;
    }

    public ITextComponent getTitle() {
        return title;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getColor() {
        return color;
    }

    public SoundEvent getTickSound() {
        return tickSound;
    }
}
