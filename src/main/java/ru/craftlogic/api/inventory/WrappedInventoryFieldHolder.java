package ru.craftlogic.api.inventory;

import net.minecraft.inventory.IInventory;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class WrappedInventoryFieldHolder extends InventoryFieldHolder {
    private IInventory inventory;

    public WrappedInventoryFieldHolder(IInventory inventory) {
        super(null);
        this.inventory = inventory;
    }

    @Override
    public void addField(int id, IntSupplier getter, IntConsumer setter) {
        throw new UnsupportedOperationException("Can't add field to wrapped inventory holder!");
    }

    @Override
    public int getInvFieldValue(int fieldId) {
        return this.inventory.getField(fieldId);
    }

    @Override
    public int getInvFieldCount() {
        return this.inventory.getFieldCount();
    }
}
