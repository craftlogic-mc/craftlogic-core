package ru.craftlogic.common.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.PhantomPlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public final class CommandHome extends CommandBase {
    CommandHome() {
        super("home", 0, "", "<target:Player>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : sender;
        if (target.isOnline()) {
            Location bedLocation = adjustBedLocation(target.asOnline().getBedLocation());
            teleportHome(ctx, sender, target.getProfile(), bedLocation);
        } else {
            PhantomPlayer fake = target.asPhantom(sender.getWorld());
            Location bedLocation = adjustBedLocation(fake.getBedLocation());
            teleportHome(ctx, sender, fake.getProfile(), bedLocation);
        }
    }

    @Nullable
    private Location adjustBedLocation(Location l) {
        if (l != null) {
            BlockPos p = l.getPos();
            net.minecraft.world.World world = l.getWorld();
            IBlockState state = l.getBlockState();
            Block block = state.getBlock();
            if (block.isBed(state, world, p, null)) {
                p = block.getBedSpawnPosition(state, world, p, null);
                if (p != null) {
                    return new Location(world, p);
                }
            }
        }
        return null;
    }

    private void teleportHome(CommandContext ctx, Player sender, GameProfile target, Location bedLocation) throws CommandException {
        if (bedLocation != null) {
            Consumer<Server> task = server -> {
                if (sender.isOnline()) {
                    sender.teleport(bedLocation);
                    if (sender.getId().equals(target.getId())) {
                        ctx.sendMessage(Text.translation("commands.home.teleport.you").green());
                    } else {
                        ctx.sendMessage(Text.translation("commands.home.teleport.other").green().arg(target.getName(), Text::darkGreen));
                    }
                }
            };
            double distance = bedLocation.distance(sender.getLocation());
            if (distance <= 200 || sender.hasPermission("commands.home.instant")) {
                task.accept(ctx.server());
            } else {
                int timeout = 5;
                Text<?, ?> message = sender.getId().equals(target.getId()) ?
                    Text.translation("tooltip.home_teleport") :
                    Text.translation("tooltip.home_teleport.other");
                sender.sendCountdown("home", message, timeout);
                UUID id = ctx.server().addDelayedTask(task, timeout * 1000 + 250);
                sender.addPendingTeleport(id);
            }
        } else {
            if (sender.getId().equals(target.getId())) {
                throw new CommandException("commands.home.missing.you");
            } else {
                throw new CommandException("commands.home.missing.other", target.getName());
            }
        }
    }
}
