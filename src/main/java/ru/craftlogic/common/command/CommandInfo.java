package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.WorldManager;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.*;
import ru.craftlogic.network.message.MessagePlayerInfo;

public final class CommandInfo extends CommandBase {
    CommandInfo() {
        super("info", 3, "<target:OfflinePlayer>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        OfflinePlayer target = ctx.get("target").asOfflinePlayer();
        WorldManager worldManager = ctx.server().getWorldManager();
        World world = ctx.sender() instanceof LocatableCommandSender ? ctx.senderAsLocatable().getWorld() : worldManager.get(Dimension.OVERWORLD);
        PhantomPlayer fakePlayer = target.asPhantom(world);
        if (ctx.sender() instanceof Player) {
            ctx.senderAsPlayer().sendPacket(new MessagePlayerInfo(
                target.getProfile(), fakePlayer.getFirstPlayed(), fakePlayer.getLastPlayed(),
                fakePlayer.getTimePlayed(), target.hasPermission("commands.info.edit"), fakePlayer.getLocation(),
                fakePlayer.getBedLocation()
            ));
        } else {
            ctx.sendMessage(Text.string("//TODO"));
        }
    }
}
