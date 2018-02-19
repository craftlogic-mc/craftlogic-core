package ru.craftlogic.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.TileEntityInfo;

public class CraftLogicHooks {
    public static TileEntity createTileEntity(Class<? extends TileEntity> clazz, World world, NBTTagCompound compound) throws Throwable {
        if (TileEntityBase.class.isAssignableFrom(clazz)) {
            TileEntityInfo<?> info = CraftLogic.getTileEntityInfo(clazz);
            if (info != null) {
                return info.create(world);
            } else {
                return null;
            }
        } else {
            return clazz.newInstance();
        }
    }

    public static void sendWindowProperty(EntityPlayerMP player, Container container, int field, int value) {
        player.connection.sendPacket(new SPacketWindowProperty(container.windowId, field, value));
    }

    public static void sendAllWindowProperties(EntityPlayerMP player, Container container, IInventory inventory) {
        for(int field = 0; field < inventory.getFieldCount(); ++field) {
            int value = inventory.getField(field);
            player.connection.sendPacket(new SPacketWindowProperty(container.windowId, field, value));
        }
    }
}
