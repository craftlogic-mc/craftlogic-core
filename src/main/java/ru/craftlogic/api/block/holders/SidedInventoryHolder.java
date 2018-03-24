package ru.craftlogic.api.block.holders;

import net.minecraft.inventory.ISidedInventory;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.util.WrappedSidedInventoryHolder;
import ru.craftlogic.api.world.Location;

public interface SidedInventoryHolder extends InventoryHolder, ISidedInventory {
    static SidedInventoryHolder wrap(Location location) {
        ISidedInventory inventory = location.getTileEntity(ISidedInventory.class);
        return inventory != null ? wrap(inventory, location) : null;
    }

    static SidedInventoryHolder wrap(ISidedInventory inventory, Location location) {
        if (inventory instanceof SidedInventoryHolder) {
            return (SidedInventoryHolder) inventory;
        }
        return new WrappedSidedInventoryHolder(inventory, location);
    }
}
