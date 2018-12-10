package ru.craftlogic.api.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.api.world.*;
import ru.craftlogic.api.CraftMessages;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandContext {
    protected final Server server;
    protected final CommandSender sender;
    protected final ICommand command;
    protected final List<Argument> indexToArg = new ArrayList<>();
    protected final Map<String, Argument> nameToArg = new HashMap<>();
    protected final Random random = new Random();

    public CommandContext(Server server, CommandSender sender, ICommand command, List<Argument> args) {
        this.server = server;
        this.sender = sender;
        this.command = command;
        for (Argument arg : args) {
            arg.setContext(this);
            this.indexToArg.add(arg);
            this.nameToArg.put(arg.name, arg);
        }
    }

    public float randomFloat() {
        return random().nextFloat();
    }

    public float randomFloat(float min) {
        return randomFloat(min, 1F);
    }

    public float randomFloat(float min, float max) {
        return min + (max - min) * randomFloat();
    }

    public Random random() {
        return this.random;
    }

    public Server server() {
        return this.server;
    }

    public CommandSender sender() {
        return this.sender;
    }

    public LocatableCommandSender senderAsLocatable() throws CommandException {
        if (this.sender instanceof LocatableCommandSender) {
            return (LocatableCommandSender) this.sender;
        } else {
            throw new CommandException("commands.generic.locatableOnly");
        }
    }

    public Player senderAsPlayer() throws CommandException {
        if (this.sender instanceof Player) {
            return (Player) this.sender;
        } else {
            throw new CommandException("commands.generic.playerOnly");
        }
    }

    public String action() {
        return this.action(0);
    }

    public String action(int index) {
        return this.get("action_" + index).asString();
    }

    public String constant() {
        return this.constant(0);
    }

    public String constant(int index) {
        return this.get("const_" + index).asString();
    }

    public boolean has(String name) {
        return this.nameToArg.containsKey(name);
    }

    public boolean hasAction() {
        return this.hasAction(0);
    }

    public boolean hasAction(int index) {
        return this.has("action_" + index);
    }

    public boolean hasConstant() {
        return this.hasConstant(0);
    }

    public boolean hasConstant(int index) {
        return this.has("const_" + index);
    }

    public Optional<Argument> getIfPresent(String name) {
        return this.has(name) ? Optional.of(this.get(name)) : Optional.empty();
    }

    public <T> Optional<T> getIfPresent(String name, CheckedFunction<Argument, T, CommandException> mapper) throws CommandException {
        Optional<Argument> value = this.getIfPresent(name);
        if (value.isPresent()) {
            return Optional.of(mapper.apply(value.get()));
        }
        return Optional.empty();
    }

    public Argument get(String name) {
        if (!this.nameToArg.containsKey(name)) {
            throw new IllegalStateException("No such argument: " + name);
        }
        return this.nameToArg.get(name);
    }

    public Argument get(int index) {
        if (index >= this.indexToArg.size()) {
            throw new ArrayIndexOutOfBoundsException("Argument index out of bounds: " + index);
        }
        return this.indexToArg.get(index);
    }

    public void sendNotification(Text<?, ?> message) {
        this.sendNotification(0, message);
    }

    public void sendNotification(int flags, Text<?, ?> message) {
        this.sendNotification(flags, message.build());
    }

    public void sendNotification(String message, Object... args) {
        this.sendNotification(0, message, args);
    }

    public void sendNotification(int flags, String message, Object... args) {
        this.sendNotification(flags, new TextComponentTranslation(message, args));
    }

    public void sendNotification(ITextComponent message) {
        this.sendNotification(0, message);
    }

    public void sendNotification(int flags, ITextComponent message) {
        CraftMessages.notifyCommandListener(this.server.unwrap(), this.sender.unwrap(), this.command, flags, message);
    }

    public void sendMessage(String message, Object... args) {
        this.sender.sendMessage(new TextComponentTranslation(message, args));
    }

    public void sendMessage(ITextComponent component) {
        this.sender.sendMessage(component);
    }

    public void sendMessage(Text<?, ?> text) {
        this.sender.sendMessage(text.build());
    }

    public boolean checkPermission(boolean panic, String permission, int opLevel) throws CommandException {
        if (this.server.isSinglePlayer() || this.sender.hasPermission(permission, opLevel)) {
            return true;
        }
        if (panic) {
            throw new CommandException("commands.generic.noPermission", permission);
        }
        return false;
    }

    public static class Argument {
        public static final Pattern IP_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

        private CommandContext context;
        private final String name, value;
        private final boolean vararg;

        public Argument(String name, String value, boolean vararg) {
            this.name = name;
            this.value = value;
            this.vararg = vararg;
        }

        void setContext(CommandContext context) {
            if (this.context != null) {
                throw new IllegalStateException("Context already set!");
            }
            this.context = context;
        }

        public boolean isVararg() {
            return this.vararg;
        }

        public String name() {
            return this.name;
        }

        public String asString() {
            return this.value;
        }

        public UUID asUUID() throws CommandException {
            try {
                return UUID.fromString(this.value);
            } catch (IllegalArgumentException exc) {
                throw new CommandException("commands.generic.uuid.invalidFormat", this.value);
            }
        }

        public OfflinePlayer asOfflinePlayer() throws CommandException {
            OfflinePlayer player = this.context.server.getPlayerManager().getOffline(this.value);
            if (player == null) {
                throw new CommandException("commands.generic.userNeverPlayed", this.value);
            }
            return player;
        }

        public Player asPlayer() throws CommandException {
            Player player = this.context.server.getPlayerManager().getOnline(this.value);
            if (player == null) {
                throw new CommandException("commands.generic.player.notFound", this.value);
            }
            return player;
        }

        public String asIP() throws CommandException {
            if (!IP_PATTERN.matcher(this.value).matches()) {
                throw new CommandException("commands.generic.ip.invalid", this.value);
            }
            return this.value;
        }

        public World asWorld() throws CommandException {
            World world = this.context.server.getWorldManager().get(this.value);
            if (world == null) {
                throw new CommandException("commands.generic.world.notFound", this.value);
            }
            return world;
        }

        public int asInt() throws CommandException {
            try {
                return Integer.parseInt(this.value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.num.invalid", this.value);
            }
        }

        public int asInt(int min, int max) throws CommandException {
            if (min >= max) {
                throw new IllegalStateException("Maximal bound less than minimal");
            }
            int value = this.asInt();
            if (value < min) {
                throw new CommandException("commands.generic.num.tooSmall", value, min);
            }
            if (value > max) {
                throw new CommandException("commands.generic.num.tooBig", value, max);
            }
            return value;
        }

        public float asFloat() throws CommandException {
            try {
                return Float.parseFloat(this.value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.num.invalid", this.value);
            }
        }

        public float asFloat(float min, float max) throws CommandException {
            if (min >= max) {
                throw new IllegalStateException("Maximal bound less than minimal");
            }
            float value = this.asFloat();
            if (value < min) {
                throw new CommandException("commands.generic.num.tooSmall", value, min);
            }
            if (value > max) {
                throw new CommandException("commands.generic.num.tooBig", value, max);
            }
            return value;
        }

        public boolean asBoolean() throws CommandException {
            String value = this.value.toLowerCase();
            switch (value) {
                case "0":
                case "false":
                case "no":
                case "n":
                    return false;
                case "1":
                case "true":
                case "yes":
                case "y":
                    return true;
                default:
                    throw new CommandException("commands.generic.boolean.invalid", value);
            }
        }

        @Override
        public String toString() {
            return "Argument{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", vararg=" + vararg +
                    '}';
        }
    }

    @Override
    public String toString() {
        return indexToArg.stream().map(a -> a.name + " = " + a.value).collect(Collectors.joining(", "));
    }
}
