package ru.craftlogic.api.command;

import ru.craftlogic.api.util.CheckedConsumer;

@FunctionalInterface
public interface CommandExecutor extends CheckedConsumer<CommandContext, Throwable> {
    default void execute(CommandContext ctx) throws Throwable {
        this.accept(ctx);
    }
}
