package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.StringUtils;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextWrapped;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ManagementCommands implements CommandRegisterer {
    @Command(name = "op", syntax = {
        "<username:CachedUsername>",
        "<username:CachedUsername> <level>",
        "<username:CachedUsername> <level> <bypassPlayerLimit>"
    }, serverOnly = true)
    public static void commandOp(CommandContext ctx) throws CommandException {
        Argument username = ctx.get("username");
        OfflinePlayer target = username.asOfflinePlayer();
        int level = 0;
        boolean bypassPlayerLimit = false;
        if (ctx.has("level")) {
            level = ctx.get("level").asInt(0, 4);
        }
        if (ctx.has("bypassPlayerLimit")) {
            bypassPlayerLimit = ctx.get("bypassPlayerLimit").asBoolean();
        }
        if (target.setOperator(true, level, bypassPlayerLimit)) {
            ctx.sendNotification("commands.op.success", target.getDisplayName());
        } else {
            ctx.sendMessage("commands.op.failed", target.getDisplayName());
        }
    }

    @Command(name = "deop", syntax = "<username:CachedUsername>", serverOnly = true)
    public static void commandDeOp(CommandContext ctx) throws CommandException {
        Argument username = ctx.get("username");
        OfflinePlayer target = username.asOfflinePlayer();
        if (target.setOperator(false, -1, false)) {
            ctx.sendNotification("commands.deop.success", target.getDisplayName());
        } else {
            ctx.sendMessage("commands.deop.failed", target.getDisplayName());
        }
    }

    @Command(name = "ops", serverOnly = true)
    public static void commandOpList(CommandContext ctx) {
        Set<OfflinePlayer> operators = ctx.server().getOperators();
        ctx.sendMessage("commands.ops.header");
        for (OfflinePlayer operator : operators) {
            boolean b = operator.isBypassesPlayerLimit();
            ITextComponent displayName = operator.getDisplayName();

            ctx.sendMessage(new TextWrapped(displayName)
                .appendTranslate("-> level: ", t -> t.arg(operator.getPermissionLevel()))
                .appendTranslate(", can bypass player limit: ", t ->
                    t.argText(b ? "yes" : "no", b ? Text::green : Text::red)
                )
            );
        }
    }

    @ArgumentCompleter(type = "CachedUsername", isEntityName = true)
    public static List<String> completerUsername(ArgumentCompletionContext ctx) {
        String[] usernames = ctx.server().getProfileCache().getUsernames();
        List<String> variants = ctx.partialName().isEmpty() ? new ArrayList<>(usernames.length) : new ArrayList<>();
        for (String username : usernames) {
            if (StringUtils.startsWithIgnoreCase(username, ctx.partialName())) {
                variants.add(username);
            }
        }
        return variants;
    }
}
