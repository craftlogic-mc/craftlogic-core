package ru.craftlogic.api.network;

import net.minecraft.network.PacketBuffer;

import java.io.DataInput;

public class WrappedDataInput implements DataInput {
    private PacketBuffer buffer;

    public WrappedDataInput(PacketBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void readFully(byte[] b) {
        this.buffer.readBytes(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        this.buffer.readBytes(b, off, len);
    }

    @Override
    public int skipBytes(int n) {
        this.buffer.skipBytes(n);
        return n;
    }

    @Override
    public boolean readBoolean() {
        return this.buffer.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.buffer.readByte();
    }

    @Override
    public int readUnsignedByte() {
        return this.buffer.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.buffer.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return this.buffer.readUnsignedShort();
    }

    @Override
    public char readChar() {
        return this.buffer.readChar();
    }

    @Override
    public int readInt() {
        return this.buffer.readInt();
    }

    @Override
    public long readLong() {
        return this.buffer.readLong();
    }

    @Override
    public float readFloat() {
        return this.buffer.readFloat();
    }

    @Override
    public double readDouble() {
        return this.buffer.readDouble();
    }

    @Override
    public String readLine() {
        return this.readUTF();
    }

    @Override
    public String readUTF() {
        return this.buffer.readString(Short.MAX_VALUE);
    }
}
