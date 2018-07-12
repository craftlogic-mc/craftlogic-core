package ru.craftlogic.api.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

import java.util.*;

public class CommandContext {
    private final Server server;
    private final CommandSender sender;
    private final ICommand command;
    private final List<Argument> indexToArg = new ArrayList<>();
    private final Map<String, Argument> nameToArg = new HashMap<>();

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

    public Server server() {
        return this.server;
    }

    public CommandSender sender() {
        return this.sender;
    }

    public Player senderAsPlayer() throws CommandException {
        if (this.sender instanceof Player) {
            return ((Player) this.sender);
        } else {
            throw new CommandException("commands.generic.playerOnly");
        }
    }

    public String action() {
        return this.action(0);
    }

    public String action(int id) {
        return this.get("action_" + id).asString();
    }

    public String constant() {
        return this.constant(0);
    }

    public String constant(int id) {
        return this.get("const_" + id).asString();
    }

    public boolean has(String name) {
        return this.nameToArg.containsKey(name);
    }

    public boolean hasAction() {
        return this.hasAction(0);
    }

    public boolean hasAction(int id) {
        return this.has("action_" + id);
    }

    public boolean hasConstant() {
        return this.hasConstant(0);
    }

    public boolean hasConstant(int id) {
        return this.has("const_" + id);
    }

    public Optional<Argument> getIfPresent(String name) {
        return this.has(name) ? Optional.of(this.get(name)) : Optional.empty();
    }

    public <T> Optional<T> getIfPresent(String name, CheckedFunction<Argument, T, CommandException> mapper) {
        return this.getIfPresent(name).map(mapper.unwrap());
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

    public void sendNotification(String message, Object... args) {
        CommandBase.notifyCommandListener(this.sender.getHandle(), this.command, message, args);
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

    public boolean checkPermissions(boolean panic, String... permissions) throws CommandException {
        if (this.server.isSinglePlayer() || this.sender.hasPermissions(permissions)) {
            return true;
        }
        if (panic) {
            throw new CommandException("commands.generic.noPermission", Arrays.toString(permissions));
        }
        return false;
    }

    public static class Argument {
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

        public OfflinePlayer asOfflinePlayer() throws CommandException {
            OfflinePlayer player = this.context.server.getOfflinePlayerByName(this.value);
            if (player == null) {
                throw new CommandException("commands.generic.userNeverPlayed", this.value);
            }
            return player;
        }

        public Player asPlayer() throws CommandException {
            Player player = this.context.server.getPlayerByName(this.value);
            if (player == null) {
                throw new CommandException("commands.generic.player.notFound", this.value);
            }
            return player;
        }

        public World asWorld() throws CommandException {
            World world = this.context.server.getWorld(this.value);
            if (world == null) {
                throw new CommandException("commands.generic.world.notFound", this.value);
            }
            return world;
        }

        public int asInt() throws CommandException {
            try {
                return Integer.parseInt(this.value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.integer.invalid", this.value);
            }
        }

        public int asInt(int min, int max) throws CommandException {
            if (min >= max) {
                throw new IllegalStateException("Maximal bound less than minimal");
            }
            int value = this.asInt();
            if (value < min || value > max) {
                throw new CommandException("commands.generic.integer.bounds", value, min, max);
            }
            return value;
        }

        public float asFloat() throws CommandException {
            try {
                return Float.parseFloat(this.value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.float.invalid", this.value);
            }
        }

        public float asFloat(float min, float max) throws CommandException {
            if (min >= max) {
                throw new IllegalStateException("Maximal bound less than minimal");
            }
            float value = this.asFloat();
            if (value < min || value > max) {
                throw new CommandException("commands.generic.float.bounds", value, min, max);
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
    }
}
