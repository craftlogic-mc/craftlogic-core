package ru.craftlogic.mixin.world;

import com.google.common.base.Throwables;
import net.minecraft.command.*;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.command.AdvancedCommandManager;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.common.command.CommandManager.CommandContainer;

import javax.annotation.Nullable;
import java.util.*;

import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler implements AdvancedCommandManager {
    @Shadow @Final
    private static Logger LOGGER;

    private final Map<String, List<ResourceLocation>> aliases = new HashMap<>();
    private final Map<ResourceLocation, CommandContainer> registry = new HashMap<>();

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    public int executeCommand(ICommandSender sender, String rawString) {
        rawString = rawString.trim();
        if (rawString.startsWith("/")) {
            rawString = rawString.substring(1);
        }

        String[] args = rawString.split(" ");
        String commandName = args[0];
        args = dropFirstString(args);

        CommandContainer container = getCommand(commandName);

        int i = 0;

        try {
            if (container == null) {
                sender.sendMessage(Text.translation("commands.generic.notFound").red().build());
            } else if (container.checkPermission(getServer(), sender, args, false)) {
                ICommand command = container.command;
                int usernameIndex = getUsernameIndex(command, args);
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

                if (usernameIndex >= 0) {
                    List<Entity> entities = EntitySelector.matchEntities(sender, args[usernameIndex], Entity.class);
                    String s1 = args[usernameIndex];
                    sender.setCommandStat(Type.AFFECTED_ENTITIES, entities.size() - 1);
                    if (entities.isEmpty()) {
                        throw new PlayerNotFoundException("commands.generic.selector.notFound", args[usernameIndex]);
                    }

                    for (Entity entity : entities) {
                        args[usernameIndex] = entity.getCachedUniqueIdString();
                        if (tryExecute(sender, args, command, rawString)) {
                            ++i;
                        }
                    }

                    args[usernameIndex] = s1;
                } else {
                    sender.setCommandStat(Type.AFFECTED_ENTITIES, 1);
                    if (tryExecute(sender, args, command, rawString)) {
                        ++i;
                    }
                }
            } else {
                sender.sendMessage(Text.translation("commands.generic.permission").red().build());
            }
        } catch (CommandException exc) {
            sender.sendMessage(Text.translation(exc).red().build());
        }

        sender.setCommandStat(Type.SUCCESS_COUNT, i);
        return i;
    }

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    protected boolean tryExecute(ICommandSender sender, String[] args, ICommand command, String rawCommand) {
        try {
            command.execute(getServer(), sender, args);
            return true;
        } catch (WrongUsageException exc) {
            sender.sendMessage(
                Text.translation("commands.generic.usage").darkRed()
                    .argTranslate(exc, Text::red)
                    .build()
            );
        } catch (CommandException exc) {
            sender.sendMessage(Text.translation(exc).red().build());
        } catch (Throwable t) {
            sender.sendMessage(Text.translation("commands.generic.exception").red().build());
            LOGGER.warn("Couldn't process command: " + rawCommand, t);
        }

        return false;
    }

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    public ICommand registerCommand(ICommand command) {
        ResourceLocation commandName = wrapWithActiveModId(command.getName(), "minecraft");
        CommandContainer container = new CommandContainer(commandName.getNamespace(), command);

        registry.put(commandName, container);
        aliases.computeIfAbsent(commandName.getPath(), k -> new ArrayList<>()).add(commandName);

        for (String alias : command.getAliases()) {
            registry.put(new ResourceLocation(commandName.getNamespace(), alias), container);
            aliases.computeIfAbsent(alias, k -> new ArrayList<>()).add(commandName);
        }

        LOGGER.debug("Registered command " + commandName);

        return command;
    }

    @Override
    public boolean unregisterCommand(ICommand command) {
        boolean dirty = false;
        Iterator<Map.Entry<ResourceLocation, CommandContainer>> reg = registry.entrySet().iterator();
        while (reg.hasNext()) {
            Map.Entry<ResourceLocation, CommandContainer> entry = reg.next();
            CommandContainer container = entry.getValue();
            if (container.command == command) {
                Iterator<List<ResourceLocation>> als = aliases.values().iterator();
                while (als.hasNext()) {
                    List<ResourceLocation> aliases = als.next();
                    dirty |= aliases.removeIf(name -> name.equals(entry.getKey()));
                    if (aliases.isEmpty()) {
                        als.remove();
                    }
                }
                reg.remove();
            }
        }
        if (dirty) {
            refreshAliases();
        }
        return dirty;
    }

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    public List<String> getTabCompletions(ICommandSender sender, String rawString, @Nullable BlockPos targetBlockPos) {
        String[] args = rawString.split(" ", -1);
        String rawCommandName = args[0];
        String modId = null;
        String commandName = rawCommandName;
        if (rawCommandName.contains(":")) {
            int colonIdx = rawCommandName.indexOf(":");
            modId = rawCommandName.substring(0, colonIdx);
            commandName = rawCommandName.substring(colonIdx);
        }
        if (args.length == 1) {
            args = dropFirstString(args);
            Set<String> result = new HashSet<>();

            for (Map.Entry<String, List<ResourceLocation>> entry : aliases.entrySet()) {
                String s = entry.getKey();
                List<ResourceLocation> cmds = entry.getValue();
                if (CommandBase.doesStringStartWith(commandName, s) && cmds != null && !cmds.isEmpty()) {
                    CommandContainer container = null;
                    if (modId == null) {
                        container = getCommand(cmds.get(cmds.size() - 1));
                    } else {
                        for (ResourceLocation c : cmds) {
                            if (c.getNamespace().equalsIgnoreCase(modId)) {
                                container = getCommand(c);
                                break;
                            }
                        }
                    }
                    if (container != null && container.checkPermission(getServer(), sender, args, true)) {
                        result.add(s);
                    }
                }
            }

            return new ArrayList<>(result);
        } else {
            CommandContainer container = getCommand(rawCommandName);
            if (container != null && container.checkPermission(getServer(), sender, args, true)) {
                try {
                    return container.command.getTabCompletions(getServer(), sender, args, targetBlockPos);
                } catch (Throwable t) {
                    if (t.getCause() instanceof CommandException) {
                        sender.sendMessage(Text.translation((CommandException)t.getCause()).red().build());
                    } else {
                        sender.sendMessage(Text.translation("commands.generic.exception").red().build());
                        LOGGER.warn("Couldn't process command completion: " + rawCommandName, t);
                    }
                }
            }

            return Collections.emptyList();
        }
    }

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    public List<ICommand> getPossibleCommands(ICommandSender sender) {
        Set<ICommand> result = new HashSet<>();

        for (CommandContainer container : registry.values()) {
            if (!result.contains(container.command) && container.checkPermission(getServer(), sender, new String[0], true)) {
                result.add(container.command);
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * @author Radviger
     * @reason Extended commands' features
     */
    @Overwrite
    public Map<String, ICommand> getCommands() {
        Map<String, ICommand> result = new HashMap<>();
        for (Map.Entry<ResourceLocation, CommandContainer> entry : registry.entrySet()) {
            CommandContainer container = entry.getValue();
            result.put(entry.getKey().toString(), container.command);
        }
        return result;
    }

    private CommandContainer getCommand(ResourceLocation name) {
        return registry.get(name);
    }

    private CommandContainer getCommand(String name) {
        if (name.contains(":")) {
            return getCommand(new ResourceLocation(name));
        }
        List<ResourceLocation> aliases = this.aliases.get(name);
        if (aliases != null && !aliases.isEmpty()) {
            return getCommand(aliases.get(aliases.size() - 1));
        }
        return null;
    }

    private void refreshAliases() {
        aliases.clear();
        for (Map.Entry<ResourceLocation, CommandContainer> entry : registry.entrySet()) {
            ResourceLocation commandName = entry.getKey();
            CommandContainer container = entry.getValue();
            aliases.computeIfAbsent(commandName.getPath(), k -> new ArrayList<>()).add(commandName);
            for (String alias : container.command.getAliases()) {
                aliases.computeIfAbsent(alias, k -> new ArrayList<>()).add(commandName);
            }
        }
    }

    @Shadow
    protected abstract MinecraftServer getServer();

    @Shadow
    private int getUsernameIndex(ICommand cmd, String[] args) throws CommandException { return -1; }

    @Shadow
    private static String[] dropFirstString(String[] arr) { return arr; }
}
