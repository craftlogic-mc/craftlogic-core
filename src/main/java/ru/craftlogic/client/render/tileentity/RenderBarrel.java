package ru.craftlogic.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y, z);

                double minU = (double) texture.getMinU();
                double maxU = (double) texture.getMaxU();
                double minV = (double) texture.getMinV();
                double maxV = (double) texture.getMaxV();

                this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;
                int a = (int) (alpha * 255F);

                double dy = fill * 0.875 + 0.0625;

                buffer.pos(0.125, dy, 0.125).tex(minU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
                buffer.pos(0.125, dy, 0.875).tex(minU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
                buffer.pos(0.875, dy, 0.875).tex(maxU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
                buffer.pos(0.875, dy, 0.125).tex(maxU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();

                tessellator.draw();

                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }
}
