package ru.craftlogic.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.common.entity.EntityThrownItem;

@SideOnly(Side.CLIENT)
public class RenderThrownItem extends RenderSnowball<EntityThrownItem> {
    public RenderThrownItem(RenderManager renderManager) {
        super(renderManager, Items.AIR, FMLClientHandler.instance().getClient().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(EntityThrownItem item) {
        return item.getItem();
    }
}