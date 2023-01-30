package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.entity.AdvancedPlayer;
import ru.craftlogic.api.event.player.PlayerTimedTeleportEvent;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedNetHandlerPlayServer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.BooleanConsumer;
import ru.craftlogic.network.message.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class Player extends OfflinePlayer implements LocatableCommandSender {
    private final Map<String, BooleanConsumer> pendingCallbacks = new HashMap<>();
    private final Set<UUID> pendingTeleports = new HashSet<>();

    public Player(Server server, GameProfile profile) {
        super(server, profile);
    }

    public static Player from(EntityPlayerMP player) {
        return ((AdvancedPlayer)player).wrapped();
    }

    public ItemStack getHeldItem(EnumHand hand) {
        return getEntity().getHeldItem(hand);
    }

    public void setHeldItem(EnumHand hand, ItemStack item) {
        getEntity().setHeldItem(hand, item);
    }

    public InventoryPlayer getInventory() {
        return getEntity().inventory;
    }

    public InventoryEnderChest getEnderInventory() {
        return getEntity().getInventoryEnderChest();
    }

    public String getIP() {
        return getEntity().getPlayerIP();
    }

    public long getLastPlayed() {
        return getEntity().getLastActiveTime();
    }

    public long getTimePlayed() {
        return ((AdvancedPlayer)getEntity()).getTimePlayed();
    }

    @Override
    public Location getLocation() {
        return new Location(getEntity());
    }

    @Override
    public ICommandSender unwrap() {
        return getEntity();
    }

    @Override
    public Player asOnline() {
        return this;
    }

    public void openInteraction(IInteractionObject interaction) {
        getEntity().displayGui(interaction);
    }

    public void openChestInventory(InventoryHolder holder) {
        getEntity().displayGUIChest(holder);
    }

    public void showScreen(ScreenHolder holder) {
        this.showScreen(holder, 0);
    }

    public void showScreen(ScreenHolder holder, int subId) {
        CraftAPI.showScreen(holder, getEntity(), subId);
    }

    public boolean teleport(Location location) {
        return teleport(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getDimensionId());
    }

    public boolean teleport(double x, double y, double z, float yaw, float pitch, int dimension) {
        EntityPlayerMP entity = getEntity();
        Set<SPacketPlayerPosLook.EnumFlags> lookFlags = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        entity.dismountRidingEntity();
        if (entity.dimension != dimension) {
            entity.changeDimension(dimension, new Teleporter(x, y, z));
        }
        entity.connection.setPlayerLocation(x, y, z, yaw, pitch, lookFlags);
        ((AdvancedNetHandlerPlayServer)entity.connection).resetPosition();
        entity.setRotationYawHead(yaw);

        if (!entity.isElytraFlying()) {
            entity.motionY = 0.0D;
            entity.onGround = true;
        }

        return true;
    }

    public UUID teleportDelayed(Consumer<Server> callback, String teleportId, Text<?, ?> toastMessage, Location target, int timeout, boolean freeze) {
        Consumer<Server> task = server -> {
            if (isOnline()) {
                sendPacket(new MessageTimedTeleportEnd(target));
                if (!MinecraftForge.EVENT_BUS.post(new PlayerTimedTeleportEvent(this, target))) {
                    if (freeze) {
                        getEntity().sendPlayerAbilities();
                    }
                    teleport(target);
                    callback.accept(server);
                }
            }
        };
        Location from = getLocation();
        double distance = target.distance(from);
        if (distance <= 200 && target.getDimension() == from.getDimension() || hasPermission("commands.teleport.instant")) {
            task.accept(server);
            return null;
        } else {
            sendPacket(new MessageTimedTeleportStart(target, timeout, freeze));
            sendCountdown(teleportId, toastMessage, timeout);
            return server.addDelayedTask(task, timeout * 1000L + 250);
        }
    }

    public void setGameMode(GameType mode) {
        getEntity().setGameType(mode);
    }

    public GameType getGameMode() {
        return getEntity().interactionManager.getGameType();
    }

    public Container getOpenContainer() {
        return getEntity().openContainer;
    }

    public boolean isFlyingAllowed() {
        return getEntity().capabilities.allowFlying;
    }

    public void setFlyingAllowed(boolean fly) {
        EntityPlayerMP entity = getEntity();
        entity.capabilities.allowFlying = fly;
        if (!fly) {
            entity.capabilities.isFlying = false;
        }
        entity.sendPlayerAbilities();
    }

    public boolean isInvulnerable() {
        return getEntity().capabilities.disableDamage;
    }

    public void setInvulnerable(boolean invulnerable) {
        EntityPlayerMP entity = getEntity();
        entity.capabilities.disableDamage = invulnerable;
        entity.sendPlayerAbilities();
    }

    public boolean isDead() {
        return getEntity().isDead;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getEntity().getDisplayName();
    }

    public EntityPlayerMP getEntity() {
        return this.server.unwrap().getPlayerList().getPlayerByUUID(this.profile.getId());
    }

    public boolean isHurt() {
        EntityPlayerMP entity = getEntity();
        return entity.getHealth() < entity.getMaxHealth();
    }

    public boolean isHungry() {
        EntityPlayerMP entity = getEntity();
        FoodStats foodStats = entity.getFoodStats();
        return foodStats.needFood();
    }

    public boolean feed() {
        if (isHungry()) {
            EntityPlayerMP entity = getEntity();
            FoodStats foodStats = entity.getFoodStats();
            foodStats.setFoodLevel(20);
            return true;
        } else {
            return false;
        }
    }

    public boolean heal() {
        if (isHurt()) {
            EntityPlayerMP entity = getEntity();
            entity.setHealth(entity.getMaxHealth());
            return true;
        } else {
            return false;
        }
    }

    public boolean confirm(String id) {
        return confirm(id, true);
    }

    public boolean decline(String id) {
        return confirm(id, false);
    }

    public boolean confirm(String id, boolean choice) {
        BooleanConsumer callback = this.pendingCallbacks.remove(id);
        if (callback != null) {
            callback.accept(choice);
            return true;
        } else {
            return false;
        }
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        EntityPlayerMP player = getEntity();
        SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
        player.connection.sendPacket(packet);
    }

    public RayTraceResult getLookingSpot() {
        IAttributeInstance reachDistance = getEntity().getEntityAttribute(EntityPlayer.REACH_DISTANCE);
        return this.getLookingSpot(reachDistance.getAttributeValue());
    }
    
    public RayTraceResult getLookingSpot(double distance) {
        return this.getLookingSpot(distance, 1F);
    }

    public RayTraceResult getLookingSpot(double distance, float partialTicks) {
        EntityPlayerMP player = getEntity();
        Vec3d eyes = player.getPositionEyes(partialTicks);
        Vec3d look = player.getLook(partialTicks);
        Vec3d target = eyes.add(look.x * distance, look.y * distance, look.z * distance);
        return player.world.rayTraceBlocks(eyes, target, false, false, true);
    }

    public Location getBedLocation() {
        return getBedLocation(getWorld());
    }

    public Location getBedLocation(World world) {
        BlockPos bed = getEntity().getBedLocation(world.getDimension().getVanilla().getId());
        return bed != null ? new Location(world.unwrap(), bed) : null;
    }

    public void disconnect(Text<?, ?> reason) {
        this.disconnect(reason.build());
    }

    public void disconnect(ITextComponent reason) {
        getEntity().connection.disconnect(reason);
    }

    public long getFirstPlayed() {
        return ((AdvancedPlayer)getEntity()).getFirstPlayed();
    }

    public static final String CMD_COOLDOWN_KEY = "CL:CMD";

    public boolean checkCommandCooldown(@Nonnull String name, boolean replenish, boolean notify, int def) {
        EntityPlayerMP entity = getEntity();
        long duration = 1000L * getPermissionMetadata("cooldown.commands." + name, def, Integer::parseInt);
        if (entity != null && duration > 0) {
            NBTTagCompound data = entity.getEntityData();
            NBTTagCompound cooldown = data.getCompoundTag(CMD_COOLDOWN_KEY);
            if (!data.hasKey(CMD_COOLDOWN_KEY)) {
                data.setTag(CMD_COOLDOWN_KEY, cooldown);
            }
            long now = System.currentTimeMillis();
            long lastUsed = cooldown.getLong(name);
            long delta = now - lastUsed;
            if (delta > duration) {
                if (replenish) {
                    cooldown.setLong(name, now);
                } else {
                    cooldown.removeTag(name);
                }
                return true;
            }
            if (notify) {
                sendMessage(Text.translation("commands.generic.cooldown").red()
                    .arg(CraftMessages.parseDuration(duration - delta).darkRed()));
            }
            return false;
        }
        return true;
    }

    public void sendPacket(AdvancedMessage packet) {
        packet.getNetwork().sendTo(getEntity(), packet);
    }

/*    public void sendPacket(String channel, NBTTagCompound packet) {
        this.sendPacket(new MessageCustom(channel, packet));
    }*/

    public void sendToast(Text<?, ?> title, int timeout) {
        this.sendToast(title.build(), timeout);
    }

    public void sendToast(ITextComponent title, int timeout) {
        this.sendPacket(new MessageToast(title, timeout));
    }

    public void sendToast(Text<?, ?> title, Text<?, ?> subtitle, int timeout) {
        this.sendToast(title.build(), subtitle.build(), timeout);
    }

    public void sendToast(ITextComponent title, ITextComponent subtitle, int timeout) {
        this.sendPacket(new MessageToast(title, subtitle, timeout));
    }

    public void sendCountdown(String id, Text<?, ?> title, int timeout) {
        this.sendCountdown(id, title.build(), timeout);
    }

    public void sendCountdown(String id, Text<?, ?> title, int timeout, int color) {
        this.sendCountdown(id, title.build(), timeout, color);
    }

    public void sendCountdown(String id, ITextComponent title, int timeout) {
        this.sendCountdown(id, title, timeout, 0xFF555555);
    }

    public void sendCountdown(String id, ITextComponent title, int timeout, int color) {
        this.sendCountdown(id, title, timeout, color, CraftSounds.COUNTDOWN_TICK);
    }

    public void sendCountdown(String id, ITextComponent title, int timeout, int color, SoundEvent tickSound) {
        this.sendPacket(new MessageCountdown(id, title, timeout, color, tickSound));
    }

    public void sendTitle(Text<?, ?> title, @Nullable Text<?, ?> subtitle, int fadeIn, int timeout, int fadeOut) {
        this.sendTitle(title.build(), subtitle == null ? null : subtitle.build(), fadeIn, timeout, fadeOut);
    }

    public void sendTitle(ITextComponent title, @Nullable ITextComponent subtitle, int fadeIn, int timeout, int fadeOut) {
        EntityPlayerMP entity = getEntity();
        if (entity != null) {
            NetHandlerPlayServer connection = entity.connection;
            connection.sendPacket(new SPacketTitle(SPacketTitle.Type.RESET, null, 0, 0, 0));
            if (subtitle != null) {
                connection.sendPacket(new SPacketTitle(SPacketTitle.Type.SUBTITLE, subtitle));
            }
            connection.sendPacket(new SPacketTitle(SPacketTitle.Type.TIMES, null, fadeIn, timeout, fadeOut));
            connection.sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, title));
        }
    }

    public void sendQuestion(String id, Text<?, ?> question, int timeout, BooleanConsumer callback) {
        sendQuestion(id, question.build(), timeout, callback);
    }

    public void sendQuestion(String id, ITextComponent question, int timeout, BooleanConsumer callback) {
        sendPacket(new MessageQuestion(id, question, timeout));
        pendingCallbacks.put(id, callback);
    }

    @Override
    public void sendQuestionIfPlayer(String id, ITextComponent question, int timeout, BooleanConsumer callback) {
        sendQuestion(id, question, timeout, callback);
    }

    public void sendToastQuestion(String id, Text<?, ?> question, int color, int timeout, BooleanConsumer callback) {
        sendToastQuestion(id, question.build(), timeout, color, callback);
    }

    public void sendToastQuestion(String id, ITextComponent question, int color, int timeout, BooleanConsumer callback) {
        sendPacket(new MessageToastQuestion(id, question, color, timeout));
        pendingCallbacks.put(id, callback);
    }

    public boolean hasQuestion(String id) {
        return pendingCallbacks.containsKey(id);
    }

    public void sendStatus(Text<?, ?> status) {
        sendStatus(status.build());
    }

    public void sendStatus(ITextComponent status) {
        getEntity().sendStatusMessage(status, true);
    }

    public boolean addPendingTeleport(UUID id) {
        return this.pendingTeleports.add(id);
    }

    public boolean removePendingTeleport(UUID id) {
        return this.pendingTeleports.remove(id) && this.server.cancelTask(id);
    }

    public Set<UUID> getPendingTeleports() {
        return this.pendingTeleports;
    }

    private static class Teleporter implements ITeleporter {
        private final double x, y, z;

        private Teleporter(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void placeEntity(net.minecraft.world.World world, Entity entity, float yaw) {
            entity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
        }
    }
}
