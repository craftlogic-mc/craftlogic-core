package ru.craftlogic.client.screen;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.screen.ScreenWithInventory;
import ru.craftlogic.common.inventory.ContainerSmeltingVat;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat;

import static ru.craftlogic.CraftLogic.MODID;

public class ScreenSmeltingVat extends ScreenWithInventory<ContainerSmeltingVat> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/smelter.png");
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
    protected void init() {}

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedRect(x, y, this.xSize, this.ySize, 0, 0);

        if (this.container.getTemperature() > 0) {
            int tmp = this.getTemperatureScaled(50);
            this.drawTexturedRect(x + 15, y + 67 - tmp, 2, tmp, 200, 50 - tmp);
        }
        if (this.container.getProgressTime() > 0) {
            int cook = this.getProgressScaled(24);
            this.drawTexturedRect(x + 89, y + 34, cook + 1, 16, 176, 14);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawCenteredText(this.smeltingVat.getDisplayName(), this.xSize / 2, 9, 0x404040);
        this.drawText(this.playerInv.getDisplayName(), 8, this.ySize - 94, 0x404040);
        if (mouseX - x >= 6 && mouseX - x <= 17 && mouseY - y >= 17 && mouseY - y <= 67) {
            int temperature = this.container.getTemperature();
            int maxTemperature = this.container.getMaxTemperature();
            ITextComponent tooltip = new TextComponentTranslation("tooltip.temperature", temperature, maxTemperature);
            this.drawTooltip(tooltip, mouseX - x, mouseY - y);
        }
    }

    private int getProgressScaled(int i) {
        return this.container.getRequiredTime() == 0
                ? 0 : this.container.getProgressTime() * i / this.container.getRequiredTime();
    }

    private int getTemperatureScaled(int i) {
        return this.container.getMaxTemperature() == 0
                ? 0 : this.container.getTemperature() * i / this.container.getMaxTemperature();
    }
}
