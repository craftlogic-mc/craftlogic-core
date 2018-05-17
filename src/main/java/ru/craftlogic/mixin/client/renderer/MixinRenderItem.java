package ru.craftlogic.mixin.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Shadow @Final
    private ItemModelMesher itemModelMesher;

    @Overwrite
    protected void registerItem(Item item, int meta, String model) {
        if (Block.getBlockFromItem(item) == Blocks.CARPET && model.endsWith("_carpet")) {
            return;
        }
        this.itemModelMesher.register(item, meta, new ModelResourceLocation(model, "inventory"));
    }
}
