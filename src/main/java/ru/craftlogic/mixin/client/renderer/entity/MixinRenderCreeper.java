package ru.craftlogic.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.entity.Creeper;

@Mixin(RenderCreeper.class)
public abstract class MixinRenderCreeper extends RenderLiving<EntityCreeper> {
    @Shadow @Final private static ResourceLocation CREEPER_TEXTURES;
    private static final ResourceLocation CREEPER_DEPRESSED_TEXTURES = new ResourceLocation(CraftAPI.MOD_ID, "textures/entity/creeper/depressed.png");

    public MixinRenderCreeper(RenderManager renderManager) {
        super(renderManager, new ModelCreeper(), 0.5F);
    }

    /**
     * @author Radviger
     * @reason Custom creepers
     */
    @Overwrite
    protected ResourceLocation getEntityTexture(EntityCreeper creeper) {
        return ((Creeper)creeper).isDepressed() ? CREEPER_DEPRESSED_TEXTURES : CREEPER_TEXTURES;
    }
}
