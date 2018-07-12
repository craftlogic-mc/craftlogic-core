package ru.craftlogic.common.script.extension;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.Player;

public class CommandSenderExtension {
    public static <T> T asType(CommandSender sender, Class<T> type) throws Exception {
        if (type == Player.class) {
            if (sender instanceof Player) {
                return (T) sender;
            } else {
                throw new CommandException("commands.generic.playerOnly");
            }
        }
        return type.cast(sender);
    }
}
