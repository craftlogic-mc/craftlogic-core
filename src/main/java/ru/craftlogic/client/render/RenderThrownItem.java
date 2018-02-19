package ru.craftlogic.client.render;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.entity.EntityThrownItem;

@SideOnly(Side.CLIENT)
public class RenderThrownItem extends RenderSnowball<EntityThrownItem> {
    public RenderThrownItem(RenderManager renderManager, RenderItem itemRenderer) {
        super(renderManager, null, itemRenderer);
    }

    @Override
    public ItemStack getStackToRender(EntityThrownItem item) {
        return item.getItem();
    }
}