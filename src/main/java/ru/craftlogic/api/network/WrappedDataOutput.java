package ru.craftlogic.api.network;

import net.minecraft.network.PacketBuffer;

import java.io.DataOutput;
import java.nio.charset.StandardCharsets;

public class WrappedDataOutput implements DataOutput {
    private PacketBuffer buffer;

    public WrappedDataOutput(PacketBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) {
        this.buffer.writeByte(b);
    }

    @Override
    public void write(byte[] b) {
        this.buffer.writeBytes(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        this.buffer.writeBytes(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) {
        this.buffer.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) {
        this.buffer.writeByte(v);
    }

    @Override
    public void writeShort(int v) {
        this.buffer.writeShort(v);
    }

    @Override
    public void writeChar(int v) {
        this.buffer.writeChar(v);
    }

    @Override
    public void writeInt(int v) {
        this.buffer.writeInt(v);
    }

    @Override
    public void writeLong(long v) {
        this.buffer.writeLong(v);
    }

    @Override
    public void writeFloat(float v) {
        this.buffer.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) {
        this.buffer.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) {
        int len = s.length();
        byte[] b = new byte[len];
        s.getBytes(0, len, b, 0);
        this.buffer.writeBytes(b, 0, len);
    }

    @Override
    public void writeChars(String s) {
        this.buffer.writeCharSequence(s, StandardCharsets.UTF_8);
    }

    @Override
    public void writeUTF(String s) {
        this.buffer.writeString(s);
    }
}
