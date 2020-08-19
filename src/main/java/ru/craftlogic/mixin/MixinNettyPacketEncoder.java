package ru.craftlogic.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(NettyPacketEncoder.class)
public class MixinNettyPacketEncoder {
    @Shadow @Final private EnumPacketDirection direction;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private static Marker RECEIVED_PACKET_MARKER;

    /**
     * @author Radviger
     * @reason Unregistered packet debug
     */
    @Overwrite
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf buf) throws IOException, Exception {
        EnumConnectionState enumconnectionstate = ctx.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get();
        if (enumconnectionstate == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet.toString());
        } else {
            Integer id = enumconnectionstate.getPacketId(direction, packet);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", ctx.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), id, packet.getClass().getName());
            }

            if (id == null) {
                throw new IOException("Can't serialize unregistered packet: " + packet.getClass().getName());
            } else {
                PacketBuffer buffer = new PacketBuffer(buf);
                buffer.writeVarInt(id);
                packet.writePacketData(buffer);
            }
        }
    }
}
