package ru.craftlogic.common.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.event.player.PlayerTeleportHomeEvent;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.PhantomPlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class CommandHome extends CommandBase {
    CommandHome() {
        super("home", 0, "", "<target:Player>");
    }


    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        if (ctx.has("target") && sender.hasPermission("commands.home.other")) {
            OfflinePlayer target = ctx.get("target").asOfflinePlayer();
            if (target.isOnline()) {
                Location bedLocation = target.asOnline().getBedLocation(sender.getWorld());
                teleportHome(ctx, sender, target, bedLocation, false);
            } else {
                PhantomPlayer fake = target.asPhantom(sender.getWorld());
                Location bedLocation = fake.getBedLocation(sender.getWorld());
                teleportHome(ctx, sender, fake, bedLocation, true);
            }
        } else {
            Location location = sender.getBedLocation();
            teleportHome(ctx, sender, sender, location, false);
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

    private void startTeleportation(CommandContext ctx, Player sender, OfflinePlayer target, Location bedLocation, boolean offline) {
        if (!MinecraftForge.EVENT_BUS.post(new PlayerTeleportHomeEvent(sender, target, bedLocation, ctx, offline))) {
            GameProfile targetProfile = target.getProfile();
            Consumer<Server> callback = server -> {
                if (sender.getId().equals(targetProfile.getId())) {
                    ctx.sendMessage(Text.translation("commands.home.teleport.you").green());
                } else {
                    ctx.sendMessage(Text.translation("commands.home.teleport.other").green().arg(targetProfile.getName(), Text::darkGreen));
                }
            };
            Text<?, ?> message = sender.getId().equals(targetProfile.getId()) ?
                Text.translation("tooltip.home_teleport") :
                Text.translation("tooltip.home_teleport.other");
            sender.teleportDelayed(callback, "home", message, bedLocation, 5, true);
        }
    }

    private void teleportHome(CommandContext ctx, Player sender, OfflinePlayer target, Location bedLocation, boolean offline) throws CommandException {
        GameProfile targetProfile = target.getProfile();
        Location safeLocation = adjustBedLocation(bedLocation);
        if (safeLocation != null) {
            startTeleportation(ctx, sender, target, safeLocation, offline);
        } else if (sender.hasPermission("commands.home.obstructed.teleport") && bedLocation != null) {
            Text<?, ?> question = sender.getId().equals(targetProfile.getId()) ?
                Text.translation("tooltip.home_teleport.obstructed.you") :
                Text.translation("tooltip.home_teleport.obstructed.other");
            sender.sendQuestion("home_obstructed", question, 60, answer -> {
                if (answer) {
                    startTeleportation(ctx, sender, target, bedLocation, offline);
                }
            });
        } else {
            if (sender.getId().equals(targetProfile.getId())) {
                throw new CommandException("commands.home.missing.you");
            } else {
                throw new CommandException("commands.home.missing.other", targetProfile.getName());
            }
        }
    }
}
