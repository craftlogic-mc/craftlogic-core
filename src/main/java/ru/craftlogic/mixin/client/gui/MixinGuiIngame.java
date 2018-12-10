package ru.craftlogic.mixin.client.gui;

import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame extends Gui {
    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getItemFromBlock(Lnet/minecraft/block/Block;)Lnet/minecraft/item/Item;"))
    public Item getItemFromBlock(Block block) {
        if (block == Blocks.PUMPKIN) {
            block = Blocks.LIT_PUMPKIN;
        }
        return Item.getItemFromBlock(block);
    }
}
