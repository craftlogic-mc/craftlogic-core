package ru.craftlogic.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import ru.craftlogic.api.inventory.holder.InventoryHolder;

public class SlotLocked extends Slot {
    public SlotLocked(InventoryHolder inventory, int id, int x, int y) {
        super(inventory, id, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }
}
