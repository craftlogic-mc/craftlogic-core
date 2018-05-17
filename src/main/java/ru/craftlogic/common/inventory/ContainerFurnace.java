package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.util.Furnace;

public class ContainerFurnace extends Container {
    private final Furnace furnace;
    public int fuel, maxFuel;
    public int temperature, hotTemperature, maxTemperature;

    public ContainerFurnace(InventoryPlayer playerInv, Furnace furnace) {
        this.furnace = furnace;

        this.addSlotToContainer(new SlotFurnaceFuel(furnace, 0, 56, 35));
        this.addSlotToContainer(new SlotFurnaceOutput(playerInv.player, furnace, 1, 116, 35));

        for (int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlotToContainer(new Slot(playerInv, x, 8 + x * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.furnace.isUsableByPlayer(player);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.furnace);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.fuel != this.furnace.getFuel()) {
                listener.sendWindowProperty(this, 0, this.furnace.getFuel());
            }

            if (this.maxFuel != this.furnace.getMaxFuel()) {
                listener.sendWindowProperty(this, 1, this.furnace.getMaxFuel());
            }

            if (this.temperature != this.furnace.getTemperature()) {
                listener.sendWindowProperty(this, 2, this.furnace.getTemperature());
            }

            if (this.hotTemperature != this.furnace.getHotTemperature()) {
                listener.sendWindowProperty(this, 3, this.furnace.getHotTemperature());
            }

            if (this.maxTemperature != this.furnace.getMaxTemperature()) {
                listener.sendWindowProperty(this, 4, this.furnace.getMaxTemperature());
            }
        }

        this.fuel = this.furnace.getFuel();
        this.maxFuel = this.furnace.getMaxFuel();
        this.temperature = this.furnace.getTemperature();
        this.hotTemperature = this.furnace.getHotTemperature();
        this.maxTemperature = this.furnace.getMaxTemperature();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 0:
                this.fuel = value;
                break;
            case 1:
                this.maxFuel = value;
                break;
            case 2:
                this.temperature = value;
                break;
            case 3:
                this.hotTemperature = value;
                break;
            case 4:
                this.maxTemperature = value;
                break;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotId);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            if (slotId == 1) {
                if (!this.mergeItemStack(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(slotStack, result);
            } else if (slotId != 0) {
                if (Furnace.isItemFuel(slotStack)) {
                    if (!this.mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotId < 29) {
                    if (!this.mergeItemStack(slotStack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotId < 38 && !this.mergeItemStack(slotStack, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotStack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }
}
