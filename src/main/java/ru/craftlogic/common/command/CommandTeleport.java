package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.Player;

public final class CommandTeleport extends CommandBase {
    CommandTeleport() {
        super("tp", 2, "<target:Player>",
            "<x:XCoord> <y:YCoord> <z:ZCoord>",
            "<whom:Player> <target:Player>",
            "<whom:Player> <x:XCoord> <y:YCoord> <z:ZCoord>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        boolean hasTarget = ctx.has("target");
        if (ctx.has("whom")) {
            Player whom = ctx.get("whom").asPlayer();
            if (hasTarget) {
                Player target = ctx.get("target").asPlayer();
                if (whom.isOnline() && target.isOnline()) {
                    whom.teleport(target.getLocation());
                }
            } else {
                teleportByCoord(whom, ctx);
            }
        } else {
            Player sender = ctx.senderAsPlayer();
            if (hasTarget) {
                Player target = ctx.get("target").asPlayer();
                if (sender.isOnline() && target.isOnline()) {
                    sender.teleport(target.getLocation());
                }
            } else {
                teleportByCoord(sender, ctx);
            }
        }
    }

    public void teleportByCoord(Player player, CommandContext ctx) throws CommandException {
        Location location = player.getLocation();
        double x = ctx.get("x").asCoord(location.getX());
        double y = ctx.get("y").asCoord(location.getY());
        double z = ctx.get("z").asCoord(location.getZ());
        player.teleport(new Location(location.getWorld(), x, y, z));
    }
}




