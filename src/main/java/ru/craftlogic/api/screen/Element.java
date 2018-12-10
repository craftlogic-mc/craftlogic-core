package ru.craftlogic.api.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public abstract class Element {
    protected static final ResourceLocation ELEMENTS_TEXTURE = new ResourceLocation("craftlogic", "textures/gui/elements.png");
    protected static final Gson GSON = new GsonBuilder().setLenient().create();

    private final ElementContainer container;
    private int x, y;
    private boolean visible = true;
    private ITextComponent tooltip;

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

    public abstract int getWidth();

    public abstract int getHeight();

    public ElementContainer getContainer() {
        return container;
    }

    protected abstract void drawBackground(int mouseX, int mouseY, float deltaTime);

    protected void drawForeground(int mouseX, int mouseY, float deltaTime) {
        if (isVisible() && isMouseOver(mouseX, mouseY)) {
            ITextComponent tooltip = getTooltip(mouseX, mouseY);
            if (tooltip != null) {
                drawTooltip(tooltip, mouseX, mouseY);
            }
        }
    }

    protected boolean isMouseOver(int mouseX, int mouseY) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        return mouseX >= x && mouseY >= y && mouseX <= x + w && mouseY <= y + h;
    }

    public void setTooltip(ITextComponent tooltip) {
        this.tooltip = tooltip;
    }

    protected ITextComponent getTooltip(int mouseX, int mouseY) {
        return tooltip;
    }

    protected void bindDefaultTexture() {
        getContainer().bindTexture(ELEMENTS_TEXTURE);
    }

    protected void drawColoredRect(double x, double y, double w, double h, int color) {
        getContainer().drawColoredRect(x, y, w, h, color);
    }

    protected void drawGradientRect(double x, double y, double w, double h, int from, int to) {
        getContainer().drawGradientRect(x, y, w, h, from, to);
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

    protected static int parseInt(Object e) {
        return ((Number) e).intValue();
    }

    protected static boolean parseBoolean(Object e) {
        return e instanceof String ? e.equals("true") : (Boolean) e;
    }

    protected static ITextComponent parseText(Object e) {
        return e instanceof String ? new TextComponentString((String) e) : (ITextComponent) e;
    }
}
