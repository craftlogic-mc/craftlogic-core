package ru.craftlogic.api.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;

public interface AdvancedCommandManager extends ICommandManager {
    boolean unregisterCommand(ICommand command);
}
