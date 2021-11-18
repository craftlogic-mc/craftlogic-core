package ru.craftlogic.common.command;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.Location;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CommandManager extends ConfigurableManager {
    public static final Logger LOGGER = LogManager.getLogger("CommandManager");

    private final CommandHandler commandHandler;
    private Map<String, Map<String, CommandContainer>> commands = new HashMap<>();
    private Map<String, ArgType> completers = new HashMap<>();
    private boolean loaded;

    public CommandManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("commands.json"), LOGGER);
        commandHandler = (CommandHandler) server.unwrap().commandManager;
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerArgumentType("TimeUnit", false, ctx -> Arrays.asList(
            "d", "day", "days", "h", "hour", "hours", "m", "minute", "minutes"
        ));
        commandManager.registerArgumentType("GameMode", false, ctx ->
            Stream.of(GameType.values()).map(GameType::getName).collect(Collectors.toList())
        );
        commandManager.registerArgumentType("World", false, ctx ->
            ctx.server().getWorldManager().getAllLoadedNames()
        );
        commandManager.registerArgumentType("Dimension", false, cxt ->
            Stream.of(DimensionType.values()).map(DimensionType::getName).collect(Collectors.toSet())
        );
        commandManager.registerArgumentType("Player", true, ctx ->
            ctx.server().getPlayerManager().getAllOnlineNames()
        );
        commandManager.registerArgumentType("OfflinePlayer", true, ctx ->
            ctx.server().getPlayerManager().getAllOfflineNames()
        );
        commandManager.registerArgumentType(new String[]{"XCoord", "YCoord", "ZCoord"}, false, ctx -> {
            if (ctx.partialName().isEmpty()) {
                Location location = ctx.targetBlockOrSelfLocation();
                int coord = -1;
                switch (ctx.type().substring(0, 1)) {
                    case "X":
                        coord = location.getBlockX();
                        break;
                    case "Y":
                        coord = location.getBlockY();
                        break;
                    case "Z":
                        coord = location.getBlockZ();
                        break;
                }
                return singletonList(String.valueOf(coord));
            }
            return emptyList();
        });

        commandManager.registerCommand(new CommandWorldSpawn());
        commandManager.registerCommand(new CommandHome());
        commandManager.registerCommand(new CommandGameMode());
        commandManager.registerCommand(new CommandFly());
        commandManager.registerCommand(new CommandGod());
        commandManager.registerCommand(new CommandHeal());
        commandManager.registerCommand(new CommandFeed());
        commandManager.registerCommand(new CommandCraft());
        commandManager.registerCommand(new CommandInventory());
        commandManager.registerCommand(new CommandEnderchest());
        commandManager.registerCommand(new CommandTime());
        commandManager.registerCommand(new CommandRequestTeleport());

        if (server.isDedicated()) {
            commandManager.registerCommand(new CommandInfo());
            commandManager.registerCommand(new CommandStop());
            commandManager.registerCommand(new CommandOp());
            commandManager.registerCommand(new CommandDeop());
            commandManager.registerCommand(new CommandOps());
            commandManager.registerCommand(new CommandSeen());
            commandManager.registerCommand(new CommandBan());
            commandManager.registerCommand(new CommandBanIp());
            commandManager.registerCommand(new CommandTempBan());
            commandManager.registerCommand(new CommandTempBanIp());
        }
    }

    public ArgType getCompleter(String type) {
        return completers.get(type);
    }

    @Override
    protected void load(JsonObject config) {
        for (Map<String, CommandContainer> entry : this.commands.values()) {
            for (Map.Entry<String, CommandContainer> e : entry.entrySet()) {
                CommandContainer container = e.getValue();
                commandHandler.registerCommand(container.command);
            }
        }
        loaded = true;
    }

    @Override
    protected void save(JsonObject config) {

    }

    public ICommand registerCommand(ICommand cmd) {
        String modId = CraftAPI.getActiveModId();
        commands.computeIfAbsent(modId, k -> new HashMap<>())
            .put(cmd.getName(), new CommandContainer(modId, cmd));
        if (loaded) {
            commandHandler.registerCommand(cmd);
        }

        return cmd;
    }

    public void registerArgumentType(String type, boolean isEntityName, CheckedFunction<ArgCompletionContext, Collection<String>, Throwable> completer) {
        this.completers.put(type, new ArgType(type, isEntityName, completer));
    }

    public void registerArgumentType(String[] types, boolean isEntityName, CheckedFunction<ArgCompletionContext, Collection<String>, Throwable> completer) {
        for (String type : types) {
            registerArgumentType(type, isEntityName, completer);
        }
    }

    public ICommand registerCommand(String name, List<CommandBase.Syntax> syntax, List<String> aliases, int opLevel, CommandExecutor executor) {
        return registerCommand(new CommandExtended(
            name,
            syntax,
            aliases,
            opLevel,
            executor
        ));
    }

    public boolean unregisterCommand(ICommand command) {
        return ((AdvancedCommandManager)commandHandler).unregisterCommand(command);
    }

    public static class ArgType implements CheckedFunction<ArgCompletionContext, Collection<String>, Throwable> {
        public final String type;
        public final boolean isEntityName;
        private final CheckedFunction<ArgCompletionContext, Collection<String>, Throwable> completer;

        public ArgType(String type, boolean isEntityName, CheckedFunction<ArgCompletionContext, Collection<String>, Throwable> completer) {
            this.type = type;
            this.isEntityName = isEntityName;
            this.completer = completer;
        }

        @Override
        public Collection<String> apply(ArgCompletionContext context) throws Throwable {
            return completer.apply(context);
        }
    }

    public static class CommandContainer {
        public final String modId;
        public final ICommand command;

        public CommandContainer(String modId, ICommand command) {
            this.modId = modId;
            this.command = command;
        }

        public boolean checkPermission(MinecraftServer server, ICommandSender sender, String[] args, boolean partial) throws CommandException {
            if (command instanceof CommandBase) {
                return ((CommandBase) command).checkPermission(server, sender, args, partial);
            } else {
                return command.checkPermission(server, sender);
            }
        }
    }

    public static final class CommandExtended extends CommandBase {
        private final CommandExecutor executor;

        public CommandExtended(String name, List<Syntax> syntax, List<String> aliases, int opLevel, CommandExecutor executor) {
            super(name, opLevel, syntax);
            this.aliases.addAll(aliases);
            this.executor = executor;
        }

        @Override
        protected void execute(CommandContext context) throws Throwable {
            executor.execute(context);
        }
    }
}
