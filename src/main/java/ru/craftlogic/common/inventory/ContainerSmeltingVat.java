package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceOutput;
import ru.craftlogic.api.inventory.ContainerBase;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat.SmelterField;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat.SmelterSlot;

public class ContainerSmeltingVat extends ContainerBase<TileEntitySmeltingVat> {
    public ContainerSmeltingVat(InventoryPlayer playerInv, TileEntitySmeltingVat smeltingVat) {
        super(smeltingVat);

        this.addSlotToContainer(new Slot(smeltingVat, 0, 47, 35));
        this.addSlotToContainer(new Slot(smeltingVat, 1, 65, 35));
        this.addSlotToContainer(new SlotFurnaceOutput(playerInv.player, smeltingVat, 2, 125, 35));

        this.addPlayerSlots(playerInv, 8, 84);
    }

    @Override
    protected boolean isOutputSlot(Slot slot) {
        return SmelterSlot.OUTPUT.matches(slot);
    }

    public int getProgressTime() {
        return this.getFieldValue(SmelterField.PROGRESS);
    }

    public int getRequiredTime() {
        return this.getFieldValue(SmelterField.REQUIRED_TIME);
    }

    public int getTemperature() {
        return this.getFieldValue(SmelterField.TEMPERATURE);
    }

    public int getMaxTemperature() {
        return this.getFieldValue(SmelterField.MAX_TEMPERATURE);
    }
}
