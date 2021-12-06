package ru.craftlogic.api.command;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.server.PlayerManager;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.api.world.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    public Player senderAsPlayerOrArg(String name) throws CommandException {
        return has(name) ? get(name).asPlayer() : senderAsPlayer();
    }

    public String action(int index) {
        return this.get("action:" + index).asString();
    }

    public boolean has(String name) {
        return this.nameToArg.containsKey(name);
    }

    public boolean hasAction(int index) {
        return this.has("action:" + index);
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
        private final CommandSender sender;
        private final String name;
        private final String value;
        private final boolean vararg;

        public Argument(CommandSender sender, String name, String value, boolean vararg) {
            this.sender = sender;
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
            return vararg;
        }

        public String name() {
            return name;
        }

        public String asString() {
            return value;
        }

        @Nonnull
        public UUID asUUID() throws CommandException {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException exc) {
                throw new CommandException("commands.generic.uuid.invalidFormat", this.value);
            }
        }

        @Nullable
        public UUID asUUIDOrNull() {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException exc) {
                return null;
            }
        }

        @Nonnull
        public OfflinePlayer asOfflinePlayer() throws CommandException {
            PlayerManager playerManager = context.server.getPlayerManager();
            UUID uuid = asUUIDOrNull();
            OfflinePlayer player = uuid != null ? playerManager.getOffline(uuid) : playerManager.getOffline(value);
            if (player == null) {
                throw new CommandException("commands.generic.userNeverPlayed", value);
            }
            return player;
        }

        @Nonnull
        public Player asPlayer() throws CommandException {
            Player player = context.server.getPlayerManager().getOnline(value);
            if (player == null || player.getGameMode() == GameType.SPECTATOR && !sender.hasPermission("command.completion.spectators")) {
                throw new CommandException("commands.generic.player.notFound", value);
            }
            return player;
        }

        public long asDuration() throws CommandException {
            return parseDuration(value);
        }

        public String asIP() throws CommandException {
            if (!IP_PATTERN.matcher(value).matches()) {
                throw new CommandException("commands.generic.ip.invalid", value);
            }
            return value;
        }

        @Nonnull
        public World asWorld() throws CommandException {
            World world = this.context.server.getWorldManager().get(value);
            if (world == null) {
                throw new CommandException("commands.generic.world.notFound", value);
            }
            return world;
        }

        public DimensionType asDimension() throws CommandException {
            try {
                return DimensionType.byName(value);
            } catch (IllegalArgumentException e) {
                throw new CommandException("commands.generic.world.notFound", value);
            }
        }

        public int asInt() throws CommandException {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.num.invalid", value);
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
                return Float.parseFloat(value);
            } catch (NumberFormatException exc) {
                throw new CommandException("commands.generic.num.invalid", value);
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

        @Nonnull
        public Item asItem() throws CommandException {
            ResourceLocation id = new ResourceLocation(value);
            if (!Item.REGISTRY.containsKey(id)) {
                throw new CommandException("commands.give.item.notFound", value);
            }
            return Item.REGISTRY.getObject(id);
        }

        @Nonnull
        public Block asBlock() throws CommandException {
            ResourceLocation id = new ResourceLocation(value);
            if (!Block.REGISTRY.containsKey(id)) {
                throw new CommandException("commands.give.block.notFound", value);
            }
            return Block.REGISTRY.getObject(id);
        }

        @Nonnull
        public IBlockState asBlockState() throws CommandException {
            int firstBracket = value.indexOf('[');
            boolean hasBracket = firstBracket >= 0;
            ResourceLocation id = new ResourceLocation(hasBracket ? value.substring(0, firstBracket) : value);
            if (!Block.REGISTRY.containsKey(id)) {
                throw new CommandException("commands.give.block.notFound", value);
            }
            Block block = Block.REGISTRY.getObject(id);
            IBlockState state = block.getDefaultState();
            if (hasBracket) {
                String args = value.substring(firstBracket + 1, value.length() - 1);
                Map<String, IProperty<?>> properties = new HashMap<>();
                for (IProperty<?> property : state.getPropertyKeys()) {
                    properties.put(property.getName(), property);
                }
                if (!args.trim().isEmpty()) {
                    for (String kv : args.split(",")) {
                        String[] parts = kv.split("=");
                        if (parts.length == 2) {
                            String k = parts[0];
                            String v = parts[1];
                            if (properties.containsKey(k)) {
                                IProperty property = properties.get(k);
                                com.google.common.base.Optional<?> value = property.parseValue(v);
                                if (value.isPresent()) {
                                    state = state.withProperty(property, (Comparable)value.get());
                                } else {
                                    throw new CommandException("Invalid property value: " + k + "=" + v + " for block " + id);
                                }
                            } else {
                                throw new CommandException("Invalid property " + k + " for block " + id);
                            }
                        } else {
                            throw new CommandException("Invalid property=value pair: " + Arrays.toString(parts) + " for block " + id);
                        }
                    }
                }
            }
            return state;
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

    public static long parseDuration(String part) throws CommandException {
        float time = 0;
        String type = part.substring(part.length() - 1);
        double t = Double.parseDouble(part.substring(0, part.length() - 1));
        switch (type) {
            case "s": {
                time += t;
                break;
            }
            case "m": {
                time += 60 * t;
                break;
            }
            case "h": {
                time += 60 * 60 * t;
                break;
            }
            case "d": {
                time += 24 * 60 *60 * t;
                break;
            }
            case "w": {
                time += 7 * 24 * 60 * 60 * t;
            }
        }
        if(time <= 0) {
            throw new CommandException("commands.generic.wrongTimeFormat", part);
        }
        return (long)(time * 1000);
    }

    @Override
    public String toString() {
        return indexToArg.stream().map(a -> a.name + " = " + a.value).collect(Collectors.joining(", "));
    }
}
