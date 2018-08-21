package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.inventory.SlotFurnaceOutput;
import ru.craftlogic.api.inventory.ContainerBase;
import ru.craftlogic.util.Furnace;
import ru.craftlogic.util.Furnace.FurnaceField;
import ru.craftlogic.util.Furnace.FurnaceSlot;

public class ContainerFurnace extends ContainerBase<Furnace> {
    public ContainerFurnace(InventoryPlayer playerInv, Furnace furnace) {
        super(furnace);

        this.addSlotToContainer(new SlotFurnaceFuel(furnace, 0, 56, 35));
        this.addSlotToContainer(new SlotFurnaceOutput(playerInv.player, furnace, 1, 116, 35));

        this.addPlayerSlots(playerInv, 8, 84);
    }

    @Override
    protected boolean isOutputSlot(Slot slot) {
        return FurnaceSlot.ASH.matches(slot);
    }

    public float getFuel() {
        int fuel = this.getFieldValue(FurnaceField.FUEL);
        int maxFuel = this.getFieldValue(FurnaceField.MAX_FUEL);
        return maxFuel > 0 ? (float)fuel/maxFuel : 0;
    }

    public int getMaxFuel() {
        return this.getFieldValue(FurnaceField.MAX_FUEL);
    }

    public int getTemperature() {
        return this.getFieldValue(FurnaceField.TEMPERATURE);
    }

    public int getMaxTemperature() {
        return this.getFieldValue(FurnaceField.MAX_TEMPERATURE);
    }
}
