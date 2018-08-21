package ru.craftlogic.mixin.client.gui.inventory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {
    @Shadow private boolean widthTooNarrow;

    @Shadow @Final private GuiRecipeBook recipeBookGui;

    @Shadow private GuiButtonImage recipeButton;

    @Shadow private float oldMouseX;

    @Shadow private float oldMouseY;

    public MixinGuiInventory(Container container) {
        super(container);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void updateScreen() {
        if (this.mc.playerController.isInCreativeMode()) {
            this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.player));
        }
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void initGui() {
        this.buttonList.clear();
        if (this.mc.playerController.isInCreativeMode()) {
            this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.player));
        } else {
            super.initGui();
        }
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.renderHoveredToolTip(mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected boolean isPointInRegion(int p_isPointInRegion_1_, int p_isPointInRegion_2_, int p_isPointInRegion_3_, int p_isPointInRegion_4_, int p_isPointInRegion_5_, int p_isPointInRegion_6_) {
        return super.isPointInRegion(p_isPointInRegion_1_, p_isPointInRegion_2_, p_isPointInRegion_3_, p_isPointInRegion_4_, p_isPointInRegion_5_, p_isPointInRegion_6_);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected void mouseClicked(int p_mouseClicked_1_, int p_mouseClicked_2_, int p_mouseClicked_3_) throws IOException {
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected boolean hasClickedOutside(int p_hasClickedOutside_1_, int p_hasClickedOutside_2_, int p_hasClickedOutside_3_, int p_hasClickedOutside_4_) {
        return p_hasClickedOutside_1_ < p_hasClickedOutside_3_ || p_hasClickedOutside_2_ < p_hasClickedOutside_4_ || p_hasClickedOutside_1_ >= p_hasClickedOutside_3_ + this.xSize || p_hasClickedOutside_2_ >= p_hasClickedOutside_4_ + this.ySize;
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {}

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected void keyTyped(char symbol, int key) throws IOException {
        super.keyTyped(symbol, key);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected void handleMouseClick(Slot p_handleMouseClick_1_, int p_handleMouseClick_2_, int p_handleMouseClick_3_, ClickType p_handleMouseClick_4_) {
        super.handleMouseClick(p_handleMouseClick_1_, p_handleMouseClick_2_, p_handleMouseClick_3_, p_handleMouseClick_4_);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void recipesUpdated() {}

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void onGuiClosed() {
        super.onGuiClosed();
    }
}
