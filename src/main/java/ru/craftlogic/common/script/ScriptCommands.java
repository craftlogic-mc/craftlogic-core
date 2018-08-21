package ru.craftlogic.common.script;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import net.minecraft.command.CommandException;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextString;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.script.impl.ScriptContainer;
import ru.craftlogic.common.script.impl.ScriptContainerFile;
import ru.craftlogic.common.script.impl.ScriptShell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptCommands implements CommandRegistrar {
    @Command(
        name = "script",
        syntax = {
            "list",
            "load <id>...",
            "unload <id:ScriptId>..."
        }
    )
    public static void commandScript(CommandContext ctx) throws Exception {
        ScriptManager scriptManager = ctx.server().getScriptManager();
        if (!scriptManager.isEnabled()) {
            throw new CommandException("commands.script.disabled");
        }
        switch (ctx.constant()) {
            case "list": {
                Collection<ScriptContainerFile> loadedScripts = scriptManager.getAllLoadedScripts();
                if (loadedScripts.isEmpty()) {
                    ctx.sendMessage(Text.translation("commands.script.list.empty").gray());
                } else {
                    TextTranslation msg = Text.translation("commands.script.list").yellow();
                    boolean first = true;
                    TextString entries = Text.string();
                    for (ScriptContainerFile script : loadedScripts) {
                        if (first) first = false;
                        else entries.appendText(", ", Text::yellow);
                        String id = script.getId();
                        entries.appendText(script.getName(), arg ->
                            arg.gold().hoverTextTranslate("commands.script.entry.click", a -> a.arg(id + ".gs"))
                                      .runCommand("/script unload " + id)
                        );
                    }
                    ctx.sendMessage(msg.arg(entries));
                }
                break;
            }
            case "load": {
                String id = ctx.get("id").asString();
                boolean reload = scriptManager.isLoaded(id);
                try {
                    ScriptContainer container = scriptManager.loadScript(id, reload, true);
                    if (container != null) {
                        ctx.sendMessage(
                            Text.translation("commands.script.load.success")
                                .arg(id, Text::darkGreen)
                                .green()
                        );
                    } else {
                        ctx.sendMessage(Text.translation("commands.script.unload.failed.generic").red());
                    }
                } catch (Exception e) {
                    ctx.sendMessage(
                        Text.translation("commands.script.unload.failed")
                            .arg(id)
                            .arg(e.getMessage())
                            .red()
                    );
                    throw e;
                }
                break;
            }
            case "unload": {
                String id = ctx.get("id").asString();
                try {
                    if (scriptManager.unloadScript(id)) {
                        ctx.sendMessage(
                            Text.translation("commands.script.unload.success")
                                .green()
                                .arg(id, arg -> arg.darkGreen().suggestCommand("/script load " + id))
                        );
                    } else {
                        ctx.sendMessage("commands.script.unload.fail.generic");
                    }
                } catch (Exception e) {
                    ctx.sendMessage(
                        Text.translation("commands.script.unload.fail")
                            .red()
                            .arg(id, Text::darkRed)
                            .arg(e.getMessage(), null)
                    );
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Command(
        name = "shell>",
        aliases = {"s>", ">"},
        syntax = "<value>..."
    )
    public static void commandShell(CommandContext ctx) throws CommandException {
        ScriptManager scriptManager = ctx.server().getScriptManager();
        if (!scriptManager.isEnabled()) {
            throw new CommandException("commands.script.disabled");
        }
        GroovyShell shell = scriptManager.getShell();
        ScriptShell script;
        try {
            script = (ScriptShell) shell.parse(ctx.get("value").asString(), "@Shell");
        } catch (GroovyRuntimeException exc) {
            String msg = exc.getMessageWithoutLocationText();
            if (msg != null) {
                ctx.sendMessage(msg);
            } else {
                throw exc;
            }
            return;
        }
        Binding binding = script.getBinding();
        binding.setProperty("$server", ctx.server());
        binding.setProperty("$me", ctx.sender());
        for (Player player : ctx.server().getOnlinePlayers()) {
            binding.setProperty("$" + player.getProfile().getName(), player);
        }
        script.setBinding(binding);
        try {
            Object result = script.run();
            ctx.sendMessage("Returned: %s", result);
        } catch (GroovyRuntimeException exc) {
            String msg = exc.getMessageWithoutLocationText();
            if (msg != null) {
                ctx.sendMessage(msg);
            } else {
                throw exc;
            }
        }
    }

    @Command(
        name = "screen",
        syntax = {
            "<id> <player:Player> <args>...",
            "<id> <player:Player>",
            "<id>"
        }
    )
    public static void commandScreen(CommandContext ctx) throws CommandException {
        String id = ctx.get("id").asString();
        String args = ctx.getIfPresent("args", Argument::asString).orElse("");
        Player player = ctx.getIfPresent("player", Argument::asPlayer).orElse(ctx.senderAsPlayer());
        CraftLogic.showScreen(id, player.getEntity(), args);
    }

    @ArgumentCompleter(
        type = "ScriptId"
    )
    public static List<String> completerScriptId(ArgumentCompletionContext ctx) {
        ScriptManager scriptManager = ctx.server().getScriptManager();
        List<String> result = new ArrayList<>();
        for (ScriptContainerFile script : scriptManager.getAllLoadedScripts()) {
            String id = script.getId();
            if (id.startsWith(ctx.partialName())) {
                result.add(id);
            }
        }
        return result;
    }
}
