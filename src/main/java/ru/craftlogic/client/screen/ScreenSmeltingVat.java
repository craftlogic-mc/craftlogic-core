package ru.craftlogic.client.screen;

import net.minecraft.entity.player.InventoryPlayer;
import ru.craftlogic.api.screen.ScreenWithInventory;
import ru.craftlogic.common.inventory.ContainerSmeltingVat;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat;

public class ScreenSmeltingVat extends ScreenWithInventory<ContainerSmeltingVat> {
    private final InventoryPlayer playerInv;
    private final TileEntitySmeltingVat smeltingVat;
    private final ContainerSmeltingVat container;

    public ScreenSmeltingVat(InventoryPlayer playerInv, TileEntitySmeltingVat smeltingVat, ContainerSmeltingVat container) {
        super(container);
        this.playerInv = playerInv;
        this.smeltingVat = smeltingVat;
        this.container = container;
    }

    @Override
    protected void init() {

    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {

    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {

    }
}
