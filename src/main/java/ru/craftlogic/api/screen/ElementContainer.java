package ru.craftlogic.api.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
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

public interface ElementContainer {
    boolean addElement(Element element);
    boolean removeElement(Element element);
    Set<Element> getElements();
    FontRenderer getFontRenderer();
    Tessellator getTessellator();
    Minecraft getClient();
    double getZLevel();
    int getWidth();
    int getHeight();

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

    default void drawTexturedRect(double x, double y, double w, double h) {
        drawTexturedRect(x, y, w, h, 0, 0);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty) {
        drawTexturedRect(x, y, w, h, tx, ty, 1F, 1F, 1F, 1F);
    }

    default void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, float r, float g, float b, float a) {
        this.drawTexturedRect(x, y, w, h, tx, ty, w, h, r, g, b, a);
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
        drawText(text.getFormattedText(), x, y, color);
    }

    default void drawText(String text, int x, int y, int color) {
        getFontRenderer().drawString(text, x, y, color);
    }

    default void drawCenteredText(ITextComponent text, int x, int y, int color) {
        drawCenteredText(text.getFormattedText(), x, y, color);
    }

    default void drawCenteredText(String text, int x, int y, int color) {
        getFontRenderer().drawString(text, (x - getFontRenderer().getStringWidth(text) / 2), (y - getFontRenderer().FONT_HEIGHT / 2), color);
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
        GuiUtils.drawHoveringText(lines, x, y, getWidth(), getHeight(), maxTextWidth, getFontRenderer());
    }

    default void playSound(SoundEvent sound) {
        Minecraft mc = getClient();
        EntityPlayer player = mc.player;
        mc.getSoundHandler().playSound(new PositionedSoundRecord(sound, SoundCategory.MASTER, 1F, 1F, new BlockPos(player)));
    }
}
