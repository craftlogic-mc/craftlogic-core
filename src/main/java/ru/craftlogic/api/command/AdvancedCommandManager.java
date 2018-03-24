package ru.craftlogic.api.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;

public interface AdvancedCommandManager extends ICommandManager {
    ICommand registerCommand(ICommand command);
    boolean unregisterCommand(ICommand command);
}
