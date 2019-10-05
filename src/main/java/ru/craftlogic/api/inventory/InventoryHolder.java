package ru.craftlogic.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.util.ItemStackMatcher;
import ru.craftlogic.api.util.WrappedInventoryHolder;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.WorldNameable;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface InventoryHolder extends IInventory, WorldNameable {
    InventoryManager getInventoryManager();

    @Override
    default void markDirty() {}

    @Override
    default int getInventoryStackLimit() {
        return 64;
    }

    @Override
    default boolean isUsableByPlayer(EntityPlayer player) {
        if (this instanceof TileEntity && ((TileEntity) this).isInvalid()) {
            return false;
        }
        if (this instanceof Locatable) {
            Location location = ((Locatable) this).getLocation();
            double distance = player.getDistanceSqToCenter(location.getPos());
            return distance <= getReachDistanceSq(player);
        }
        return true;
    }

    @Override
    default void openInventory(EntityPlayer player) {}

    @Override
    default void closeInventory(EntityPlayer player) {}

    default boolean isItemValidForSlot(SlotIdentifier slot, ItemStack stack) {
        return this.isItemValidForSlot(slot.id(), stack);
    }

    @Override
    default boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Nullable
    InventoryFieldHolder getFieldHolder();

    @Deprecated
    default int getField(FieldIdentifier field) {
        return this.getField(field.id());
    }

    @Override
    @Deprecated
    default int getField(int id) {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        return fieldHolder != null ? fieldHolder.getInvFieldValue(id) : 0;
    }

    @Deprecated
    default void setField(FieldIdentifier field, int value) {
        this.setField(field.id(), value);
    }

    @Override
    @Deprecated
    default void setField(int id, int value) {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        if (fieldHolder != null) {
            fieldHolder.setInvFieldValue(id, value);
        }
    }

    @Override
    @Deprecated
    default int getFieldCount() {
        InventoryFieldHolder fieldHolder = getFieldHolder();
        if (fieldHolder != null) {
            return fieldHolder.getInvFieldCount();
        }
        return 0;
    }

    default double getReachDistanceSq(EntityPlayer player) {
        double d = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1.5;
        return d * d;
    }

    @Override
    default int getSizeInventory() {
        return this.getInventoryManager().size();
    }

    @Override
    default boolean isEmpty() {
        return this.getInventoryManager().isEmpty();
    }

    default ItemStack getStackInSlot(SlotIdentifier slot) {
        return this.getStackInSlot(slot.id());
    }

    @Override
    default ItemStack getStackInSlot(int slot) {
        return this.getInventoryManager().get(slot);
    }

    default ItemStack decrStackSize(SlotIdentifier slot, int amount) {
        return this.decrStackSize(slot.id(), amount);
    }

    @Override
    default ItemStack decrStackSize(int slot, int amount) {
        return this.getInventoryManager().split(slot, amount);
    }

    default ItemStack removeStackFromSlot(SlotIdentifier slot) {
        return this.removeStackFromSlot(slot.id());
    }

    @Override
    default ItemStack removeStackFromSlot(int slot) {
        return this.getInventoryManager().remove(slot);
    }

    default void setInventorySlotContents(SlotIdentifier slot, ItemStack stack) {
        this.setInventorySlotContents(slot.id(), stack);
    }

    @Override
    default void setInventorySlotContents(int slot, ItemStack stack) {
        this.getInventoryManager().set(slot, stack);
    }

    @Override
    default void clear() {
        this.getInventoryManager().clear();
    }

    default boolean growSlotContents(SlotIdentifier slot, Item type, int amount) {
        return growSlotContents(slot.id(), type, amount);
    }

    default boolean growSlotContents(int slot, Item type, int amount) {
        return growSlotContents(slot, new ItemStack(type), amount);
    }

    default boolean growSlotContents(SlotIdentifier slot, ItemStack type, int amount) {
        return growSlotContents(slot.id(), type, amount);
    }

    default boolean growSlotContents(int slot, ItemStack type, int amount) {
        ItemStack s = getStackInSlot(slot);
        if (s.isEmpty()) {
            setInventorySlotContents(slot, type);
            return true;
        } else if (type.isItemEqual(s) && s.getCount() + amount < s.getMaxStackSize()) {
            s.grow(amount);
            return true;
        }
        return false;
    }

    default boolean containsItem(Predicate<ItemStack> predicate) {
        return containsItem(predicate, 1);
    }

    default boolean containsItem(Predicate<ItemStack> predicate, int amount) {
        return countItem(predicate) >= amount;
    }

    default int countItem(Predicate<ItemStack> predicate) {
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

    default int consumeItem(ItemStack type, int amount, boolean allowPartialAmount) {
        Predicate<ItemStack> matcher = ItemStackMatcher.typeAndTag(type);
        int count = countItem(matcher);
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

    default boolean canFitItem(Predicate<ItemStack> predicate, int amount) {
        return searchForEmptySpace(predicate) >= amount;
    }

    default int searchForEmptySpace(Predicate<ItemStack> predicate) {
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

    default int insertItem(ItemStack type, int amount, boolean allowPartialAmount) {
        Predicate<ItemStack> matcher = ItemStackMatcher.typeAndTag(type);
        int space = searchForEmptySpace(matcher);
        if (space == 0) {
            return 0;
        }
        if (space >= amount || allowPartialAmount) {
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
        return amount - space;
    }

    default void dropItems(boolean clear) {
        if (this instanceof Locatable) {
            Location location = ((Locatable) this).getLocation();
            InventoryHelper.dropInventoryItems(location.getWorld(), location.getPos(), this);
            if (clear) {
                this.clear();
            }
        } else {
            throw new UnsupportedOperationException();
        }
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

    interface SlotIdentifier {
        default int id() {
            return ((Enum)this).ordinal();
        }

        default boolean matches(Slot slot) {
            return slot.slotNumber == this.id();
        }
    }

    interface FieldIdentifier {
        default int id() {
            return ((Enum)this).ordinal();
        }
    }
}
