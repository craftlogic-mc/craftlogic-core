package ru.craftlogic.common.script;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import groovy.lang.Closure;
import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.command.CommandExecutor;
import ru.craftlogic.api.util.Pair;

import java.util.*;

public class ScriptContainer {
    private final ScriptManager manager;
    private final String id;
    private final String name;
    private final Set<String> authors = new HashSet<>();
    final Script script;
    Closure<Void> loadingHandler, unloadingHandler;
    Map<String, Pair<EventPriority, Closure<Void>>> eventHandlers = new HashMap<>();
    List<ICommand> commands = new ArrayList<>();

    public ScriptContainer(ScriptManager manager, String id, JsonObject info, Script script) {
        this.manager = manager;
        this.id = id;
        this.name = info.has("name") ? info.get("name").getAsString() : id;
        if (info.has("authors")) {
            for (JsonElement e : info.get("authors").getAsJsonArray()) {
                this.authors.add(e.getAsString());
            }
        }
        this.script = script;
        script.setContainer(this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ScriptManager getManager() {
        return manager;
    }

    public Set<String> getAuthors() {
        return ImmutableSet.copyOf(this.authors);
    }

    void println() {
        this.print("\n");
    }

    void println(Object value) {
        this.print(value + "\n");
    }

    void printf(String format, Object... values) {
        this.print(String.format(format, values));
    }

    void print(Object value) {
        System.out.printf("[SCRIPT] %s: %s", this.name, value);
    }

    public void load() {
        if (this.loadingHandler != null) {
            this.loadingHandler.call();
        }
    }

    public void unload() {
        if (this.unloadingHandler != null) {
            this.unloadingHandler.call();
        }
        for (ICommand cmd : this.commands) {
            this.manager.getServer().unregisterCommand(cmd);
        }
    }

    public ICommand registerCommand(String name, List<String> syntax, List<String> aliases, List<String> permissions, CommandExecutor executor) {
        Server server = this.manager.getServer();
        return server.registerCommand(name, syntax, aliases, permissions, executor);
    }
}
