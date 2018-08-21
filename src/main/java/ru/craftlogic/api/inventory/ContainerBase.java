package ru.craftlogic.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.inventory.InventoryHolder.FieldIdentifier;

public abstract class ContainerBase<I extends InventoryHolder> extends Container {
    private int localSlots;
    private int[] cachedFields;
    protected I inventory;

    public ContainerBase(I inventory) {
        this.cachedFields = new int[inventory.getFieldCount()];
        this.inventory = inventory;
    }

    protected boolean isOutputSlot(Slot slot) {
        return false;
    }

    protected boolean isLocalSlot(Slot slot) {
        return slot.inventory == getInventoryHolder();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        InventoryHolder inventoryHolder = getInventoryHolder();
        if (inventoryHolder != null) {
            return (!(inventoryHolder instanceof TileEntity) || !((TileEntity) inventoryHolder).isInvalid())
                    && inventoryHolder.isUsableByPlayer(player);
        }
        return false;
    }

    protected InventoryHolder getInventoryHolder() {
        return this.inventory;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        InventoryHolder inventoryHolder = this.getInventoryHolder();
        if (inventoryHolder != null && inventoryHolder.getFieldCount() > 0) {
            listener.sendAllWindowProperties(this, this.getInventoryHolder());
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        InventoryHolder inventoryHolder = this.getInventoryHolder();

        if (inventoryHolder.getFieldCount() > 0) {
            for (IContainerListener listener : this.listeners) {
                for (int i = 0; i < inventoryHolder.getFieldCount(); i++) {
                    int newValue = inventoryHolder.getField(i);
                    if (this.cachedFields[i] != newValue) {
                        listener.sendWindowProperty(this, i, newValue);
                        this.cachedFields[i] = newValue;
                    }
                }
            }
        }
    }

    protected int getFieldValue(FieldIdentifier id) {
        return this.getFieldValue(id.id());
    }

    protected int getFieldValue(int id) {
        return this.cachedFields[id];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        this.cachedFields[id] = value;
    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        if (this.isLocalSlot(slot)) {
            this.localSlots++;
        }
        return super.addSlotToContainer(slot);
    }

    protected void addPlayerSlots(InventoryPlayer inv, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inv, j + i * 9 + x + 1, 8 + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(inv, i, x + i * 18, y + 58));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        ItemStack result = ItemStack.EMPTY;
        int hotbarSize = InventoryPlayer.getHotbarSize();
        Slot slot = this.inventorySlots.get(slotId);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (slotId < this.localSlots) {
                boolean isOutput = this.isOutputSlot(slot);
                if (!this.mergeItemStack(stackInSlot, this.localSlots, this.inventorySlots.size(), isOutput)) {
                    return ItemStack.EMPTY;
                }
                if (isOutput) {
                    slot.onSlotChange(stackInSlot, result);
                }
            } else {
                boolean doneLocals = false;
                for (int i = 0; i < this.localSlots; i++) {
                    if (this.inventorySlots.get(i).isItemValid(stackInSlot)) {
                        if (!this.mergeItemStack(stackInSlot, i, i + 1, false)) {
                            continue;
                        }
                        doneLocals = true;
                        break;
                    }
                }
                if (doneLocals) {
                    return ItemStack.EMPTY;
                } else {
                    int totalSlots = this.inventorySlots.size();
                    if (slotId >= this.localSlots && slotId < totalSlots - hotbarSize) {
                        if (!this.mergeItemStack(stackInSlot, totalSlots - hotbarSize, totalSlots, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slotId >= totalSlots - hotbarSize && slotId < totalSlots
                            && !this.mergeItemStack(stackInSlot, this.localSlots, totalSlots - hotbarSize, false)) {

                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return result;
    }
}
