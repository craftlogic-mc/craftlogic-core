package ru.craftlogic.util;

import net.minecraft.command.ICommandSender;
import ru.craftlogic.api.world.LocatableCommandSender;

public class WrappedCommandSender implements LocatableCommandSender {
    private ICommandSender sender;

    public WrappedCommandSender(ICommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public ICommandSender unwrap() {
        return sender;
    }

    @Override
    public boolean hasPermission(String permission, int opLevel) {
        return opLevel == 0;
    }
}
