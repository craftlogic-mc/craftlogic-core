package ru.craftlogic.common.script.impl;

import groovy.lang.Closure;
import net.minecraft.command.ICommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.common.script.Events;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class ScriptFile extends ScriptBase<ScriptContainerFile> {
    @Override
    protected void showChatMessage(ITextComponent message) {
        CraftAPI.getServer().sendMessage(message);
    }

    @Override
    protected ITextComponent getPrefix() {
        return this.container.getPrefix();
    }

    protected void loaded(Closure<Void> handler) {
        this.container.loadingHandler = handler;
    }

    protected void unloaded(Closure<Void> handler) {
        this.container.unloadingHandler = handler;
    }

    protected void when(String event, Closure<Void> handler) {
        Class<? extends Event> type = Objects.requireNonNull(Events.get(event), "Unknown event type: " + event);
        this.when(type, handler);
    }

    protected void when(Class<? extends Event> event, Closure<Void> handler) {
        this.when(event, EventPriority.NORMAL, handler);
    }

    protected void when(String event, EventPriority priority, Closure<Void> handler) {
        Class<? extends Event> type = Objects.requireNonNull(Events.get(event), "Unknown event type: " + event);
        this.when(type, priority, handler);
    }

    protected void when(Class<? extends Event> event, EventPriority priority, Closure<Void> handler) {
        this.container.when(event, priority, handler);
    }

    protected void command(String name, Closure<Void> handler) {
        this.command(new LinkedHashMap<>(), name, handler);
    }

    protected void command(Map<String, Object> data, String name, Closure<Void> handler) {
        List<String> permissions = parseList(data, "permissions", singletonList("commands." + name));
        List<String> aliases = parseList(data, "aliases", emptyList());
        List<String> syntax = parseList(data, "syntax", singletonList(""));
        if (syntax.isEmpty()) {
            syntax.add("");
        }
        ICommand cmd = this.container.registerCommand(name, syntax, aliases, permissions,
            ctx -> {
                handler.setDelegate(ctx);
                handler.call(ctx);
                handler.setDelegate(null);
            }
        );
        this.container.commands.add(cmd);
    }

    protected void payload(String channel, Closure<NBTTagCompound> callback) {
        this.container.payloadHandler.put(channel, callback);
    }

    private List<String> parseList(Map<String, Object> map, String key, List<String> def) {
        return map.containsKey(key) ? parseList(map.get(key)) : def;
    }

    private List<String> parseList(Object obj) {
        if (obj instanceof String) {
            return singletonList((String)obj);
        } else {
            return (List<String>)obj;
        }
    }
}
