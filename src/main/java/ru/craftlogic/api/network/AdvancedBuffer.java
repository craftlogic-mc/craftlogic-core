package ru.craftlogic.api.network;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import ru.craftlogic.api.world.ChunkLocation;
import ru.craftlogic.api.world.Location;

public class AdvancedBuffer extends PacketBuffer {
    public AdvancedBuffer(ByteBuf buf) {
        super(buf);
    }

    public AdvancedBuffer writeBlockLocation(Location location) {
        writeInt(location.getDimensionId());
        writeLong(location.getPos().toLong());
        return this;
    }

    public AdvancedBuffer writeEntityLocation(Location location) {
        writeInt(location.getDimensionId());
        writeDouble(location.getX());
        writeDouble(location.getY());
        writeDouble(location.getZ());
        writeFloat(location.getYaw());
        writeFloat(location.getPitch());
        return this;
    }

    public Location readBlockLocation() {
        return new Location(readInt(), BlockPos.fromLong(readLong()));
    }

    public Location readEntityLocation() {
        return new Location(readInt(), readDouble(), readDouble(), readDouble(), readFloat(), readFloat());
    }

    public AdvancedBuffer writeChunkLocation(ChunkLocation location) {
        writeInt(location.getDimensionId());
        writeInt(location.getChunkX());
        writeInt(location.getChunkZ());
        return this;
    }

    public ChunkLocation readChunkLocation() {
        return new ChunkLocation(readInt(), readInt(), readInt());
    }

    public GameProfile readProfile() {
        boolean hasName = readBoolean();
        return new GameProfile(readUniqueId(), hasName ? readString(Short.MAX_VALUE) : null);
    }

    public void writeProfile(GameProfile profile) {
        boolean hasName = profile.getName() != null;
        writeBoolean(hasName);
        if (hasName) {
            writeUniqueId(profile.getId());
            writeString(profile.getName());
        } else {
            writeUniqueId(profile.getId());
        }
    }
}
