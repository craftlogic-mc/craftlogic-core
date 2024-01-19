package ru.craftlogic.mixin.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    @Shadow protected abstract void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    /**
     * @author Radviger
     * @reason Radial item usage cooldown
     */
    @Overwrite
    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int x, int y, @Nullable String text) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(s, (float) (x + 19 - 2 - fr.getStringWidth(s)), (float) (y + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int color = stack.getItem().getRGBDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float) health * 13.0F);
                draw(buffer, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                draw(buffer, x + 2, y + 13, i, 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float cooldown = player == null ? 0.0F : player.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

            if (cooldown > 0.0F && !stack.getItem().getClass().getName().endsWith("ItemHealingSword")) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                drawCooldown(tessellator.getBuffer(), x, y, cooldown, 255, 255, 255, 127);
                tessellator.draw();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    private void drawCooldown(BufferBuilder buffer, int x, int y, float cooldown, int red, int green, int blue, int alpha) {
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + 8, y + 8, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + 8, y, 0).color(red, green, blue, alpha).endVertex();
        double angle = (-cooldown * 2 * Math.PI - Math.PI / 2);
        double ex = 8 + Math.cos(angle) * 8;
        double ey = 8 + Math.sin(angle) * 8;
        if (cooldown > 0.125) {
            buffer.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        }
        if (cooldown > 0.375) {
            buffer.pos(x, y + 16, 0).color(red, green, blue, alpha).endVertex();
        }
        if (cooldown > 0.625) {
            buffer.pos(x + 16, y + 16, 0).color(red, green, blue, alpha).endVertex();
        }
        if (cooldown > 0.875) {
            buffer.pos(x + 16, y, 0).color(red, green, blue, alpha).endVertex();
            ey = 0;
        } else if (cooldown > 0.625) {
            ex = 16;
        } else if (cooldown > 0.375) {
            ey = 16;
        } else if (cooldown > 0.125) {
            ex = 0;
        } else {
            ey = 0;
        }
        buffer.pos(x + ex, y + ey, 0).color(red, green, blue, alpha).endVertex();
    }
}
