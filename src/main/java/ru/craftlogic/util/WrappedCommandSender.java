package ru.craftlogic.util;

import net.minecraft.command.ICommandSender;
import ru.craftlogic.api.world.CommandSender;

public class WrappedCommandSender implements CommandSender {
    private ICommandSender sender;

    public WrappedCommandSender(ICommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public ICommandSender getHandle() {
        return sender;
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        return false;
    }
}
