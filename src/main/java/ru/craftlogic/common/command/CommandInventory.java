package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.util.WrappedPlayerInventory;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

public final class CommandInventory extends CommandBase {
    CommandInventory() {
        super("inventory", 2, "<target:Player>");
        this.aliases.add("inv");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.get("target").asOfflinePlayer();
        World requesterWorld = viewer.getWorld();
        if (target.hasData(requesterWorld)) {
            viewer.openChestInventory(new WrappedPlayerInventory(viewer, target.asPhantom(requesterWorld)));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
        }
    }
}
