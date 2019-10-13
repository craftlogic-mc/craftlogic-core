package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.OfflinePlayer;

public final class CommandOp extends CommandBase {
    CommandOp() {
        super("op", 4,
            "<username:OfflinePlayer>",
            "<username:OfflinePlayer> <level>",
            "<username:OfflinePlayer> <level> <bypassPlayerLimit>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer target = ctx.get("username").asOfflinePlayer();
        int level = ctx.getIfPresent("level", a -> a.asInt(1, 4)).orElse(1);
        boolean bypassPlayerLimit = ctx.getIfPresent("bypassPlayerLimit", CommandContext.Argument::asBoolean).orElse(false);

        boolean success = target.setOperator(true, level, bypassPlayerLimit);
        ctx.sendNotification("commands.op." + (success ? "success" : "failed"), target.getDisplayName());
    }
}
