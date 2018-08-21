package ru.craftlogic.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Dimension;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class CraftNetwork {
    static SimpleNetworkWrapper NET;

    static void init(Side side) {
        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    }

    private static int packetId;

    public static <IN extends AdvancedMessage, OUT extends AdvancedMessage>
    void registerMessage(IMessageHandler<? super IN, ? extends OUT> handler, Class<IN> type, Side side) {
        NET.registerMessage(handler, type, packetId++, side);
    }

    public static void sendTo(EntityPlayer target, IMessage message) {
        sendTo((EntityPlayerMP)target, message);
    }

    public static void sendTo(EntityPlayerMP target, IMessage message) {
        NET.sendTo(message, target);
    }

    @SideOnly(Side.CLIENT)
    public static void sendToServer(IMessage message) {
        NET.sendToServer(message);
    }

    public static void broadcast(IMessage message) {
        NET.sendToAll(message);
    }

    public static void broadcastInWorld(Dimension dimension, IMessage message) {
        NET.sendToDimension(message, dimension.getVanilla().getId());
    }
}
