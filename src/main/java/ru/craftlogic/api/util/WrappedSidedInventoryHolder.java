package ru.craftlogic.api.util;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import ru.craftlogic.api.inventory.SidedInventoryHolder;
import ru.craftlogic.api.world.Location;

public class WrappedSidedInventoryHolder extends WrappedInventoryHolder implements SidedInventoryHolder {
    public WrappedSidedInventoryHolder(ISidedInventory inventory, Location location) {
        super(inventory, location);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return ((ISidedInventory)inventory).getSlotsForFace(side);
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {
        return ((ISidedInventory)inventory).canInsertItem(slot, stack, side);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {
        return ((ISidedInventory)inventory).canExtractItem(slot, stack, side);
    }
}
