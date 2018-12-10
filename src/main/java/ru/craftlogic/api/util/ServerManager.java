package ru.craftlogic.api.util;

import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.common.command.CommandManager;

public abstract class ServerManager {
    protected final Server server;
    protected final Logger logger;

    protected ServerManager(Server server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public final Server getServer() {
        return server;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    public void load() throws Exception {}

    public void unload() throws Exception {
        this.save();
    }

    public void save() throws Exception {}

    public void registerCommands(CommandManager commandManager) {}
}
