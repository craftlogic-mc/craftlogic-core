package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.Date;

import static ru.craftlogic.api.command.CommandContext.parseDuration;
import static ru.craftlogic.common.command.CommandBan.banUser;

public final class CommandTempBan extends CommandBase {
    CommandTempBan() {
        super("tempban", 3,
            "<username:OfflinePlayer> <time> <reason>...",
            "<username:OfflinePlayer> <time>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        Date expirationDate = new Date(System.currentTimeMillis() + parseDuration(ctx.get("time").asString()));
        String reason = ctx.getIfPresent("reason", CommandContext.Argument::asString).orElse(null);
        banUser(ctx, player, expirationDate, reason);
    }
}
