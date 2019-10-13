package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListIPBansEntry;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CommandBanIp extends CommandBase {
    CommandBanIp() {
        super("ban-ip", 4,
            "<address> <reason>...",
            "<address>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        String address = ctx.get("address").asIP();
        String reason = ctx.getIfPresent("reason", CommandContext.Argument::asString).orElse(null);
        banIP(ctx, address, null, reason);
    }

    static void banIP(CommandContext ctx, String address, Date expirationDate, String reason) {
        UserListIPBansEntry bansEntry = new UserListIPBansEntry(address, null, ctx.server().getName(), expirationDate, reason);

        PlayerList playerList = ctx.server().unwrap().getPlayerList();
        playerList.getBannedIPs().addEntry(bansEntry);

        List<String> users = new ArrayList<>();

        for (Player player : ctx.server().getPlayerManager().getAllOnline()) {
            if (address.equals(player.getIP())) {
                users.add(player.getName());
                player.disconnect(CraftMessages.getBanMessage(bansEntry, true));
            }
        }

        if (users.isEmpty()) {
            ctx.sendNotification(
                Text.translation("commands.banip.success").yellow()
                    .arg(address, Text::gold)
            );
        } else {
            ctx.sendNotification(
                Text.translation("commands.banip.success.players").yellow()
                    .arg(address, Text::gold)
                    .arg(String.join(", ", users), Text::gold)
            );
        }
    }
}
