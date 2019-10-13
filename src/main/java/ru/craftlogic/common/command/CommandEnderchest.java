package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.util.WrappedPlayerEnderchest;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

public final class CommandEnderchest extends CommandBase {
    CommandEnderchest() {
        super("inventory", 2, "<target:Player>", "");
        this.aliases.add("ec");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : viewer;
        World requesterWorld = viewer.getWorld();
        if (target.hasData(requesterWorld)) {
            viewer.openChestInventory(new WrappedPlayerEnderchest(viewer, target.asPhantom(requesterWorld)));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
        }
    }
}
