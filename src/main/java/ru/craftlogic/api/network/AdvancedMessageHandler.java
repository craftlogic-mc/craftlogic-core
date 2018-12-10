package ru.craftlogic.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AdvancedMessageHandler {
    protected static <IN extends AdvancedMessage, OUT extends AdvancedMessage>
    IMessageHandler<? super IN, ? extends OUT> synced(IMessageHandler<? super IN, ? extends OUT> handler) {
        return (message, context) -> {
            syncTask(context, () -> handler.onMessage(message, context));
            return null;
        };
    }

    protected static EntityPlayer getPlayer(MessageContext context) {
        return context.side.isClient() ? getClientPlayer() : context.getServerHandler().player;
    }

    protected static MinecraftServer getServer(MessageContext context) {
        return ((AdvancedNetHandlerPlayServer)context.getServerHandler()).getServer();
    }

    protected static void syncTask(MessageContext context, Runnable task) {
        IThreadListener listener = context.side.isClient()
                ? FMLClientHandler.instance().getClient()
                : FMLCommonHandler.instance().getMinecraftServerInstance();
        listener.addScheduledTask(task);
    }

    @SideOnly(Side.CLIENT)
    private static EntityPlayer getClientPlayer() {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }
}
