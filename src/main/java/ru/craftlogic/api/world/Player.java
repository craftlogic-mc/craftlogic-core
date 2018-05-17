package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.server.Server;

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

    public void openChestInventory(InventoryHolder holder) {
        getEntity().displayGUIChest(holder);
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

    public void playSound(SoundEvent sound, float volume, float pitch) {
        EntityPlayerMP player = getEntity();
        SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
        player.connection.sendPacket(packet);
    }
}
