package ru.craftlogic.mixin.client.gui.inventory;

import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiRecipeBook.class)
public class MixinGuiRecipeBook {
    /**
     * @author Radviger
     * @reason Removed recipe book
     */
    @Overwrite
    public boolean isVisible() {
        return false;
    }
}
