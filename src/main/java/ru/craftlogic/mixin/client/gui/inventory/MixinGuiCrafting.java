package ru.craftlogic.mixin.client.gui.inventory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;

@Mixin(GuiCrafting.class)
public abstract class MixinGuiCrafting extends GuiContainer {
    public MixinGuiCrafting(Container container) {
        super(container);
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void initGui() {
        this.buttonList.clear();
        super.initGui();
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void updateScreen() {
        super.updateScreen();
    }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTickas) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTickas);
        this.renderHoveredToolTip(mouseX, mouseY);
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
    protected void actionPerformed(GuiButton p_actionPerformed_1_) { }

    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    protected void keyTyped(char p_keyTyped_1_, int p_keyTyped_2_) throws IOException {
        super.keyTyped(p_keyTyped_1_, p_keyTyped_2_);
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
