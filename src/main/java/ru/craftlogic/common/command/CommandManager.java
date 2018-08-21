package ru.craftlogic.common.command;

import com.google.gson.JsonObject;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.permission.PermissionManager;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("CommandManager");

    private final CommandHandler commandHandler;
    private Map<String, Map<String, CommandContainer>> commands = new HashMap<>();
    private Map<String, TypedArgCompleter> completers = new HashMap<>();
    private Map<Class<? extends CommandRegistrar>, List<ICommand>> typedCommands = new HashMap<>();
    private boolean loaded;

    public CommandManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("commands.json"), LOGGER);
        this.commandHandler = (CommandHandler) server.getHandle().commandManager;
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(GameplayCommands.class);
        commandManager.registerCommandContainer(ManagementCommands.class);
    }

    @Override
    protected void load(JsonObject config) {
        for (Map<String, CommandContainer> entry : this.commands.values()) {
            for (Map.Entry<String, CommandContainer> e : entry.entrySet()) {
                CommandContainer container = e.getValue();
                this.commandHandler.registerCommand(container.command);
            }
        }
        this.loaded = true;
    }

    @Override
    protected void save(JsonObject config) {

    }

    public ICommand registerCommand(String name, List<String> syntax, List<String> aliases, List<String> permissions, CommandExecutor executor) {
        String modId = CraftAPI.getActiveModId();

        CommandExtended cmd = new CommandExtended(
            syntax,
            name,
            aliases,
            permissions,
            executor
        );

        this.commands.computeIfAbsent(modId, k -> new HashMap<>())
            .put(name, new CommandContainer(modId, cmd));
        if (this.loaded) {
            this.commandHandler.registerCommand(cmd);
        }

        return cmd;
    }

    public boolean unregisterCommand(ICommand command) {
        return ((AdvancedCommandManager)this.commandHandler).unregisterCommand(command);
    }

    public void registerCommandContainer(Class<? extends CommandRegistrar> objClass) {
        for (Method method : objClass.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) > 0) {
                if (method.isAnnotationPresent(Command.class)) {
                    if (method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == CommandContext.class
                        && method.getReturnType() == void.class) {

                        Command annotation = method.getAnnotation(Command.class);

                        if (annotation.serverOnly() && !this.server.isDedicated()) {
                            continue;
                        }

                        try {
                            MethodHandle mh = MethodHandles.lookup().unreflect(method);
                            ICommand cmd = this.registerCommand(
                                annotation.name(),
                                Arrays.asList(annotation.syntax()),
                                Arrays.asList(annotation.aliases()),
                                Arrays.asList(annotation.permissions()),
                                mh::invoke
                            );
                            this.typedCommands
                                    .getOrDefault(objClass, new ArrayList<>())
                                    .add(cmd);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("Error occurred while registering command container " + objClass, e);
                        }
                    }
                } else if (method.isAnnotationPresent(ArgumentCompleter.class)) {
                    if (method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == ArgumentCompletionContext.class
                        && method.getReturnType() == List.class) {

                        ArgumentCompleter annotation = method.getAnnotation(ArgumentCompleter.class);

                        try {
                            MethodHandle mh = MethodHandles.lookup().unreflect(method);
                            for (String type : annotation.type()) {
                                this.completers.put(type, new TypedArgCompleter(
                                    type,
                                    annotation.isEntityName(),
                                    ctx -> (List<String>) mh.invoke(ctx)
                                ));
                            }
                        } catch (IllegalAccessException e) {
                            LOGGER.error("Error occurred while registering argument completer " + objClass, e);
                        }
                    }
                }
            }
        }
    }

    public boolean unregisterCommandContainer(Class<? extends CommandRegistrar> objClass) {
        List<ICommand> cmds = this.typedCommands.get(objClass);
        boolean any = false;
        if (cmds != null) {
            for (ICommand cmd : cmds) {
                if (((AdvancedCommandManager)this.commandHandler).unregisterCommand(cmd)) {
                    any = true;
                }
            }
            if (cmds.isEmpty()) {
                this.typedCommands.remove(objClass);
            }
        }
        return any;
    }

    public static class TypedArgCompleter {
        private final String type;
        private final boolean isEntityName;
        private final CheckedFunction<ArgumentCompletionContext, List<String>, Throwable> completer;

        public TypedArgCompleter(String type, boolean isEntityName, CheckedFunction<ArgumentCompletionContext, List<String>, Throwable> completer) {
            this.type = type;
            this.isEntityName = isEntityName;
            this.completer = completer;
        }
    }

    public static class CommandContainer {
        public final String modId;
        public final ICommand command;

        public CommandContainer(String modId, ICommand command) {
            this.modId = modId;
            this.command = command;
        }
    }

    private static final Pattern VARARG_PATTERN = Pattern.compile("<([a-zA-Z0-9:]+)>\\.\\.\\.");
    private static final Pattern ARG_PATTERN = Pattern.compile("<([a-zA-Z0-9:]+)>");
    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[([a-zA-Z0-9|]+)\\]");
    private static final Pattern CONSTANT_ACTION_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    public final class CommandExtended implements ICommand {
        private final List<ArgumentsPattern> patterns;
        private final String name;
        private final List<String> aliases;
        private final List<String> permissions;
        private final CommandExecutor executor;

        public CommandExtended(List<String> syntax, String name, List<String> aliases, List<String> permissions, CommandExecutor executor) {
            List<ArgumentsPattern> patterns = new ArrayList<>();

            for (String a : syntax) {
                patterns.add(new ArgumentsPattern(a));
            }

            this.patterns = patterns;
            this.name = name;
            this.aliases = aliases;

            if (permissions.isEmpty()) {
                permissions = Collections.singletonList("commands." + name);
            }

            this.permissions = permissions;
            this.executor = executor;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "commands." + this.name + ".usage";
        }

        @Override
        public List<String> getAliases() {
            return this.aliases;
        }

        @Override
        public void execute(MinecraftServer _s, ICommandSender sender, String[] rawArgs) throws CommandException {
            for (ArgumentsPattern pattern : this.patterns) {
                if (pattern.matches(rawArgs) == MatchLevel.FULL) {
                    CommandContext ctx = pattern.parse(CommandManager.this.server, sender, this, rawArgs);
                    try {
                        this.executor.execute(ctx);
                    } catch (CommandException e) {
                        throw e;
                    } catch (Throwable t) {
                        LOGGER.error("Error occurred while executing command '" + this.getName() + "'", t);
                        throw new CommandException("commands.generic.unknownFailure", t.getMessage());
                    }
                    return;
                }
            }
            throw new WrongUsageException(this.getUsage(sender));
        }

        @Override
        public boolean checkPermission(MinecraftServer _s, ICommandSender sender) {
            if (_s == sender || sender instanceof CommandBlockBaseLogic) {
                return true;
            } else if (sender instanceof EntityPlayer) {
                if (!_s.isDedicatedServer() && sender.getName().equals(_s.getServerOwner())) {
                    return true;
                }
                PermissionManager permissionManager = CommandManager.this.server.getPermissionManager();
                return permissionManager.hasPermissions(((EntityPlayer)sender).getGameProfile(), this.permissions);
            }
            return false;
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer _s, ICommandSender sender, String[] rawArgs, @Nullable BlockPos targetPos) {
            Set<String> result = new HashSet<>();
            for (ArgumentsPattern pattern : this.patterns) {
                if (pattern.matches(rawArgs) != MatchLevel.NONE) {
                    result.addAll(pattern.complete(CommandManager.this.server, sender, rawArgs, targetPos));
                }
            }
            return new ArrayList<>(result);
        }

        @Override
        public boolean isUsernameIndex(String[] rawArgs, int index) {
            for (ArgumentsPattern pattern : this.patterns) {
                if (pattern.args.size() > index && pattern.args.get(index).isEntityName()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int compareTo(ICommand other) {
            return this.getName().compareTo(other.getName());
        }
    }

    public class ArgumentsPattern {
        private final String pattern;
        private List<Argument> args = new ArrayList<>();
        private int actionCounter, constantCounter;

        public ArgumentsPattern(String pattern) {
            this.pattern = pattern;
            if (!pattern.isEmpty()) {
                String[] split = pattern.split(" ");
                for (int i = 0; i < split.length; i++) {
                    String p = split[i];
                    Matcher m;
                    boolean vararg;
                    if ((vararg = (m = VARARG_PATTERN.matcher(p)).matches()) || (m = ARG_PATTERN.matcher(p)).matches()) {
                        if (vararg && i != split.length - 1) {
                            throw new IllegalArgumentException("Vararg can only be the last argument of a command");
                        }
                        String name = m.group(1);
                        String t = null;
                        if (name.contains(":")) {
                            String[] data = name.split(":");
                            if (data.length != 2) {
                                throw new IllegalArgumentException("Invalid argument type '" + name + "' at index: " + i);
                            }
                            name = data[0];
                            t = data[1];
                        }
                        if (name.startsWith("action") || name.startsWith("const")) {
                            throw new IllegalArgumentException(name + " is a reserved keyword and you cannot use it as an argument name!");
                        }
                        String type = t;
                        this.args.add(new Argument(name, vararg) {
                            @Override
                            public List<String> complete(Server server, ICommandSender sender, String partialValue, @Nullable BlockPos targetBlock) {
                                TypedArgCompleter c = CommandManager.this.completers.get(type);
                                if (c != null) {
                                    try {
                                        Location l = targetBlock != null ? new Location(sender.getEntityWorld(), targetBlock) : null;
                                        return c.completer.apply(new ArgumentCompletionContext(server, type, CommandSender.from(server, sender), partialValue, l));
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                }
                                return Collections.emptyList();
                            }

                            @Override
                            public MatchLevel matches(String partialValue) {
                                return MatchLevel.FULL;
                            }

                            @Override
                            public boolean isEntityName() {
                                TypedArgCompleter c = CommandManager.this.completers.get(type);
                                return c != null && c.isEntityName;
                            }
                        });
                    } else if ((m = ACTION_PATTERN.matcher(p)).matches()) {
                        String[] variants = m.group(1).split("\\|");
                        this.args.add(new Argument("action_" + this.actionCounter++, false) {
                            @Override
                            public List<String> complete(Server server, ICommandSender sender, String partialValue, @Nullable BlockPos targetBlock) {
                                List<String> result = new ArrayList<>();
                                for (String variant : variants) {
                                    if (variant.startsWith(partialValue)) {
                                        result.add(variant);
                                    }
                                }
                                return result;
                            }

                            @Override
                            public MatchLevel matches(String partialValue) {
                                for (String variant : variants) {
                                    if (variant.equals(partialValue)) {
                                        return MatchLevel.FULL;
                                    } else if (variant.startsWith(partialValue)) {
                                        return MatchLevel.PARTIAL;
                                    }
                                }
                                return MatchLevel.NONE;
                            }
                        });
                    } else if ((m = CONSTANT_ACTION_PATTERN.matcher(p)).matches()) {
                        String s = m.group();
                        this.args.add(new Argument("const_" + this.constantCounter++, false) {
                            @Override
                            public List<String> complete(Server server, ICommandSender sender, String partialValue, @Nullable BlockPos targetBlock) {
                                if (s.startsWith(partialValue)) {
                                    return Collections.singletonList(s);
                                }
                                return Collections.emptyList();
                            }

                            @Override
                            public MatchLevel matches(String partialValue) {
                                if (s.equals(partialValue)) {
                                    return MatchLevel.FULL;
                                } else if (s.startsWith(partialValue)) {
                                    return MatchLevel.PARTIAL;
                                } else {
                                    return MatchLevel.NONE;
                                }
                            }
                        });
                    } else {
                        throw new IllegalStateException("Invalid argument type at index " + i);
                    }
                }
            }
        }

        public MatchLevel matches(String[] rawArgs) {
            int rawLen = rawArgs.length;
            int len = this.args.size();
            if (len == 0) {
                return rawLen == 0 ? MatchLevel.FULL : MatchLevel.NONE;
            } else if (rawLen == 0) {
                return MatchLevel.PARTIAL;
            }
            for (int i = 0; i < len; i++) {
                boolean lastArg = (i == len - 1);
                boolean lastRawArg = (i == rawLen - 1);
                Argument arg = this.args.get(i);
                if (lastRawArg && !lastArg) {
                    MatchLevel m = arg.matches(rawArgs[i]);
                    return m == MatchLevel.NONE ? MatchLevel.NONE : MatchLevel.PARTIAL;
                }
                String value;
                if (lastArg && !lastRawArg) {
                    if (arg.isVararg) {
                        value = String.join(" ", Arrays.copyOfRange(rawArgs, i, rawLen));
                    } else {
                        return MatchLevel.NONE;
                    }
                } else {
                    value = rawArgs[i];
                }
                switch (arg.matches(value)) {
                    case PARTIAL:
                        return MatchLevel.PARTIAL;
                    case NONE:
                        return MatchLevel.NONE;
                }
            }
            return MatchLevel.FULL;
        }

        public CommandContext parse(Server server, ICommandSender sender, ICommand command, String[] rawArgs) {
            List<CommandContext.Argument> args = new ArrayList<>();

            for (int i = 0; i < this.args.size(); i++) {
                Argument arg = this.args.get(i);
                String value;
                if (i == this.args.size() - 1 && rawArgs.length > this.args.size() && arg.isVararg) {
                    value = String.join(" ", Arrays.copyOfRange(rawArgs, i, rawArgs.length));
                } else {
                    value = rawArgs[i];
                }
                args.add(arg.parse(value));
            }

            return new CommandContext(server, CommandSender.from(server, sender), command, args);
        }

        public List<String> complete(Server server, ICommandSender sender, String[] rawArgs, @Nullable BlockPos targetPos) {
            int lastRawIndex = rawArgs.length - 1;
            int lastArgIndex = this.args.size() - 1;
            if (lastRawIndex <= lastArgIndex) {
                if (lastArgIndex >= 0) {
                    String value = rawArgs[lastRawIndex];
                    return this.args.get(lastRawIndex).complete(server, sender, value, targetPos);
                }
            }
            return Collections.emptyList();
        }

        public abstract class Argument {
            private final String name;
            private final boolean isVararg;

            public Argument(String name, boolean isVararg) {
                this.name = name;
                this.isVararg = isVararg;
            }

            public abstract List<String> complete(Server server, ICommandSender sender, String partialValue, @Nullable BlockPos targetBlock);
            public abstract MatchLevel matches(String partialValue);
            public boolean isEntityName() {
                return false;
            }

            public CommandContext.Argument parse(String value) {
                return new CommandContext.Argument(this.name, value, this.isVararg);
            }
        }
    }

    public enum MatchLevel {
        NONE, PARTIAL, FULL
    }
}
