package ru.craftlogic.api.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public abstract class AdvancedMessage implements IMessage {
    @Override
    public final void fromBytes(ByteBuf buf) {
        try {
            this.read(new AdvancedBuffer(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void read(AdvancedBuffer buf) throws IOException;

    @Override
    public final void toBytes(ByteBuf buf) {
        try {
            this.write(new AdvancedBuffer(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void write(AdvancedBuffer buf) throws IOException;
}
