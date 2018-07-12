package ru.craftlogic.common.script.impl;

import groovy.lang.Closure;
import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import ru.craftlogic.api.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public abstract class ScriptFile extends ScriptBase {

    @Override
    public void print(Object value) {
        this.container.print(value);
    }

    protected void loaded(Closure<Void> handler) {
        this.container.loadingHandler = handler;
    }

    protected void unloaded(Closure<Void> handler) {
        this.container.unloadingHandler = handler;
    }

    protected void when(String event, Closure<Void> handler) {
        this.when(event, EventPriority.NORMAL, handler);
    }

    protected void when(String event, EventPriority priority, Closure<Void> handler) {
        this.container.eventHandlers.put(event, Pair.of(priority, handler));
    }

    protected void command(String name, Closure<Void> handler) {
        this.command(new LinkedHashMap<>(), name, handler);
    }

    protected void command(Map<String, List<String>> data, String name, Closure<Void> handler) {
        List<String> permissions = data.getOrDefault("permissions", singletonList("commands." + name));
        List<String> aliases = data.getOrDefault("aliases", new ArrayList<>());
        List<String> syntax = data.getOrDefault("syntax", singletonList(""));
        if (syntax.isEmpty()) {
            syntax.add("");
        }
        ICommand cmd = this.container.registerCommand(name, syntax, aliases, permissions,
            ctx -> {
                handler.setDelegate(ctx);
                handler.call();
                handler.setDelegate(null);
            }
        );
        this.container.commands.add(cmd);
    }
}
