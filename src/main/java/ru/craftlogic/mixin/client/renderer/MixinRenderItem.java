package ru.craftlogic.mixin.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.sin;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Shadow public float zLevel;
    @Shadow @Final
    private ItemModelMesher itemModelMesher;

    /**
     * @author Radviger
     * @reason Dirty hook
     */
    @Overwrite
    public void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack item, int x, int y, @Nullable String countString) {
        if (!item.isEmpty()) {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buff = tess.getBuffer();
            if (item.getCount() != 1 || countString != null) {
                String s = countString == null ? String.valueOf(item.getCount()) : countString;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fontRenderer.drawStringWithShadow(s, (float)(x + 19 - 2 - fontRenderer.getStringWidth(s)), (float)(y + 6 + 3), 0xffffff);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }

            if (item.getItem().showDurabilityBar(item)) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                double health = item.getItem().getDurabilityForDisplay(item);
                int rgb = item.getItem().getRGBDurabilityForDisplay(item);
                int width = Math.round(13.0F - (float)health * 13.0F);
                this.draw(buff, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.draw(buff, x + 2, y + 13, width, 1, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 255);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float cooldown = player == null ? 0F : player.getCooldownTracker().getCooldown(item.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());
            if (cooldown > 0) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();

                buff.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

                float pi = (float)Math.PI;

                float angle = (1 - cooldown) * pi * 2F + pi / 2F;
                float sin = (sin(angle) + 1) / 2F;
                float cos = (cos(angle) + 1) / 2F;

                buff.pos(x + 8, y + 8, this.zLevel).color(255, 255, 255, 127).endVertex();
                buff.pos(x + 8, y, this.zLevel).color(255, 255, 255, 127).endVertex();
                if (cooldown > 0.125) {
                    buff.pos(x + 16, y, this.zLevel).color(255, 255, 255, 127).endVertex();
                }
                if (cooldown > 0.375) {
                    buff.pos(x + 16, y + 16, this.zLevel).color(255, 255, 255, 127).endVertex();
                }
                if (cooldown > 0.625) {
                    buff.pos(x, y + 16, this.zLevel).color(255, 255, 255, 127).endVertex();
                }
                if (cooldown > 0.875) {
                    buff.pos(x, y, this.zLevel).color(255, 255, 255, 127).endVertex();
                }
                if (cooldown > 0.875) {
                    sin = 0;
                } else if (cooldown > 0.625) {
                    cos = 0;
                } else if (cooldown > 0.375) {
                    sin = 1;
                } else if (cooldown > 0.125) {
                    cos = 1;
                } else {
                    sin = 0;
                }
                buff.pos(x + cos * 16, y + sin * 16, this.zLevel).color(255, 255, 255, 127).endVertex();

                tess.draw();

                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

    }

    @Shadow
    private void draw(BufferBuilder buf, int x, int y, int w, int h, int r, int g, int b, int a) {}
}
