package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

import java.util.function.Consumer;

public final class CommandWorldSpawn extends CommandBase {
    CommandWorldSpawn() {
        super("worldspawn", 0, "", "<target:Player>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player target = ctx.has("target") ? ctx.get("target").asPlayer() : ctx.senderAsPlayer();
        Consumer<Server> callback = server -> {
            target.sendMessage(Text.translation("commands.spawn.teleport").green());
        };
        Text<?, ?> message = Text.translation("tooltip.spawn_teleport");
        target.teleportDelayed(callback, "spawn", message, target.getWorld().getSpawnLocation(), 5, true);
    }
}
