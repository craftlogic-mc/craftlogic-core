package ru.craftlogic.api.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.block.holders.ScreenHolder;

import java.util.EnumSet;
import java.util.Set;

public class OnlinePlayer extends Player {
    private final EntityPlayerMP entity;

    public OnlinePlayer(Server server, EntityPlayerMP entity) {
        super(server, entity.getGameProfile());
        this.entity = entity;
    }

    public InventoryPlayer getInventory() {
        return this.entity.inventory;
    }

    public InventoryEnderChest getEnderInventory() {
        return this.entity.getInventoryEnderChest();
    }

    public Location getLocation() {
        return new Location(this.entity);
    }

    public World getWorld() {
        return this.server.getWorld(Dimension.fromVanilla(this.entity.world.provider.getDimensionType()));
    }

    public void openChestInventory(InventoryHolder holder) {
        this.entity.displayGUIChest(holder);
    }

    public void showScreen(ScreenHolder holder) {
        this.showScreen(holder, 0);
    }

    public void showScreen(ScreenHolder holder, int subId) {
        CraftLogic.showScreen(holder, this.entity, subId);
    }

    public boolean teleport(Location location) {
        return this.teleport(location.getX(), location.getY(), location.getZ(), location.getY(), location.getPitch());
    }

    public boolean teleport(double x, double y, double z, float yaw, float pitch) {
        Set<SPacketPlayerPosLook.EnumFlags> lookFlags = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        this.entity.dismountRidingEntity();
        this.entity.connection.setPlayerLocation(x, y, z, yaw, pitch, lookFlags);
        this.entity.setRotationYawHead(yaw);

        if (!this.entity.isElytraFlying()) {
            this.entity.motionY = 0.0D;
            this.entity.onGround = true;
        }
        return true;
    }

    public void setGameMode(GameType mode) {
        this.entity.setGameType(mode);
    }

    public GameType getGameMode() {
        return this.entity.interactionManager.getGameType();
    }

    public Container getOpenContainer() {
        return this.entity.openContainer;
    }

    public boolean isFlyingAllowed() {
        return this.entity.capabilities.allowFlying;
    }

    public void setFlyingAllowed(boolean fly) {
        this.entity.capabilities.allowFlying = fly;
    }
}
