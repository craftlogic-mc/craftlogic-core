package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.inventory.InterfaceVirtualWorkbench;

public final class CommandCraft extends CommandBase {
    CommandCraft() {
        super("craft", 1,
            "",
            "<target:Player>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player target = ctx.senderAsPlayerOrArg("target");
        target.openInteraction(new InterfaceVirtualWorkbench(target.getWorld().unwrap()));
    }
}
