package ru.craftlogic.mixin.client.gui.toast;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RecipeToast.class)
public class MixinToastRecipe {
    /**
     * @author Radviger
     * @reason Removed crafting book recipe notification
     */
    @Overwrite
    public static void addOrUpdate(GuiToast gui, IRecipe recipe) {}
}
