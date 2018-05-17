package ru.craftlogic.api.command;

import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;

public class ArgumentCompletionContext {
    private final Server server;
    private final String type;
    private final CommandSender sender;
    private final String partialName;
    private final Location targetBlock;

    public ArgumentCompletionContext(Server server, String type, CommandSender sender, String partialName, @Nullable Location targetBlock) {
        this.server = server;
        this.type = type;
        this.sender = sender;
        this.partialName = partialName;
        this.targetBlock = targetBlock;
    }

    public Server server() {
        return this.server;
    }

    public String type() {
        return this.type;
    }

    public CommandSender sender() {
        return this.sender;
    }

    public String partialName() {
        return this.partialName;
    }

    @Nullable
    public Location targetBlock() {
        return this.targetBlock;
    }

    public Location targetBlockOrSelfLocation() {
        return this.targetBlock() != null ? this.targetBlock() : this.sender().getLocation();
    }
}
