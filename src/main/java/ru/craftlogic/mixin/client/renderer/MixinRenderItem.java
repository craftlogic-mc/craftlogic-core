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
    @Shadow @Final private ItemModelMesher itemModelMesher;

    /**
     * @author Radviger
     * @reason Custom carpet models
     */
    @Overwrite
    public void registerBlock(Block block, int meta, String model) {
        if (block == Blocks.CARPET) {
            return;
        }
        this.registerItem(Item.getItemFromBlock(block), meta, model);
    }

    /**
     * @author Radviger
     * @reason Custom carpet models
     */
    @Overwrite
    public void registerItem(Item item, int meta, String model) {
        this.itemModelMesher.register(item, meta, new ModelResourceLocation(model, "inventory"));
    }
}
