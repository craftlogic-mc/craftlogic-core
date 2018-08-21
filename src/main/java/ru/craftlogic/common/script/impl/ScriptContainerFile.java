package ru.craftlogic.common.script.impl;

import com.google.gson.JsonObject;
import groovy.lang.Closure;
import net.minecraft.command.ICommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import ru.craftlogic.api.command.CommandExecutor;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.util.Pair;
import ru.craftlogic.common.script.ScriptManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptContainerFile extends ScriptContainer {
    protected final ScriptManager manager;
    Closure<Void> loadingHandler, unloadingHandler;
    Map<String, Closure<NBTTagCompound>> payloadHandler = new HashMap<>();
    List<ICommand> commands = new ArrayList<>();
    private Map<Class<? extends Event>, Pair<EventPriority, IEventListener>> listeners = new HashMap<>();

    public ScriptContainerFile(ScriptManager manager, String id, JsonObject info, ScriptFile script) {
        super(id, info, script);
        this.manager = manager;
    }

    public void load() {
        if (this.loadingHandler != null) {
            this.loadingHandler.call();
        }
    }

    public void unload() {
        try {
            int busID = (int) F_busID.get(MinecraftForge.EVENT_BUS);
            for (Pair<EventPriority, IEventListener> pair : this.listeners.values()) {
                ListenerList.unregisterAll(busID, pair.second());
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        this.listeners.clear();
        if (this.unloadingHandler != null) {
            this.unloadingHandler.call();
        }
        for (ICommand cmd : this.commands) {
            this.manager.getServer().getCommandManager().unregisterCommand(cmd);
        }
    }

    public void when(Class<? extends Event> eventType, EventPriority priority, Closure<Void> handler) {
        try {
            int busID = (int) F_busID.get(MinecraftForge.EVENT_BUS);
            Constructor<? extends Event> constructor = eventType.getConstructor();
            constructor.setAccessible(true);
            Event event = constructor.newInstance();
            IEventListener listener = e -> {
                handler.setDelegate(e);
                handler.call(e);
                handler.setDelegate(null);
            };
            event.getListenerList().register(busID, priority, listener);
            this.listeners.put(eventType, Pair.of(priority, listener));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public ScriptManager getManager() {
        return manager;
    }

    public ICommand registerCommand(String name, List<String> syntax, List<String> aliases, List<String> permissions, CommandExecutor executor) {
        Server server = this.manager.getServer();
        return server.getCommandManager().registerCommand(name, syntax, aliases, permissions, executor);
    }

    private static Field F_busID;

    static {
        try {
            F_busID = EventBus.class.getDeclaredField("busID");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        F_busID.setAccessible(true);
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
