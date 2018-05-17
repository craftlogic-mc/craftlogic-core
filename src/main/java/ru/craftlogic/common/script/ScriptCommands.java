package ru.craftlogic.common.script;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import net.minecraft.command.CommandException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.script.impl.ScriptContainer;
import ru.craftlogic.common.script.impl.ScriptShell;

import java.util.ArrayList;
import java.util.List;

public class ScriptCommands implements CommandRegisterer {
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
                ctx.sendMessage("commands.script.list");
                ctx.sendMessage(
                    new TextTranslation("commands.generic.list.entry")
                );
                TextTranslation msg = new TextTranslation("commands.script.list");
                for (String script : scriptManager.getAllLoadedScripts()) {
                    msg.appendTranslate("commands.script.list.entry", sub ->
                        sub.argText(script, arg ->
                            arg.gray().suggestCommand("/script unload " + script)
                        )
                    );
                }
                ctx.sendMessage(msg);
                break;
            }
            case "load": {
                String id = ctx.get("id").asString();
                boolean reload = scriptManager.isLoaded(id);
                try {
                    ScriptContainer container = scriptManager.loadScript(id, reload, true);
                    if (container != null) {
                        ctx.sendMessage(
                            new TextTranslation("commands.script.load.success")
                                .argText(id, Text::darkGreen)
                                .green()
                        );
                    } else {
                        ctx.sendMessage("Script loading failed");
                    }
                } catch (Exception e) {
                    ctx.sendMessage("Error loading script '%s': %s. See console for more details.", id, e.getMessage());
                    throw e;
                }
                break;
            }
            case "unload": {
                String id = ctx.get("id").asString();
                try {
                    if (scriptManager.unloadScript(id)) {
                        ctx.sendMessage(
                            new TextTranslation("commands.script.unload.success")
                                .green()
                                .argText(id, arg -> arg.darkGreen().suggestCommand("/script load " + id))
                        );
                    } else {
                        ctx.sendMessage("commands.script.unload.fail.generic");
                    }
                } catch (Exception e) {
                    ctx.sendMessage(
                        new TextTranslation("commands.script.unload.fail")
                            .red()
                            .argText(id, Text::darkRed)
                            .argText(e.getMessage(), null)
                    );
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Command(
        name = "shell>",
        aliases = "s>",
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
        } catch (MultipleCompilationErrorsException exc) {
            String msg = exc.getMessageWithoutLocationText();
            if (msg != null) {
                ctx.sendMessage(msg);
            } else {
                throw exc;
            }
            return;
        }
        Binding binding = script.getBinding();
        binding.setVariable("server", ctx.server());
        binding.setVariable("me", ctx.sender());
        for (Player player : ctx.server().getOnlinePlayers()) {
            binding.setVariable(player.getProfile().getName(), player);
        }
        script.setBinding(binding);
        try {
            Object result = script.run();
            ctx.sendMessage("[S>]: %s", result);
        } catch (GroovyRuntimeException exc) {
            String msg = exc.getMessageWithoutLocationText();
            if (msg != null) {
                ctx.sendMessage(msg);
            } else {
                throw exc;
            }
        }
    }

    @ArgumentCompleter(
        type = "ScriptId"
    )
    public static List<String> completerScriptId(ArgumentCompletionContext ctx) {
        ScriptManager scriptManager = ctx.server().getScriptManager();
        List<String> result = new ArrayList<>();
        for (String id : scriptManager.getAllLoadedScripts()) {
            if (id.startsWith(ctx.partialName())) {
                result.add(id);
            }
        }
        return result;
    }
}
