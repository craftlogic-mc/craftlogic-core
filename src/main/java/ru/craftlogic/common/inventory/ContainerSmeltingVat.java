package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat;

public class ContainerSmeltingVat extends Container {

    private final TileEntitySmeltingVat smeltingVat;

    public ContainerSmeltingVat(InventoryPlayer playerInv, TileEntitySmeltingVat smeltingVat) {
        this.smeltingVat = smeltingVat;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.smeltingVat.isUsableByPlayer(player);
    }
}
