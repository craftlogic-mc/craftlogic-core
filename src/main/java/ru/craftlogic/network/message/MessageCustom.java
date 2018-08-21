package ru.craftlogic.network.message;

import net.minecraft.nbt.NBTTagCompound;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.IOException;

public class MessageCustom extends AdvancedMessage {
    private String channel;
    private NBTTagCompound data;

    @ReflectiveUsage
    public MessageCustom() {}

    public MessageCustom(String channel, NBTTagCompound data) {
        this.channel = channel;
        this.data = data;
    }

    @Override
    protected void read(AdvancedBuffer buf) throws IOException {
        this.channel = buf.readString(Short.MAX_VALUE);
        this.data = buf.readCompoundTag();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeString(this.channel);
        buf.writeCompoundTag(this.data);
    }

    public String getChannel() {
        return channel;
    }

    public NBTTagCompound getData() {
        return data;
    }
}
