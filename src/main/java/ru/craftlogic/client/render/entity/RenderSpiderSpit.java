package ru.craftlogic.client.render.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.common.entity.projectile.EntitySpiderSpit;

public class RenderSpiderSpit extends RenderSnowball<EntitySpiderSpit> {
    public RenderSpiderSpit(RenderManager renderManager) {
        super(renderManager, Items.AIR, FMLClientHandler.instance().getClient().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(EntitySpiderSpit spit) {
        return new ItemStack(CraftItems.SPIT);
    }
}
