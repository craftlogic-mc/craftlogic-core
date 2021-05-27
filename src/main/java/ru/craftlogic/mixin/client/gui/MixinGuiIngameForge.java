package ru.craftlogic.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge extends GuiIngame {
    @Shadow private FontRenderer fontrenderer;

    @Shadow @Final private static int WHITE;

    public MixinGuiIngameForge(Minecraft mc) {
        super(mc);
    }

    /**
     * @author Radviger
     * @reason Fixed glitching message overlay
     */
    @Overwrite(remap = false)
    protected void renderRecordOverlay(int width, int height, float partialTicks) {
        if (overlayMessageTime > 0) {
            mc.profiler.startSection("overlayMessage");
            float hue = (float) overlayMessageTime - partialTicks;
            int opacity = (int) (hue * 256.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (width / 2), (float) (height - 68), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int color = (animateOverlayMessageColor ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                fontrenderer.drawStringWithShadow(overlayMessage, -fontrenderer.getStringWidth(overlayMessage) / 2F, -4, color | (opacity << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            mc.profiler.endSection();
        }
    }
}
