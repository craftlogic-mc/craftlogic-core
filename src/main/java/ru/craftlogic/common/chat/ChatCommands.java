package ru.craftlogic.common.chat;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.Command;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.command.CommandRegistrar;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.network.message.MessageClearChat;

import java.io.IOException;

import static ru.craftlogic.common.command.ManagementCommands.parseDuration;

public class ChatCommands implements CommandRegistrar {
    @Command(name = "mute", syntax = {
        "<target:Player> <duration>",
        "<target:Player> <duration> <reason>"
    }, serverOnly = true)
    public static void commandMute(CommandContext ctx) throws CommandException {
        ChatManager chatManager = ctx.server().getChatManager();
        if (!chatManager.isEnabled()) {
            throw new CommandException("commands.chat.disabled");
        }

        Player target = ctx.get("target").asPlayer();
        long duration = parseDuration(ctx.get("duration").asString());
        String reason = ctx.getIfPresent("reason", Argument::asString).orElse(null);

        if (chatManager.getMute(target) != null) {
            throw new CommandException("commands.mute.already", target.getName());
        }
        chatManager.addMute(target, System.currentTimeMillis() + duration, reason);

        Text<?, ?> d = CraftMessages.parseDuration(duration).darkRed();

        if (reason != null) {
            ctx.sendNotification(
                Text.translation("commands.mute.target.reason").gray()
                    .arg(target.getName(), Text::darkGray)
                    .arg(d.darkGray())
                    .arg(reason, Text::darkGray)
            );
            target.sendMessage(
                Text.translation("commands.mute.you.reason").red()
                    .arg(d.darkRed())
                    .arg(reason, Text::red)
            );
        } else {
            ctx.sendNotification(
                Text.translation("commands.mute.target").gray()
                    .arg(target.getName(), Text::darkGray)
                    .arg(d.darkGray())
            );
            target.sendMessage(
                Text.translation("commands.mute.you").red()
                    .arg(d.darkRed())
            );
        }
    }

    @Command(name = "unmute", syntax = "<target:Player>", serverOnly = true)
    public static void commandUnmute(CommandContext ctx) throws CommandException {
        ChatManager chatManager = ctx.server().getChatManager();
        if (!chatManager.isEnabled()) {
            throw new CommandException("commands.chat.disabled");
        }

        Player player = ctx.get("target").asPlayer();

        if (chatManager.removeMute(player)) {
            ctx.sendNotification(
                Text.translation("commands.unmute.success").gray()
                    .arg(player.getName(), Text::darkGray)
            );
        } else {
            throw new CommandException("commands.unmute.already", player.getName());
        }
    }

    @Command(name = "chat", syntax = {
        "clear all",
        "clear",
        "reload"
    })
    public static void commandChat(CommandContext ctx) throws CommandException {
        ChatManager chatManager = ctx.server().getChatManager();
        if (!chatManager.isEnabled()) {
            throw new CommandException("commands.chat.disabled");
        }

        switch (ctx.constant()) {
            case "clear": {
                ctx.server().broadcastPacket(new MessageClearChat(ctx.hasConstant(1)));
                ctx.sendNotification(
                    Text.translation("commands.chat.clear").gray()
                );
                break;
            }
            case "reload": {
                try {
                    chatManager.load();
                    ctx.sendNotification(
                        Text.translation("commands.chat.reload.success").gray()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CommandException("commands.chat.reload.failed", e.getMessage());
                }
                break;
            }
        }
    }
}
