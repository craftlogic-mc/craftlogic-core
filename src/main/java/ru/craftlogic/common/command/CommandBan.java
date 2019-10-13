package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListBansEntry;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

import java.util.Date;

public final class CommandBan extends CommandBase {
    CommandBan() {
        super("ban", 3,
            "<username:OfflinePlayer> <reason>...",
            "<username:OfflinePlayer>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        String reason = ctx.getIfPresent("reason", CommandContext.Argument::asString).orElse(null);
        banUser(ctx, player, null, reason);
    }

    static void banUser(CommandContext ctx, OfflinePlayer player, Date expirationDate, String reason) {
        UserListBansEntry bansEntry = new UserListBansEntry(player.getProfile(), null, ctx.server().getName(), expirationDate, reason);
        PlayerList playerList = ctx.server().unwrap().getPlayerList();
        playerList.getBannedPlayers().addEntry(bansEntry);

        if (player.isOnline()) {
            Player p = player.asOnline();
            p.disconnect(CraftMessages.getBanMessage(bansEntry, false));
        }

        ctx.sendNotification(
            Text.translation("commands.ban.success").yellow()
                .arg(player.getName(), Text::gold)
        );
    }
}
