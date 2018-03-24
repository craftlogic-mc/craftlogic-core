package ru.craftlogic.mixin;

import com.google.common.base.Throwables;
import net.minecraft.command.*;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.command.AdvancedCommandManager;
import ru.craftlogic.common.command.CommandRegistry.CommandContainer;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler implements AdvancedCommandManager {
    @Shadow @Final
    private static Logger LOGGER;

    private final Map<String, ResourceLocation> aliases = new HashMap<>();
    private final Map<String, List<CommandContainer>> registry = new HashMap<>();

    @Overwrite
    public int executeCommand(ICommandSender sender, String rawString) {
        rawString = rawString.trim();
        if (rawString.startsWith("/")) {
            rawString = rawString.substring(1);
        }

        String[] args = rawString.split(" ");
        String modId = null;
        String commandName = args[0];
        if (commandName.contains(":")) {
            modId = commandName.substring(0, commandName.indexOf(":"));
        }
        args = dropFirstString(args);

        List<CommandContainer> matchedCommands = this.registry.get(commandName);
        CommandContainer container = null;
        if (matchedCommands != null && !matchedCommands.isEmpty()) {
            if (modId == null) {
                container = matchedCommands.get(matchedCommands.size() - 1);
            } else {
                for (CommandContainer c : matchedCommands) {
                    if (c.modId.equalsIgnoreCase(modId)) {
                        container = c;
                        break;
                    }
                }
            }
        }

        int i = 0;

        try {
            if (container == null) {
                TextComponentTranslation msg = new TextComponentTranslation("commands.generic.notFound");
                msg.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(msg);
            } else if (container.command.checkPermission(this.getServer(), sender)) {
                ICommand command = container.command;
                int usernameIndex = this.getUsernameIndex(command, args);
                CommandEvent event = new CommandEvent(command, sender, args);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    if (event.getException() != null) {
                        Throwables.throwIfUnchecked(event.getException());
                    }

                    return 1;
                }

                if (event.getParameters() != null) {
                    args = event.getParameters();
                }

                if (usernameIndex > -1) {
                    List<Entity> entities = EntitySelector.matchEntities(sender, args[usernameIndex], Entity.class);
                    String s1 = args[usernameIndex];
                    sender.setCommandStat(Type.AFFECTED_ENTITIES, entities.size() - 1);
                    if (entities.isEmpty()) {
                        throw new PlayerNotFoundException("commands.generic.selector.notFound", args[usernameIndex]);
                    }

                    for (Entity entity : entities) {
                        args[usernameIndex] = entity.getCachedUniqueIdString();
                        if (this.tryExecute(sender, args, command, rawString)) {
                            ++i;
                        }
                    }

                    args[usernameIndex] = s1;
                } else {
                    sender.setCommandStat(Type.AFFECTED_ENTITIES, 1);
                    if (this.tryExecute(sender, args, command, rawString)) {
                        ++i;
                    }
                }
            } else {
                TextComponentTranslation msg = new TextComponentTranslation("commands.generic.permission");
                msg.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(msg);
            }
        } catch (CommandException exc) {
            TextComponentTranslation msg = new TextComponentTranslation(exc.getMessage(), exc.getErrorObjects());
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        }

        sender.setCommandStat(Type.SUCCESS_COUNT, i);
        return i;
    }

    @Overwrite
    protected boolean tryExecute(ICommandSender sender, String[] args, ICommand command, String commandName) {
        try {
            command.execute(this.getServer(), sender, args);
            return true;
        } catch (WrongUsageException exc) {
            TextComponentTranslation msg = new TextComponentTranslation("commands.generic.usage", new TextComponentTranslation(exc.getMessage(), exc.getErrorObjects()));
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        } catch (CommandException exc) {
            TextComponentTranslation msg = new TextComponentTranslation(exc.getMessage(), exc.getErrorObjects());
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        } catch (Throwable t) {
            TextComponentTranslation msg = new TextComponentTranslation("commands.generic.exception");
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
            LOGGER.warn("Couldn't process command: " + commandName, t);
        }

        return false;
    }

    @Overwrite
    protected abstract MinecraftServer getServer();

    @Overwrite
    public ICommand registerCommand(ICommand command) {
        ResourceLocation commandName;
        if (command.getName().contains(":")) {
            commandName = new ResourceLocation(command.getName());
        } else {
            ModContainer amc = Loader.instance().activeModContainer();
            commandName = new ResourceLocation((amc != null ? amc.getModId() : "minecraft"), command.getName());
        }
        this.registry
            .computeIfAbsent(commandName.getResourcePath(), k -> new ArrayList<>())
            .add(new CommandContainer(commandName.getResourceDomain(), command));

        for (String alias : command.getAliases()) {
            this.aliases.put(alias, commandName);
        }

        System.out.println("Registered command " + commandName);

        return command;
    }

    @Override
    public boolean unregisterCommand(ICommand command) {
        for (Iterator<Map.Entry<String, List<CommandContainer>>> iterator = this.registry.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, List<CommandContainer>> entry = iterator.next();
            entry.getValue().removeIf(container -> container.command == command);
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        return false;
    }

    @Overwrite
    public List<String> getTabCompletions(ICommandSender sender, String rawString, @Nullable BlockPos targetBlockPos) {
        String[] args = rawString.split(" ", -1);
        String commandName = args[0];
        String modId = null;
        if (commandName.contains(":")) {
            modId = commandName.substring(0, commandName.indexOf(":"));
        }
        if (args.length == 1) {
            List<String> result = new ArrayList<>();

            for (String s : this.registry.keySet()) {
                List<CommandContainer> cmds = this.registry.get(s);
                CommandContainer container = null;
                if (modId == null) {
                    container = cmds.get(cmds.size() - 1);
                } else {
                    for (CommandContainer c : cmds) {
                        if (c.modId.equalsIgnoreCase(modId)) {
                            container = c;
                        }
                    }
                }
                if (container != null) {
                    if (CommandBase.doesStringStartWith(commandName, s) && container.command.checkPermission(this.getServer(), sender)) {
                        result.add(s);
                    }
                }
            }

            return result;
        } else {
            List<CommandContainer> cmds = this.registry.get(commandName);
            if (cmds != null && !cmds.isEmpty()) {
                CommandContainer container = null;
                if (modId == null) {
                    container = cmds.get(cmds.size() - 1);
                } else {
                    for (CommandContainer c : cmds) {
                        if (c.modId.equalsIgnoreCase(modId)) {
                            container = c;
                        }
                    }
                }
                if (container != null && container.command.checkPermission(this.getServer(), sender)) {
                    return container.command.getTabCompletions(this.getServer(), sender, dropFirstString(args), targetBlockPos);
                }
            }

            return Collections.emptyList();
        }
    }

    @Overwrite
    public List<ICommand> getPossibleCommands(ICommandSender sender) {
        List<ICommand> result = new ArrayList<>();

        for (List<CommandContainer> containers : this.registry.values()) {
            CommandContainer container = containers.get(containers.size() - 1);
            if (container.command.checkPermission(this.getServer(), sender)) {
                result.add(container.command);
            }
        }

        return result;
    }

    @Override
    public Map<String, ICommand> getCommands() {
        Map<String, ICommand> result = new HashMap<>();
        for (Map.Entry<String, List<CommandContainer>> entry : this.registry.entrySet()) {
            List<CommandContainer> cmds = entry.getValue();
            CommandContainer container = cmds.get(cmds.size() - 1);
            result.put(entry.getKey(), container.command);
            result.put(container.modId + ":" + entry.getKey(), container.command);
        }
        return result;
    }

    @Shadow
    private static String[] dropFirstString(String[] arr) { return arr; }

    @Shadow
    private int getUsernameIndex(ICommand cmd, String[] args) throws CommandException { return -1; }
}
