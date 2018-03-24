package ru.craftlogic.common.script;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.*;

import java.util.ArrayList;
import java.util.List;

public class ScriptCommands implements CommandContainer {
    @Command(
        name = "script",
        syntax = {
            "list",
            "load <id>...",
            "unload <id:ScriptId>..."
        }
    )
    public static void commandScript(CommandContext ctx) throws CommandException {
        ScriptManager scriptManager = ctx.server().getScriptManager();
        if (!scriptManager.isEnabled()) {
            throw new CommandException("Scripting system disabled!");
        }
        switch (ctx.constant()) {
            case "list":
                ctx.sendMessage("Loaded scripts: %s", scriptManager.getAllLoadedScripts());
                break;
            case "load": {
                String id = ctx.get("id").asString();
                boolean reload = scriptManager.isLoaded(id);
                try {
                    ScriptContainer container = scriptManager.loadScript(id, reload, true);
                    if (container != null) {
                        ctx.sendMessage("Successfully loaded script '%s'", id);
                    } else {
                        ctx.sendMessage("Script loading failed");
                    }
                } catch (Exception e) {
                    ctx.sendMessage("Error loading script '%s': %s. See console for more details.", id, e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
            case "unload": {
                String id = ctx.get("id").asString();
                try {
                    if (scriptManager.unloadScript(id)) {
                        ctx.sendMessage("Successfully unloaded script '%s'", id);
                    } else {
                        ctx.sendMessage("Script unloading failed");
                    }
                } catch (Exception e) {
                    ctx.sendMessage("Error unloading script '%s': %s. See console for more details.", id, e.getMessage());
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
            throw new CommandException("Scripting system disabled!");
        }
        Object result = scriptManager.getShell().evaluate(ctx.get("value").asString());
        if (result != null) {
            ctx.sendMessage("[S>]: ", result);
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
