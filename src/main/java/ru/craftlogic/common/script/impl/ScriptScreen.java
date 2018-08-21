package ru.craftlogic.common.script.impl;

import groovy.lang.Closure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.CraftNetwork;

@SideOnly(Side.CLIENT)
public abstract class ScriptScreen extends ScriptBase<ScriptContainerScreen> {
    protected void tick(Closure<Void> callback) {
        this.container.tick = callback;
    }

    protected void init(Closure<Void> callback) {
        this.container.init = callback;
    }

    protected void closed(Closure<Void> callback) {
        this.container.closed = callback;
    }

    protected void drawBackground(Closure<Void> callback) {
        this.container.drawBackground = callback;
    }

    protected void drawForeground(Closure<Void> callback) {
        this.container.drawForeground = callback;
    }

    protected void payload(String channel, Closure<NBTTagCompound> callback) {
        this.container.payloadHandler.put(channel, callback);
    }

    @Override
    protected void showChatMessage(ITextComponent message) {
        getPlayer().sendMessage(message);
    }

    protected void sendChatMessage(String message) {
        getPlayer().sendChatMessage(message);
    }

    protected void sendPacket(IMessage packet) {
        CraftNetwork.sendToServer(packet);
    }

    private Minecraft getClient() {
        return Minecraft.getMinecraft();
    }

    private EntityPlayerSP getPlayer() {
        return getClient().player;
    }
}
