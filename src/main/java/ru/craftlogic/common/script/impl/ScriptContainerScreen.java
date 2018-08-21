package ru.craftlogic.common.script.impl;

import com.google.gson.JsonObject;
import groovy.lang.Closure;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScriptContainerScreen extends ScriptContainer {
    public Closure<Void> closed, tick, init, drawBackground, drawForeground;
    Map<String, Closure<NBTTagCompound>> payloadHandler = new HashMap<>();

    public ScriptContainerScreen(String id, JsonObject info, ScriptScreen script) {
        super(id, info, script);
    }

    public NBTTagCompound handlePayload(String channel, NBTTagCompound data) {
        Closure<NBTTagCompound> handler = this.payloadHandler.get(channel);
        if (handler != null) {
            handler.setDelegate(data);
            NBTTagCompound response = handler.call(data);
            handler.setDelegate(null);
            return response;
        }
        return null;
    }
}
