package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandRequestTeleport extends CommandBase {
    CommandRequestTeleport() {
        super("tpa", 0, "<target:Player>");
        aliases.add("call");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        Player target = ctx.get("target").asPlayer();
        if (sender == target) {
            throw new CommandException("commands.request_teleport.self");
        }
        if (sender.getWorld().getDimension() != target.getWorld().getDimension()) {
            throw new CommandException("commands.tp.notSameDimension");
        }
        if (target.hasQuestion("tpa")) {
            throw new CommandException("commands.request_teleport.pending", target.getName());
        } else {
            Text<?, ?> title = Text.translation("commands.request_teleport.question").arg(sender.getName());
            target.sendToastQuestion("tpa", title, 0x404040, 30, accepted -> {
                if (sender.isOnline() && target.isOnline()) {
                    if (accepted) {
                        if (sender.getWorld().getDimension() != target.getWorld().getDimension()) {
                            sender.sendMessage(Text.translation("commands.tp.notSameDimension").red());
                        } else {
                            Text<?, ?> message = Text.translation("commands.request_teleport.accepted").green();
                            sender.sendMessage(message);
                            target.sendMessage(message);
                            Text<?, ?> toast = Text.translation("tooltip.request_teleport").arg(target.getName());
                            sender.teleportDelayed(server -> {}, "tpa", toast, target.getLocation(), 5, true);
                        }
                    } else {
                        Text<?, ?> message = Text.translation("commands.request_teleport.declined").red();
                        sender.sendMessage(message);
                        target.sendMessage(message);
                    }
                }
            });
        }
    }
}
