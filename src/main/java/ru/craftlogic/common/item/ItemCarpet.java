package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;

public class ItemCarpet extends ItemBlock implements Colored, ModelAutoReg {
    public ItemCarpet(Block block) {
        super(block);
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue();
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerCustomMeshDefinition(this, stack ->
            new ModelResourceLocation("craftlogic:carpet", "inventory")
        );
    }
}
