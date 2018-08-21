package ru.craftlogic.client.render.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.client.render.entity.layer.LayerSpiderEyes;
import ru.craftlogic.client.render.model.ModelSpider;

@SideOnly(Side.CLIENT)
public class RenderSpider<S extends EntitySpider> extends RenderLiving<S> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/spider/spider.png");

    public RenderSpider(RenderManager renderManager) {
        super(renderManager, new ModelSpider(), 1F);
        this.addLayer(new LayerSpiderEyes<>(this));
    }

    @Override
    protected float getDeathMaxRotation(S spider) {
        return 180F;
    }

    @Override
    protected ResourceLocation getEntityTexture(S spider) {
        return TEXTURE;
    }
}
