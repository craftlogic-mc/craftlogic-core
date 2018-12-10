package ru.craftlogic.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import ru.craftlogic.common.tileentity.TileEntityDryingRack;

public class RenderDryingRack extends TileEntitySpecialRenderer<TileEntityDryingRack> {
    @Override
    public void render(TileEntityDryingRack rack, double x, double y, double z, float partialTicks, int damage, float alpha) {
        if (!rack.isEmpty()) {
            ItemStack item = rack.getIngredient();

            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            mc.getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }


}
