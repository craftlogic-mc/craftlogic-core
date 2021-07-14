package ru.craftlogic.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import ru.craftlogic.api.barrel.BarrelMode;
import ru.craftlogic.common.tileentity.TileEntityBarrel;

public class RenderBarrel extends TileEntitySpecialRenderer<TileEntityBarrel> {
    @Override
    public void render(TileEntityBarrel barrel, double x, double y, double z, float partialTicks, int damage, float alpha) {
        if (!barrel.isEmpty() && !barrel.isClosed()) {
            BarrelMode mode = barrel.getMode();
            float fill = mode.getFill(barrel);

            if (fill > 0) {
                Minecraft mc = Minecraft.getMinecraft();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                int color = mode.getColor(barrel);

                TextureAtlasSprite texture = mode.getTexture(mc, barrel);

                GlStateManager.disableNormalize();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);

                double minU = texture.getMinU();
                double maxU = texture.getMaxU();
                double minV = texture.getMinV();
                double maxV = texture.getMaxV();

                bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;
                int a = (int) (alpha * 255F);

                double dy = fill * 0.875 + 0.0625;

                int light = barrel.getState().getPackedLightmapCoords(barrel.getWorld(), barrel.getPos());
                int sky = light >> 16 & 65535;
                int block = light & 65535;
                buffer.pos(0.125, dy, 0.125).color(r, g, b, a).tex(minU, minV).lightmap(sky, block).endVertex();
                buffer.pos(0.125, dy, 0.875).color(r, g, b, a).tex(minU, maxV).lightmap(sky, block).endVertex();
                buffer.pos(0.875, dy, 0.875).color(r, g, b, a).tex(maxU, maxV).lightmap(sky, block).endVertex();
                buffer.pos(0.875, dy, 0.125).color(r, g, b, a).tex(maxU, minV).lightmap(sky, block).endVertex();

                tessellator.draw();

                GlStateManager.popMatrix();
                GlStateManager.enableLighting();
            }
        }
    }
}
