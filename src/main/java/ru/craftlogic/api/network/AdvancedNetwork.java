package ru.craftlogic.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.Dimension;

public class AdvancedNetwork {
    private final String channel;
    private SimpleNetworkWrapper net;
    private int packetId;

    public AdvancedNetwork(String channel) {
        this.channel = channel;
    }

    public void openChannel() {
        this.net = NetworkRegistry.INSTANCE.newSimpleChannel(channel);
    }

    public <IN extends AdvancedMessage, OUT extends AdvancedMessage>
    void registerMessage(IMessageHandler<? super IN, ? extends OUT> handler, Class<IN> type, Side side) {
        net.registerMessage(handler, type, packetId++, side);
    }

    public void sendTo(EntityPlayer target, IMessage message) {
        sendTo((EntityPlayerMP)target, message);
    }

    public void sendTo(EntityPlayerMP target, IMessage message) {
        net.sendTo(message, target);
    }

    @SideOnly(Side.CLIENT)
    public void sendToServer(IMessage message) {
        net.sendToServer(message);
    }

    public void broadcast(IMessage message) {
        net.sendToAll(message);
    }

    public void broadcastInWorld(Dimension dimension, IMessage message) {
        net.sendToDimension(message, dimension.getVanilla().getId());
    }
}
