package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.model.ModelManager;

public class ItemCarpet extends ItemBlock implements Colored, ModelRegistrar {
    public ItemCarpet(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setTranslationKey("woolCarpet");
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getTranslationKey(ItemStack item) {
        return super.getTranslationKey() + "." + EnumDyeColor.byMetadata(item.getMetadata()).getTranslationKey();
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue();
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerCustomMeshDefinition(this, stack ->
            new ModelResourceLocation(CraftAPI.MOD_ID + ":carpet", "inventory")
        );
        modelManager.registerItemVariants(this, CraftAPI.MOD_ID + ":carpet");
    }
}
