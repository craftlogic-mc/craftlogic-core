package ru.craftlogic.mixin.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends Entity {
    @Shadow
    public NetHandlerPlayServer connection;

    public MixinEntityPlayerMP(World world) {
        super(world);
    }

    @Overwrite
    public void sendWindowProperty(Container container, int id, int value) {
        this.connection.sendPacket(new SPacketWindowProperty(container.windowId, id, value));
    }

    @Overwrite
    public void sendAllWindowProperties(Container container, IInventory inventory) {
        for(int i = 0; i < inventory.getFieldCount(); ++i) {
            this.connection.sendPacket(new SPacketWindowProperty(container.windowId, i, inventory.getField(i)));
        }
    }
}
