package ru.craftlogic.api.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.World;
import ru.craftlogic.common.PermissionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandContext {
    private final Server server;
    private final ICommandSender sender;
    private final List<Argument> argList = new ArrayList<>();
    private final HashMap<String, Argument> argMap = new HashMap<>();

    public CommandContext(Server server, ICommandSender sender, List<Argument> args) {
        this.server = server;
        this.sender = sender;
        for (Argument arg : args) {
            arg.setContext(this);
            this.argList.add(arg);
            this.argMap.put(arg.name, arg);
        }
    }

    public Server server() {
        return this.server;
    }

    public ICommandSender sender() {
        return this.sender;
    }

    public OnlinePlayer senderAsPlayer() throws CommandException {
        if (!(this.sender instanceof EntityPlayerMP)) {
            throw new CommandException("commands.generic.sender.playerOnly");
        } else {
            return new OnlinePlayer(this.server, (EntityPlayerMP) this.sender);
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
        return this.argMap.containsKey(name);
    }

    public Argument get(String name) {
        if (!this.argMap.containsKey(name)) {
            throw new IllegalStateException("No such argument: " + name);
        }
        return this.argMap.get(name);
    }

    public Argument get(int index) {
        if (index >= this.argList.size()) {
            throw new ArrayIndexOutOfBoundsException("Argument index out of bounds: " + index);
        }
        return this.argList.get(index);
    }

    public void sendMessage(String message, Object... args) {
        this.sender.sendMessage(new TextComponentTranslation(message, args));
    }

    public void sendMessage(ITextComponent component) {
        this.sender.sendMessage(component);
    }

    public void failure(String message, Object... args) throws CommandException {
        throw new CommandException(message, args);
    }

    public boolean checkPermissions(String... permissions) throws CommandException {
        if (this.sender instanceof MinecraftServer || this.sender instanceof CommandBlockBaseLogic) {
            return true;
        } else if (this.sender instanceof EntityPlayer) {
            PermissionManager permissionManager = this.server.getPermissionManager();
            if (permissionManager.hasPermissions(((EntityPlayer)this.sender).getGameProfile(), permissions)) {
                return true;
            } else {
                throw new CommandException("commands.generic.noPermission", Arrays.toString(permissions));
            }
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

        public OnlinePlayer asPlayer() throws CommandException {
            OnlinePlayer player = this.context.server.getPlayerByName(this.value);
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

    }
}
