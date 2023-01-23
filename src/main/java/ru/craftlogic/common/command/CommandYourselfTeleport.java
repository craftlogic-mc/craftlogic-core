package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraftforge.common.MinecraftForge;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.event.player.PlayerTeleportReplyEvent;
import ru.craftlogic.api.event.player.PlayerTeleportRequestEvent;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandYourselfTeleport extends CommandBase {
    CommandYourselfTeleport() {
        super("s", 2, "<target:Player>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        Player target = ctx.get("target").asPlayer();
        if (sender == target) {
            throw new CommandException("commands.request_teleport.self");
        }
        if (sender.isOnline() && target.isOnline()) {
            target.teleport(sender.getLocation());
        }
    }
}

