package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.WorldManager;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.*;

public final class CommandSeen extends CommandBase {
    CommandSeen() {
        super("seen", 1, "<username:OfflinePlayer>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        if (player.isOnline()) {
            Text<?, ?> coordinates = CraftMessages.parseCoordinates(player.asOnline().getLocation());
            ctx.sendMessage(
                Text.translation("commands.seen.online").yellow()
                    .arg(player.getName(), Text::gold)
                    .arg(coordinates.gold())
            );
        } else {
            WorldManager worldManager = ctx.server().getWorldManager();
            World world = ctx.sender() instanceof LocatableCommandSender ? ctx.senderAsLocatable().getWorld() : worldManager.get(Dimension.OVERWORLD);
            PhantomPlayer fakePlayer = player.asPhantom(world);
            Location lastLocation = fakePlayer.getLocation();
            long lastPlayed = fakePlayer.getLastPlayed();
            if (lastPlayed != 0 && lastLocation != null) {
                Text<?, ?> coordinates = CraftMessages.parseCoordinates(lastLocation);
                Text<?, ?> time = CraftMessages.parseDuration(System.currentTimeMillis() - lastPlayed);
                ctx.sendMessage(
                    Text.translation("commands.seen.offline").yellow()
                        .arg(fakePlayer.getName(), Text::gold)
                        .arg(coordinates.gold())
                        .arg(time.gold())
                );
            } else {
                throw new CommandException("commands.inventory.no_data", player.getName());
            }
        }
    }
}
