package ru.craftlogic.common.script;

import groovy.lang.Closure;
import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import ru.craftlogic.api.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Script extends groovy.lang.Script {
    private ScriptContainer container;

    void setContainer(ScriptContainer container) {
        this.container = container;
    }

    @Override
    public void println() {
        this.container.println();
    }

    @Override
    public void print(Object value) {
        this.container.print(value);
    }

    @Override
    public void println(Object value) {
        this.container.println(value);
    }

    @Override
    public void printf(String format, Object value) {
        this.container.printf(format, value);
    }

    @Override
    public void printf(String format, Object[] values) {
        super.printf(format, values);
    }

    public void loaded(Closure<Void> handler) {
        this.container.loadingHandler = handler;
    }

    public void unloaded(Closure<Void> handler) {
        this.container.unloadingHandler = handler;
    }

    public void when(String event, Closure<Void> handler) {
        this.when(event, EventPriority.NORMAL, handler);
    }

    public void when(String event, EventPriority priority, Closure<Void> handler) {
        this.container.eventHandlers.put(event, Pair.of(priority, handler));
    }

    public void command(Map<String, Object> data, String name, Closure<Void> handler) {
        List<String> permissions = (List<String>) data.getOrDefault("permissions", Collections.singletonList("commands." + name));
        List<String> aliases = (List<String>)data.getOrDefault("aliases", new ArrayList<>());
        List<String> syntax = (List<String>)data.getOrDefault("syntax", Collections.singletonList(""));
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
