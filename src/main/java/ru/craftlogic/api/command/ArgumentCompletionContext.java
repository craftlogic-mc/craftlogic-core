package ru.craftlogic.api.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import ru.craftlogic.api.Server;

import javax.annotation.Nullable;

public class ArgumentCompletionContext {
    private final Server server;
    private final ICommandSender sender;
    private final String partialName;
    private final BlockPos targetBlock;

    public ArgumentCompletionContext(Server server, ICommandSender sender, String partialName, @Nullable BlockPos targetBlock) {
        this.server = server;
        this.sender = sender;
        this.partialName = partialName;
        this.targetBlock = targetBlock;
    }

    public Server server() {
        return this.server;
    }

    public ICommandSender sender() {
        return this.sender;
    }

    public String partialName() {
        return this.partialName;
    }

    @Nullable
    public BlockPos targetBlock() {
        return this.targetBlock;
    }
}
