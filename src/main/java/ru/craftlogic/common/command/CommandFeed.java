package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.init.SoundEvents;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandFeed extends CommandBase {
    CommandFeed() {
        super("feed", 1,
            "<target:Player>",
            ""
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player target = ctx.senderAsPlayerOrArg("target");
        if (ctx.sender() != target) {
            ctx.checkPermission(true, "commands.feed.other", 2);
        }
        if (target.feed()) {
            target.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1F, ctx.randomFloat(0.7F));
            if (ctx.sender() == target) {
                ctx.sendNotification(
                    Text.translation("commands.feed.self").gray()
                );
            } else {
                ctx.sendNotification(
                    Text.translation("commands.feed.other").gray()
                        .arg(target.getName(), Text::darkGray)
                );
            }
        } else {
            if (ctx.sender() == target) {
                throw new CommandException("commands.feed.self.not_hungry");
            } else {
                throw new CommandException("commands.feed.other.not_hungry", target.getName());
            }
        }
    }
}
