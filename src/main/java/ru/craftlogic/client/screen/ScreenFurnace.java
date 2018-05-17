package ru.craftlogic.client.screen;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.screen.ScreenWithInventory;
import ru.craftlogic.common.inventory.ContainerFurnace;
import ru.craftlogic.util.Furnace;

import static ru.craftlogic.CraftLogic.MODID;

public class ScreenFurnace extends ScreenWithInventory {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/furnace.png");
    private final InventoryPlayer playerInv;
    private final Furnace furnace;
    private final ContainerFurnace container;

    public ScreenFurnace(InventoryPlayer playerInv, Furnace furnace, ContainerFurnace container) {
        super(container);
        this.container = container;
        this.playerInv = playerInv;
        this.furnace = furnace;
    }

    @Override
    protected void init() {}

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedRect(x, y, this.xSize, this.ySize, 0, 0);
        if (this.container.fuel > 0) {
            int fuel = this.getFuelScaled(12);
            this.drawTexturedRect(x + 84, y + 48 - fuel, 14, fuel + 2, 176, 12 - fuel);
        }
        if (this.container.temperature > 0) {
            int tmp = this.getTemperatureScaled(50);
            this.drawTexturedRect(x + 15, y + 67 - tmp, 2, tmp, 200, 50 - tmp);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawCenteredText(this.furnace.getDisplayName(), this.xSize / 2, 9, 0x404040);
        this.drawText(this.playerInv.getDisplayName(), 8, this.ySize - 94, 0x404040);
        if (mouseX - x >= 6 && mouseX - x <= 17 && mouseY - y >= 17 && mouseY - y <= 67) {
            this.drawTooltip(I18n.format("tooltip.temperature", this.container.temperature, this.container.maxTemperature), mouseX - x, mouseY - y);
        }
    }

    public int getFuelScaled(int i) {
        return this.container.maxFuel == 0 ? 0 : this.container.fuel * i / this.container.maxFuel;
    }

    private int getTemperatureScaled(int i) {
        return this.container.maxTemperature == 0 ? 0 : this.container.temperature * i / this.container.maxTemperature;
    }
}
