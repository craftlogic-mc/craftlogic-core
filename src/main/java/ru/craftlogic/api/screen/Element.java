package ru.craftlogic.api.screen;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class Element {
    protected static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("craftlogic", "textures/gui/elements.png");

    private final ElementContainer container;
    private int x, y;
    private boolean visible = true;

    public Element(ElementContainer container, int x, int y) {
        this.container = container;
        this.x = x;
        this.y = y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ElementContainer getContainer() {
        return container;
    }

    protected abstract void draw(int mouseX, int mouseY, float deltaTime);

    protected void bindDefaultTexture() {
        getContainer().bindTexture(DEFAULT_TEXTURE);
    }

    protected void drawTexturedRect(double x, double y, double w, double h, double tx, double ty) {
        getContainer().drawTexturedRect(x, y, w, h, tx, ty, 1F, 1F, 1F, 1F);
    }

    protected void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, int rgba) {
        getContainer().drawTexturedRect(x, y, w, h, tx, ty, rgba);
    }

    protected void drawTexturedRect(double x, double y, double w, double h, double tx, double ty, float r, float g, float b, float a) {
        getContainer().drawTexturedRect(x, y, w, h, tx, ty, r, g, b, a);
    }

    protected void drawText(ITextComponent text, int x, int y, int color) {
        getContainer().drawCenteredText(text, x, y, color);
    }

    protected void drawText(ITextComponent text, int x, int y, int color, boolean shadow) {
        getContainer().drawCenteredText(text, x, y, color, shadow);
    }

    protected void drawText(String text, int x, int y, int color) {
        getContainer().drawText(text, x, y, color);
    }

    protected void drawText(String text, int x, int y, int color, boolean shadow) {
        getContainer().drawText(text, x, y, color, shadow);
    }

    protected void drawCenteredText(ITextComponent text, int x, int y, int color) {
        getContainer().drawCenteredText(text, x, y, color);
    }

    protected void drawCenteredText(ITextComponent text, int x, int y, int color, boolean shadow) {
        getContainer().drawCenteredText(text, x, y, color, shadow);
    }

    protected void drawCenteredText(String text, int x, int y, int color) {
        getContainer().drawCenteredText(text, x, y, color);
    }

    protected void drawCenteredText(String text, int x, int y, int color, boolean shadow) {
        getContainer().drawCenteredText(text, x, y, color, shadow);
    }

    protected void drawTooltip(String text, int x, int y) {
        getContainer().drawTooltip(text, x, y);
    }

    protected void drawTooltip(ITextComponent text, int x, int y) {
        getContainer().drawTooltip(text.getUnformattedText(), x, y);
    }

    protected void drawTooltip(List<String> lines, int x, int y) {
        getContainer().drawTooltip(lines, x, y);
    }
}
