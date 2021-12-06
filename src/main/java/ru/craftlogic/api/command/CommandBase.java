package ru.craftlogic.api.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ru.craftlogic.api.permission.PermissionManager;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.LocatableCommandSender;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.command.CommandManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandBase implements ICommand {
    private static final Pattern VARARG_PATTERN = Pattern.compile("<([a-zA-Z0-9:]+)>\\.\\.\\.");
    private static final Pattern ARG_PATTERN = Pattern.compile("<([a-zA-Z0-9_:]+)>");
    private static final Pattern ACTION_PATTERN = Pattern.compile("([a-zA-Z0-9_|]+)");

    private final String name;
    private final int opLevel;
    private final List<Syntax> syntax;
    private final List<ArgPattern> patterns = new ArrayList<>();
    protected final List<String> aliases = new ArrayList<>();

    protected CommandBase(String name, int opLevel, List<Syntax> syntax) {
        this.name = name;
        this.opLevel = opLevel;
        this.syntax = syntax;
        for (Syntax s : syntax) {
            this.patterns.add(new ArgPattern(s.pattern, s.permission));
        }
    }

    protected CommandBase(String name, int opLevel, String... syntax) {
        this(name, opLevel, convertSyntax(name, syntax));
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getUsage(ICommandSender sender) {
        return "commands." + name + ".usage";
    }

    @Override
    public final List<String> getAliases() {
        return aliases;
    }

    public final int getOpLevel() {
        return opLevel;
    }

    @Override
    public final void execute(MinecraftServer _server, ICommandSender _sender, String[] rawArgs) throws CommandException {
        Server server = Server.from(_server);
        for (ArgPattern pattern : patterns) {
            if (pattern.matches(rawArgs) == MatchLevel.FULL) {
                CommandContext ctx = pattern.parse(server, _sender, this, rawArgs);
                try {
                    execute(ctx);
                } catch (CommandException e) {
                    throw e;
                } catch (Throwable t) {
                    CommandManager.LOGGER.error("Error occurred while executing command '" + getName() + "'", t);
                    throw new CommandException("commands.generic.unknownFailure", t.getMessage());
                }
                return;
            }
        }
        throw new WrongUsageException(getUsage(_sender));
    }

    protected abstract void execute(CommandContext context) throws Throwable;

    public final ArgPattern getPattern(MinecraftServer _server, ICommandSender _sender, String[] rawArgs, boolean partial, boolean firstPermitted) throws CommandException {
        Server server = Server.from(_server);
        ArgPattern any = null;
        for (ArgPattern pattern : patterns) {
            MatchLevel match = pattern.matches(rawArgs);
            if (partial ? match != MatchLevel.NONE : match == MatchLevel.FULL) {
                any = pattern;
                CommandSender sender = CommandSender.from(server, _sender);
                if (!firstPermitted || sender.hasPermission(pattern.permission, opLevel)) {
                    return pattern;
                }
            }
        }
        if (firstPermitted && any != null) {
            return null;
        } else {
            throw new WrongUsageException(getUsage(_sender));
        }
    }

    public final boolean checkPermission(MinecraftServer _server, ICommandSender _sender, String[] rawArgs, boolean partial) throws CommandException {
        if (_server.isSinglePlayer() && _sender.getName().equalsIgnoreCase(_server.getServerOwner()) || _sender == _server) {
            return true;
        }
        Server server = Server.from(_server);
        CommandSender from = CommandSender.from(server, _sender);
        PermissionManager permissionManager = server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            return getPattern(_server, _sender, rawArgs, partial, true) != null;
        } else {
            return from.getOperatorLevel() >= opLevel;
        }
    }

    @Override
    public final boolean checkPermission(MinecraftServer _server, ICommandSender _sender) {
        if (_server.isSinglePlayer() && _sender.getName().equalsIgnoreCase(_server.getServerOwner())) {
            return true;
        }
        Server server = Server.from(_server);
        CommandSender from = CommandSender.from(server, _sender);
        PermissionManager permissionManager = server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            return false;
        } else {
            return from.getOperatorLevel() >= opLevel;
        }
    }

    @Override
    public final List<String> getTabCompletions(MinecraftServer _server, ICommandSender _sender, String[] rawArgs, @Nullable BlockPos targetPos) {
        Server server = Server.from(_server);
        CommandSender sender = CommandSender.from(server, _sender);
        Set<String> result = new HashSet<>();
        for (ArgPattern pattern : patterns) {
            if (pattern.matches(rawArgs) != MatchLevel.NONE) {
                result.addAll(pattern.complete(server, sender, rawArgs, targetPos));
            }
        }
        return new ArrayList<>(result);
    }

    @Override
    public final boolean isUsernameIndex(String[] args, int index) {
        for (ArgPattern pattern : patterns) {
            if (pattern.args.size() > index && pattern.args.get(index).isEntityName()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final int compareTo(ICommand other) {
        return getName().compareTo(other.getName());
    }

    private static List<Syntax> convertSyntax(String name, String... syntax) {
        List<Syntax> result = new ArrayList<>();
        for (String s : syntax) {
            result.add(new Syntax(s, "commands." + name));
        }
        return result;
    }

    public static class Syntax {
        protected final String pattern;
        protected final String permission;

        public Syntax(String pattern, String permission) {
            this.pattern = pattern;
            this.permission = permission;
        }
    }

    public static class ArgPattern {
        private final String pattern;
        private final String permission;
        private List<Arg> args = new ArrayList<>();
        private int actionCounter;

        public ArgPattern(String pattern, String permission) {
            this.pattern = pattern;
            this.permission = permission;
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
                        if (name.startsWith("action")) {
                            throw new IllegalArgumentException(name + " is a reserved keyword and cannot be used as argument name!");
                        }
                        String type = t;
                        this.args.add(new Arg(name, vararg) {
                            @Override
                            public List<String> complete(Server server, CommandSender sender, String partialValue, @Nullable BlockPos targetBlock) {
                                CommandManager.ArgType c = server.getCommandManager().getCompleter(type);
                                if (c != null) {
                                    try {
                                        Location l = targetBlock != null && sender instanceof LocatableCommandSender ? ((LocatableCommandSender) sender).getWorld().getLocation(targetBlock) : null;
                                        ArgCompletionContext ctx = new ArgCompletionContext(server, type, sender, partialValue, l);
                                        Collection<String> variants = c.apply(ctx);
                                        List<String> result = new ArrayList<>();
                                        for (String variant : variants) {
                                            if (variant.toLowerCase().startsWith(partialValue.toLowerCase())) {
                                                result.add(variant);
                                            }
                                        }
                                        return result;
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
                                Server server = Server.from(FMLCommonHandler.instance().getMinecraftServerInstance());
                                CommandManager.ArgType c = server.getCommandManager().getCompleter(type);
                                return c != null && c.isEntityName;
                            }
                        });
                    } else if ((m = ACTION_PATTERN.matcher(p)).matches()) {
                        String[] variants = m.group(1).split("\\|");
                        this.args.add(new Arg("action:" + this.actionCounter++, false) {
                            @Override
                            public List<String> complete(Server server, CommandSender sender, String partialValue, @Nullable BlockPos targetBlock) {
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
                    } else {
                        throw new IllegalStateException("Invalid argument type at index " + i + " in pattern '" + pattern + "'");
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
                Arg arg = this.args.get(i);
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

        public CommandContext parse(Server server, ICommandSender _sender, ICommand command, String[] rawArgs) {
            CommandSender sender = CommandSender.from(server, _sender);
            List<CommandContext.Argument> args = new ArrayList<>();

            for (int i = 0; i < this.args.size(); i++) {
                Arg arg = this.args.get(i);
                String value;
                if (i == this.args.size() - 1 && rawArgs.length > this.args.size() && arg.isVararg) {
                    value = String.join(" ", Arrays.copyOfRange(rawArgs, i, rawArgs.length));
                } else {
                    value = rawArgs[i];
                }
                args.add(arg.parse(sender, value));
            }

            return new CommandContext(server, sender, command, args);
        }

        public List<String> complete(Server server, CommandSender sender, String[] rawArgs, @Nullable BlockPos targetPos) {
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

        public abstract static class Arg {
            private final String name;
            private final boolean isVararg;

            public Arg(String name, boolean isVararg) {
                this.name = name;
                this.isVararg = isVararg;
            }

            public abstract List<String> complete(Server server, CommandSender sender, String partialValue, @Nullable BlockPos targetBlock);
            public abstract MatchLevel matches(String partialValue);
            public boolean isEntityName() {
                return false;
            }

            public CommandContext.Argument parse(CommandSender sender, String value) {
                return new CommandContext.Argument(sender, this.name, value, this.isVararg);
            }
        }
    }

    public enum MatchLevel {
        NONE, PARTIAL, FULL
    }
}
