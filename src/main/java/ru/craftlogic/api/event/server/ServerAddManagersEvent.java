package ru.craftlogic.api.event.server;

import net.minecraftforge.fml.common.eventhandler.Event;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.util.ServerManager;

import java.nio.file.Path;
import java.util.function.BiFunction;

public class ServerAddManagersEvent extends Event {
    private final Server server;

    public ServerAddManagersEvent(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public <M extends ServerManager> void addManager(Class<? extends M> type, BiFunction<Server, Path, M> factory) {
        this.server.addManager(type, factory);
    }
}
