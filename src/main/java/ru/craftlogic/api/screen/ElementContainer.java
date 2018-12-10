package ru.craftlogic.api.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public interface ElementContainer {
    ResourceLocation BLANK_TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/empty.png");

    boolean addElement(Element element);
    boolean removeElement(Element element);
    Set<Element> getElements();
    FontRenderer getFontRenderer();
    Tessellator getTessellator();
    Minecraft getClient();
    double getZLevel();
    int getWindowWidth();
    int getWindowHeight();
    int getWidth();
    int getHeight();
    int getLocalX();
    int getLocalY();

    void drawBackground(int mouseX, int mouseY, float deltaTime);
    void drawForeground(int mouseX, int mouseY, float deltaTime);

    default float getTextureScaleX() {
        return 1;
    }
    default float getTextureScaleY() {
        return 1;
    }

    default void bindTexture(ResourceLocation texture) {
        this.bindTexture(texture, 256, 256);
    }

    void bindTexture(ResourceLocation texture, int width, int height);

    default void drawColoredRect(double x, double y, double w, double h, int color) {
        this.drawGradientRect(x, y, w, h, color, color);
    }

    default void drawGradientRect(double x, double y, double w, double h, int from, int to) {
        float fa = from < 0 || from > 0xFFFFF ? (float)(from >> 24 & 255) / 255.0F : 1F;
        float fr = (float)(from >> 16 & 255) / 255.0F;
        float fg = (float)(from >> 8 & 255) / 255.0F;
        float fb = (float)(from & 255) / 255.0F;
        float ta = to < 0 || to > 0xFFFFF ? (float)(to >> 24 & 255) / 255.0F : 1F;
        float tr = (float)(to >> 16 & 255) / 255.0F;
        float tg = (float)(to >> 8 & 255) / 255.0F;
        float tb = (float)(to & 255) / 255.0F;
        double z = getZLevel();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + w, y, z).color(fr, fg, fb, fa).endVertex();
        buffer.pos(x, y, z).color(fr, fg, fb, fa).endVertex();
        buffer.pos(x, y + h, z).color(tr, tg, tb, ta).endVertex();
        buffer.pos(x + w, y + h, z).color(tr, tg, tb, ta).endVertex();
        tess.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    
    default void drawTexturedRect(double x, double y, double w, double h) {
        drawTexturedRect(x, y, w, h, 0, 0);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty) {
        drawTexturedRect(x, y, w, h, tx, ty, 1F, 1F, 1F, 1F);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, int rgba) {
        float r = (float)((rgba >> 16) & 0xFF) / 255F;
        float g = (float)((rgba >> 8) & 0xFF) / 255F;
        float b = (float)((rgba >> 0) & 0xFF) / 255F;
        float a = rgba < 0 || rgba > 0xFFFFFF ? (float)((rgba >> 24) & 0xFF) / 255F : 1F;
        drawTexturedRect(x, y, w, h, tx, ty, r, g, b, a);
    }
    
    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, float r, float g, float b, float a) {
        this.drawTexturedRect(x, y, w, h, tx, ty, w, h, r, g, b, a);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, double tw, double th, int rgba) {
        float r = (float)((rgba >> 16) & 0xFF) / 255F;
        float g = (float)((rgba >> 8) & 0xFF) / 255F;
        float b = (float)((rgba >> 0) & 0xFF) / 255F;
        float a = rgba < 0 || rgba > 0xFFFFF ? (float)((rgba >> 24) & 0xFF) / 255F : 1F;
        this.drawTexturedRect(x, y, w, h, tx, ty, tw, th, r, g, b, a);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, double tw, double th, float r, float g, float b, float a) {
        Tessellator tess = getTessellator();
        BufferBuilder buffer = tess.getBuffer();
        double z = getZLevel();
        float scaleX = getTextureScaleX();
        float scaleY = getTextureScaleY();

        tx *= scaleX;
        ty *= scaleY;
        tw *= scaleX;
        th *= scaleY;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x, y + h, z).tex(tx, ty + th).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).tex(tx + tw, ty + th).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).tex(tx + tw, ty).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z).tex(tx, ty).color(r, g, b, a).endVertex();
        tess.draw();
    }

    default void drawText(ITextComponent text, int x, int y, int color) {
        drawText(text, x, y, color, false);
    }

    default void drawText(ITextComponent text, int x, int y, int color, boolean shadow) {
        drawText(text.getFormattedText(), x, y, color, shadow);
    }

    default void drawText(String text, int x, int y, int color) {
        drawText(text, x, y, color, false);
    }

    default void drawText(String text, int x, int y, int color, boolean shadow) {
        getFontRenderer().drawString(text, x, y, color, shadow);
    }

    default void drawCenteredText(ITextComponent text, int x, int y, int color) {
        drawCenteredText(text, x, y, color, false);
    }

    default void drawCenteredText(ITextComponent text, int x, int y, int color, boolean shadow) {
        drawCenteredText(text.getFormattedText(), x, y, color, shadow);
    }

    default void drawCenteredText(String text, int x, int y, int color) {
        drawCenteredText(text, x, y, color, false);
    }

    default void drawCenteredText(String text, int x, int y, int color, boolean shadow) {
        getFontRenderer().drawString(text, (x - getFontRenderer().getStringWidth(text) / 2), (y - getFontRenderer().FONT_HEIGHT / 2), color, shadow);
    }

    default void drawTooltip(ITextComponent text, int x, int y) {
        drawTooltip(text.getFormattedText(), x, y);
    }

    default void drawTooltip(String text, int x, int y) {
        drawTooltip(Collections.singletonList(text), x, y);
    }

    default void drawTooltip(List<String> lines, int x, int y) {
        drawTooltip(lines, x, y, -1);
    }

    default void drawTooltip(ITextComponent text, int x, int y, int maxTextWidth) {
        drawTooltip(text.getFormattedText(), x, y, maxTextWidth);
    }

    default void drawTooltip(String text, int x, int y, int maxTextWidth) {
        drawTooltip(Collections.singletonList(text), x, y);
    }

    default void drawTooltip(List<String> lines, int x, int y, int maxTextWidth) {
        x -= getLocalX();
        y -= getLocalY();
        GuiUtils.drawHoveringText(lines, x, y, getWindowWidth(), getWindowHeight(), maxTextWidth, getFontRenderer());
    }

    default void playSound(SoundEvent sound) {
        Minecraft mc = getClient();
        EntityPlayer player = mc.player;
        mc.getSoundHandler().playSound(new PositionedSoundRecord(sound, SoundCategory.MASTER, 1F, 1F, new BlockPos(player)));
    }
}
