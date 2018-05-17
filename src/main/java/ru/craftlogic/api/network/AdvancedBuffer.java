package ru.craftlogic.api.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import ru.craftlogic.api.world.Location;

public class AdvancedBuffer extends PacketBuffer {
    public AdvancedBuffer(ByteBuf buf) {
        super(buf);
    }

    public AdvancedBuffer writeBlockLocation(Location location) {
        this.writeInt(location.getDimension());
        this.writeLong(location.getPos().toLong());
        return this;
    }

    public AdvancedBuffer writeEntityLocation(Location location) {
        this.writeInt(location.getDimension());
        this.writeDouble(location.getX());
        this.writeDouble(location.getY());
        this.writeDouble(location.getZ());
        this.writeFloat(location.getYaw());
        this.writeFloat(location.getPitch());
        return this;
    }

    public Location readBlockLocation() {
        return new Location(readInt(), BlockPos.fromLong(readLong()));
    }

    public Location readEntityLocation() {
        return new Location(readInt(), readDouble(), readDouble(), readDouble(), readFloat(), readFloat());
    }
}
