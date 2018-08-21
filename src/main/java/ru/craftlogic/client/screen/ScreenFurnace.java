package ru.craftlogic.client.screen;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import ru.craftlogic.api.screen.ScreenWithInventory;
import ru.craftlogic.api.screen.element.widget.WidgetFlame;
import ru.craftlogic.api.screen.element.widget.WidgetFluid;
import ru.craftlogic.api.screen.element.widget.WidgetTemperature;
import ru.craftlogic.common.inventory.ContainerFurnace;
import ru.craftlogic.util.Furnace;

public class ScreenFurnace extends ScreenWithInventory<ContainerFurnace> {
    private final InventoryPlayer playerInv;
    private final Furnace furnace;

    public ScreenFurnace(InventoryPlayer playerInv, Furnace furnace, ContainerFurnace container) {
        super(container);
        this.playerInv = playerInv;
        this.furnace = furnace;
    }

    @Override
    protected void init() {
        int x = getLocalX();
        int y = getLocalY();
        this.addElement(new WidgetFluid(this, x + 150, y + 17, () -> FluidRegistry.WATER, () -> 5700, () -> 10_000));
        this.addElement(new WidgetFlame(this, x + 79, y + 35, this.furnace::getFuel));
        this.addElement(new WidgetTemperature(this, x + 6, y + 17, this.furnace::getTemperature, this.furnace::getMaxTemperature));
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.bindTexture(BLANK_TEXTURE);
        this.drawTexturedRect(getLocalX(), getLocalY(), getWidth(), getHeight());
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {
        this.drawCenteredText(this.furnace.getDisplayName(), getWidth() / 2, 9, 0x404040);
        this.drawText(this.playerInv.getDisplayName(), 8, getHeight() - 94, 0x404040);
    }
}
