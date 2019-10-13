package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.Set;

public final class CommandOps extends CommandBase {
    CommandOps() {
        super("ops", 4,
            "",
            "<level>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        int level = ctx.getIfPresent("level", a -> a.asInt(1, 4)).orElse(1);

        Set<OfflinePlayer> operators = ctx.server().getPlayerManager().getOperators(level);

        if (operators.isEmpty()) {
            if (level != 1) {
                throw new CommandException("commands.ops.not_found", level);
            } else {
                throw new CommandException("commands.ops.empty");
            }
        }

        ctx.sendMessage("commands.ops.header");

        for (OfflinePlayer operator : operators) {
            boolean b = operator.isBypassesPlayerLimit();
            ITextComponent displayName = operator.getDisplayName();

            ctx.sendMessage(
                Text.translation("commands.ops.entry")
                    .arg(displayName, a -> a.gold().runCommand("/deop " + operator.getName()))
                    .arg(operator.getOperatorLevel())
                    .argTranslate("commands.generic." + (b ? "yes" : "no"), b ? Text::green : Text::red)
            );
        }
    }
}
