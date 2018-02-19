package ru.craftlogic.api.block.holders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.ItemStackMatcher;
import ru.craftlogic.api.util.WrappedInventoryHolder;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface InventoryHolder extends IInventory {
    Location getLocation();

    InventoryItemManager getItemManager();

    @Override
    default int getInventoryStackLimit() {
        return 64;
    }

    @Override
    default boolean isUsableByPlayer(EntityPlayer player) {
        Location location = this.getLocation();
        return (!(this instanceof TileEntity) || !((TileEntity) this).isInvalid()) && player.getDistanceSqToCenter(location) <= this.getReachDistanceSq();
    }

    @Override
    default void openInventory(EntityPlayer player) {}

    @Override
    default void closeInventory(EntityPlayer player) {}

    @Override
    default boolean isItemValidForSlot(int slotId, ItemStack stack) {
        return true;
    }

    @Nullable
    default InventoryFieldHolder getFieldHolder() {
        return null;
    }

    @Override
    default int getField(int id) {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        return fieldHolder != null ? fieldHolder.getInvFieldValue(id) : 0;
    }

    @Override
    default void setField(int id, int value) {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        if (fieldHolder != null) {
            fieldHolder.setInvFieldValue(id, value);
        }
    }

    @Override
    default int getFieldCount() {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        if (fieldHolder != null) {
            return fieldHolder.getInvFieldCount();
        }
        return 0;
    }

    default int getReachDistanceSq() {
        return 25;
    }

    @Override
    default String getName() {
        if (this instanceof TileEntityBase) {
            return ((TileEntityBase)this).getItemStack().getUnlocalizedName() + ".name";
        } else {
            return this.getLocation().getBlock().getUnlocalizedName() + ".name";
        }
    }

    @Override
    default boolean hasCustomName() {
        return false;
    }

    @Override
    default int getSizeInventory() {
        return this.getItemManager().size();
    }

    @Override
    default boolean isEmpty() {
        return this.getItemManager().isEmpty();
    }

    @Override
    default ItemStack getStackInSlot(int slotId) {
        return this.getItemManager().get(slotId);
    }

    @Override
    default ItemStack decrStackSize(int slotId, int amount) {
        return this.getItemManager().split(slotId, amount);
    }

    @Override
    default ItemStack removeStackFromSlot(int slotId) {
        return this.getItemManager().remove(slotId);
    }

    @Override
    default void setInventorySlotContents(int slotId, ItemStack stack) {
        this.getItemManager().set(slotId, stack);
    }

    @Override
    default void clear() {
        this.getItemManager().clear();
    }

    default boolean hasItem(Predicate<ItemStack> predicate, int amount) {
        return searchForItem(predicate) >= amount;
    }

    default int searchForItem(Predicate<ItemStack> predicate) {
        int counter = 0;
        for (int i = 0; i < this.getSizeInventory(); i++) {
            ItemStack s = this.getStackInSlot(i);
            if (s != null) {
                if (predicate.test(s)) {
                    counter += s.getCount();
                }
            }
        }
        return counter;
    }

    default int takeItem(ItemStack type, int amount, boolean allowPartialAmount) {
        Predicate<ItemStack> matcher = ItemStackMatcher.typeAndTag(type);
        int count = searchForItem(matcher);
        if (count == 0) {
            return 0;
        }
        if (count >= amount || allowPartialAmount) {
            int insufficient = amount;
            for (int i = 0; i < getSizeInventory(); i++) {
                ItemStack slotItem = this.getStackInSlot(i);
                if (matcher.test(slotItem)) {
                    int c = slotItem.getCount();
                    if (insufficient >= c) {
                        slotItem.setCount(0);
                    } else {
                        slotItem.shrink(insufficient);
                    }
                    insufficient -= c;
                }
            }
            return insufficient;
        }
        return amount - count;
    }

    default boolean hasSpace(Predicate<ItemStack> predicate, int amount) {
        return searchForSpace(predicate) >= amount;
    }

    default int searchForSpace(Predicate<ItemStack> predicate) {
        int counter = 0;
        for (int i = 0; i < this.getSizeInventory(); i++) {
            ItemStack s = this.getStackInSlot(i);
            if (s != null) {
                if (s.isEmpty()) {
                    counter += getInventoryStackLimit();
                } else if (predicate.test(s)) {
                    counter += Math.max(0, s.getMaxStackSize() - s.getCount());
                }
            }
        }
        return counter;
    }

    default int pushItem(ItemStack type, int amount, boolean allowPartialAmount) {
        Predicate<ItemStack> matcher = ItemStackMatcher.typeAndTag(type);
        int count = searchForSpace(matcher);
        if (count == 0) {
            return 0;
        }
        if (count >= amount || allowPartialAmount) {
            int rest = amount;
            for (int i = 0; i < getSizeInventory(); i++) {
                ItemStack slotItem = this.getStackInSlot(i);
                if (slotItem.isEmpty()) {
                    int c = Math.min(type.getMaxStackSize(), rest);
                    slotItem.grow(c);
                    rest -= c;
                } else if (matcher.test(slotItem)) {
                    int c = slotItem.getMaxStackSize() - slotItem.getCount();
                    if (rest >= c) {
                        slotItem.setCount(slotItem.getMaxStackSize());
                    } else {
                        slotItem.grow(c);
                    }
                    rest -= c;
                }
            }
            return rest;
        }
        return amount - count;
    }

    static InventoryHolder wrap(Location location) {
        IInventory inventory = location.getTileEntity(IInventory.class);
        return inventory != null ? wrap(inventory, location) : null;
    }

    static InventoryHolder wrap(IInventory inventory, Location location) {
        if (inventory instanceof InventoryHolder) {
            return (InventoryHolder) inventory;
        } else {
            return new WrappedInventoryHolder(inventory, location);
        }
    }
}
