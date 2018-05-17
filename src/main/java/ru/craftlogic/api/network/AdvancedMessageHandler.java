package ru.craftlogic.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AdvancedMessageHandler<IN extends IMessage, OUT extends IMessage>
        implements IMessageHandler<IN, OUT> {

    @Override
    public final OUT onMessage(IN message, MessageContext context) {
        return this.handle(message, context);
    }

    protected abstract OUT handle(IN message, MessageContext context);

    protected EntityPlayer getPlayer(MessageContext context) {
        return context.side.isClient() ? getClientPlayer() : context.getServerHandler().player;
    }

    protected void scheduleTask(MessageContext context, Runnable task) {
        IThreadListener listener = context.side.isClient()
                ? FMLClientHandler.instance().getClient()
                : FMLCommonHandler.instance().getMinecraftServerInstance();
        listener.addScheduledTask(task);
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }
}
