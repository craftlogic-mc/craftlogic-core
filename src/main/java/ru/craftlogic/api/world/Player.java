package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.CraftNetwork;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.network.message.MessageCountdown;
import ru.craftlogic.network.message.MessageCustom;
import ru.craftlogic.network.message.MessageToast;

import java.util.EnumSet;
import java.util.Set;

public class Player extends OfflinePlayer implements CommandSender {
    public Player(Server server, GameProfile profile) {
        super(server, profile);
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

    @Override
    public long getLastPlayed(World world) {
        return getEntity().getLastActiveTime();
    }

    @Override
    public Location getLocation() {
        return new Location(getEntity());
    }

    @Override
    public ICommandSender getHandle() {
        return getEntity();
    }

    public World getWorld() {
        return this.server.getWorld(Dimension.fromVanilla(getEntity().world.provider.getDimensionType()));
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

    public void showScreen(String name) {
        this.showScreen(name, "");
    }

    public void showScreen(String name, String args) {
        CraftLogic.showScreen(name, getEntity(), args);
    }

    public void showScreen(ScreenHolder holder) {
        this.showScreen(holder, 0);
    }

    public void showScreen(ScreenHolder holder, int subId) {
        CraftLogic.showScreen(holder, getEntity(), subId);
    }

    public boolean teleport(Location location) {
        return this.teleport(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch());
    }

    public boolean teleport(double x, double y, double z, float yaw, float pitch) {
        EntityPlayerMP entity = getEntity();
        Set<SPacketPlayerPosLook.EnumFlags> lookFlags = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        entity.dismountRidingEntity();
        entity.connection.setPlayerLocation(x, y, z, yaw, pitch, lookFlags);
        entity.setRotationYawHead(yaw);

        if (!entity.isElytraFlying()) {
            entity.motionY = 0.0D;
            entity.onGround = true;
        }

        return true;
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

    public boolean isDead() {
        return getEntity().isDead;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getEntity().getDisplayName();
    }

    public EntityPlayerMP getEntity() {
        return this.server.getHandle().getPlayerList().getPlayerByUUID(this.profile.getId());
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

    public void playSound(SoundEvent sound, float volume, float pitch) {
        EntityPlayerMP player = getEntity();
        SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
        player.connection.sendPacket(packet);
    }

    public RayTraceResult getLookingSpot() {
        return this.getLookingSpot(getEntity().capabilities.isCreativeMode ? 5 : 4.5);
    }
    
    public RayTraceResult getLookingSpot(double distance) {
        return this.getLookingSpot(distance, 1F);
    }

    public RayTraceResult getLookingSpot(double distance, float partialTicks) {
        EntityPlayerMP player = getEntity();
        Vec3d eyes = player.getPositionEyes(partialTicks);
        Vec3d look = player.getLook(partialTicks);
        Vec3d target = eyes.addVector(look.x * distance, look.y * distance, look.z * distance);
        return player.world.rayTraceBlocks(eyes, target, false, false, true);
    }

    public void disconnect(Text<?, ?> reason) {
        this.disconnect(reason.build());
    }

    public void disconnect(ITextComponent reason) {
        getEntity().connection.disconnect(reason);
    }

    public long getFirstPlayed() {
        return ((ru.craftlogic.api.entity.Player)getEntity()).getFirstPlayed();
    }

    public void sendPacket(AdvancedMessage packet) {
        CraftNetwork.sendTo(getEntity(), packet);
    }

    public void sendPacket(String channel, NBTTagCompound packet) {
        this.sendPacket(new MessageCustom(channel, packet));
    }

    public void sendToast(Text<?, ?> title, long timeout) {
        this.sendToast(title.build(), timeout);
    }

    public void sendToast(ITextComponent title, long timeout) {
        this.sendPacket(new MessageToast(title, timeout));
    }

    public void sendToast(Text<?, ?> title, Text<?, ?> subtitle, long timeout) {
        this.sendToast(title.build(), subtitle.build(), timeout);
    }

    public void sendToast(ITextComponent title, ITextComponent subtitle, long timeout) {
        this.sendPacket(new MessageToast(title, subtitle, timeout));
    }

    public void sendCountdown(String id, Text<?, ?> title, int timeout) {
        this.sendCountdown(id, title.build(), timeout);
    }

    public void sendCountdown(String id, Text<?, ?> title, int timeout, int color) {
        this.sendCountdown(id, title.build(), timeout, color);
    }

    public void sendCountdown(String id, ITextComponent title, int timeout) {
        this.sendPacket(new MessageCountdown(id, title, timeout));
    }

    public void sendCountdown(String id, ITextComponent title, int timeout, int color) {
        this.sendPacket(new MessageCountdown(id, title, timeout, color));
    }
}
