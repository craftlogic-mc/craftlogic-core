package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;

import java.util.Date;

import static ru.craftlogic.api.command.CommandContext.parseDuration;
import static ru.craftlogic.common.command.CommandBanIp.banIP;

public final class CommandTempBanIp extends CommandBase {
    CommandTempBanIp() {
        super("tempban-ip", 4,
            "<address> <time> <reason>...",
            "<address> <time>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        String address = ctx.get("address").asIP();
        Date expirationDate = new Date(System.currentTimeMillis() + parseDuration(ctx.get("time").asString()));
        String reason = ctx.getIfPresent("reason", CommandContext.Argument::asString).orElse(null);
        banIP(ctx, address, expirationDate, reason);
    }
}
