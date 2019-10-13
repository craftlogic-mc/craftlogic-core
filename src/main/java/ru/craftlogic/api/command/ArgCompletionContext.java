package ru.craftlogic.api.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.LocatableCommandSender;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;

public class ArgCompletionContext {
    private final Server server;
    private final String type;
    private final CommandSender sender;
    private final String partialName;
    private final Location targetBlock;

    public ArgCompletionContext(Server server, String type, CommandSender sender, String partialName, @Nullable Location targetBlock) {
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

    public LocatableCommandSender senderAsLocatable() throws CommandException {
        if (this.sender instanceof LocatableCommandSender) {
            return (LocatableCommandSender) this.sender;
        } else {
            throw new CommandException("commands.generic.locatableOnly");
        }
    }

    public Player senderAsPlayer() throws CommandException {
        if (this.sender instanceof Player) {
            return (Player) this.sender;
        } else {
            throw new CommandException("commands.generic.playerOnly");
        }
    }

    public String partialName() {
        return this.partialName;
    }

    @Nullable
    public Location targetBlock() {
        return this.targetBlock;
    }

    public Location targetBlockOrSelfLocation() {
        return this.targetBlock() != null ? this.targetBlock() : ((LocatableCommandSender)this.sender()).getLocation();
    }
}
