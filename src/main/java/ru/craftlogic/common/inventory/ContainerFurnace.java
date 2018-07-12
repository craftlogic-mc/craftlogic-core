package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.inventory.SlotFurnaceOutput;
import ru.craftlogic.api.inventory.ContainerBase;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.util.Furnace;
import ru.craftlogic.util.Furnace.FurnaceField;
import ru.craftlogic.util.Furnace.FurnaceSlot;

public class ContainerFurnace extends ContainerBase {
    private final Furnace furnace;

    public ContainerFurnace(InventoryPlayer playerInv, Furnace furnace) {
        this.furnace = furnace;

        this.addSlotToContainer(new SlotFurnaceFuel(furnace, 0, 56, 35));
        this.addSlotToContainer(new SlotFurnaceOutput(playerInv.player, furnace, 1, 116, 35));

        this.addPlayerSlots(playerInv, 8, 84);
    }

    @Override
    protected InventoryHolder getInventoryHolder() {
        return this.furnace;
    }

    @Override
    protected boolean isOutputSlot(Slot slot) {
        return FurnaceSlot.ASH.matches(slot);
    }

    public int getFuel() {
        return this.getFieldValue(FurnaceField.FUEL);
    }

    public int getMaxFuel() {
        return this.getFieldValue(FurnaceField.MAX_FUEL);
    }

    public int getTemperature() {
        return this.getFieldValue(FurnaceField.TEMPERATURE);
    }

    public int getHotTemperature() {
        return this.getFieldValue(FurnaceField.HOT_TEMPERATURE);
    }

    public int getMaxTemperature() {
        return this.getFieldValue(FurnaceField.MAX_TEMPERATURE);
    }
}
