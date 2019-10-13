package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.OfflinePlayer;

public final class CommandDeop extends CommandBase {
    CommandDeop() {
        super("deop", 4, "<username:OfflinePlayer>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer target = ctx.get("username").asOfflinePlayer();
        boolean success = target.setOperator(false, -1, false);
        ctx.sendNotification("commands.deop." + (success ? "success" : "failed"), target.getDisplayName());
    }
}
